import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class BoardPanel extends JPanel {

    private List<Player> players = new ArrayList<>();
    private Map<Player, Integer> deckCounts = new HashMap<>();
    private final Map<StarCard, Rectangle> starBounds = new HashMap<>();
    private final Map<Player, Rectangle> playerBounds = new HashMap<>();
    private final Set<StarCard> selectableStars = new HashSet<>();
    private final Set<Player> selectablePlayers = new HashSet<>();
    private Consumer<StarCard> onStarClicked;
    private Consumer<Player> onPlayerClicked;
    private Player localPlayer;

    public BoardPanel() {
        setBackground(Color.LIGHT_GRAY);
        installMouseHandling();
    }

    public void setLocalPlayer(Player player) {
        this.localPlayer = player;
        repaint();
    }

    private void installMouseHandling() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<StarCard, Rectangle> entry : starBounds.entrySet()) {
                    if (entry.getValue().contains(e.getPoint()) && selectableStars.contains(entry.getKey())) {
                        if (onStarClicked != null) onStarClicked.accept(entry.getKey());
                        return;
                    }
                }
                for (Map.Entry<Player, Rectangle> entry : playerBounds.entrySet()) {
                    if (entry.getValue().contains(e.getPoint()) && selectablePlayers.contains(entry.getKey())) {
                        if (onPlayerClicked != null) onPlayerClicked.accept(entry.getKey());
                        return;
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean target = starBounds.entrySet().stream().anyMatch(entry -> entry.getValue().contains(e.getPoint()) && selectableStars.contains(entry.getKey()))
                        || playerBounds.entrySet().stream().anyMatch(entry -> entry.getValue().contains(e.getPoint()) && selectablePlayers.contains(entry.getKey()));
                setCursor(Cursor.getPredefinedCursor(target ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setSelectable(Set<StarCard> stars, Set<Player> players) {
        selectableStars.clear();
        selectablePlayers.clear();
        if (stars != null) selectableStars.addAll(stars);
        if (players != null) selectablePlayers.addAll(players);
        repaint();
    }

    public void clearSelectable() {
        setSelectable(null, null);
    }

    public void setOnStarClicked(Consumer<StarCard> l) { this.onStarClicked = l; }
    public void setOnPlayerClicked(Consumer<Player> l) { this.onPlayerClicked = l; }

    public void playCardToDropZone(Card card, Image cardImg) { repaint(); }
    public void setPlayers(List<Player> players) { this.players = (players == null) ? new ArrayList<>() : players; repaint(); }
    public void setDeckCounts(Map<Player, Integer> deckCounts) { this.deckCounts = (deckCounts == null) ? new HashMap<>() : deckCounts; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        starBounds.clear();
        playerBounds.clear();

        int w = getWidth();
        int h = getHeight();


        if (players == null || players.isEmpty()) return;

        
        List<Player> orderedPlayers = new ArrayList<>();
        if (localPlayer != null && players.contains(localPlayer)) {
            orderedPlayers.add(localPlayer);
            for (Player p : players) {
                if (p != localPlayer) {
                    orderedPlayers.add(p);
                }
            }
        } else {
            orderedPlayers.addAll(players);
        }

        int totalPlayers = Math.min(orderedPlayers.size(), 4);

        
        for (int i = 0; i < totalPlayers; i++) {
            Player p = orderedPlayers.get(i);
            if (p == null) continue;

            int px = 20, py = 20;
            if (i == 0) { px = w / 2 - 225; py = h - 100; }     
            else if (i == 1) { px = w / 2 - 225; py = 20; }     
            else if (i == 2) { px = 20; py = h / 2 - 50; }     
            else if (i == 3) { px = w - 160; py = h / 2 - 50; } 

            
            Rectangle pRect = new Rectangle(px, py, 140, 60);
            playerBounds.put(p, pRect);
            g.setColor(selectablePlayers.contains(p) ? Color.YELLOW : Color.WHITE);
            g.fillRect(pRect.x, pRect.y, pRect.width, pRect.height);
            g.setColor(Color.BLACK);
            g.drawRect(pRect.x, pRect.y, pRect.width, pRect.height);

            Integer deck = deckCounts.get(p);
            g.drawString(p.getUsername() + (!p.isActive() ? " (OUT)" : ""), px + 10, py + 20);
            g.drawString("AP: " + p.getActionPoints() + " | Deck: " + (deck == null ? 0 : deck), px + 10, py + 40);

            
            StarCard[] field = p.getField();
            if (field == null) continue;

            int startX = (i == 0 || i == 1) ? px + 150 : px;
            int startY = (i == 0 || i == 1) ? py : py + 70;

            for (int slot = 0; slot < field.length; slot++) {
                int sx = (i == 0 || i == 1) ? startX + (slot * 55) : startX + (slot % 3 * 45);
                int sy = (i == 0 || i == 1) ? startY : startY + (slot / 3 * 60);
                int sw = (i == 0 || i == 1) ? 50 : 40;
                int sh = (i == 0 || i == 1) ? 60 : 55;

                Rectangle sRect = new Rectangle(sx, sy, sw, sh);
                g.setColor(Color.WHITE);
                g.fillRect(sx, sy, sw, sh);
                g.setColor(Color.BLACK);
                g.drawRect(sx, sy, sw, sh);

                StarCard star = field[slot];
                if (star != null) {
                    starBounds.put(star, sRect);
                    if (selectableStars.contains(star)) {
                        g.setColor(Color.YELLOW);
                        g.drawRect(sx - 2, sy - 2, sw + 4, sh + 4);
                    }

                    
                    String imgPath = ImgPath.get(star.getName());
                    Image cardImg = null;
                    if (imgPath != null) {
                        cardImg = new ImageIcon(imgPath).getImage();
                    }

                    if (cardImg != null && cardImg.getWidth(null) > 0) {
                        g.drawImage(cardImg, sx, sy, sw, sh, this);
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillRect(sx, sy, sw, sh);
                    }

                    g.setColor(Color.BLACK);
                    g.drawRect(sx, sy, sw, sh);

                    
                    g.setColor(Color.RED);
                    g.setFont(new Font("Tahoma", Font.BOLD, 10));
                    g.drawString("HP:" + star.getHp(), sx + 4, sy + 15);

                    String status = (p.isShielded(star) ? "[S]" : "") + (p.isCloaked(star) ? "[C]" : "");
                    if (!status.isEmpty()) {
                        g.setColor(Color.MAGENTA);
                        g.drawString(status, sx + 4, sy + 30);
                    }
                }
            }
        }
    }
}
