import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BoardPanel extends JPanel {

    private Image bgImage;
    private Image blackHoleImage;

    private Image currentDropCardImage;
    private Timer dropZoneTimer;

    private List<Player> players;
    private final Map<String, Image> imageCache;
    private final Map<StarCard, Rectangle> starBounds;
    private final Map<Player, Rectangle> playerBounds;
    private final Set<StarCard> selectableStars;
    private final Set<Player> selectablePlayers;
    private Consumer<StarCard> onStarClicked;
    private Consumer<Player> onPlayerClicked;

    public BoardPanel() {
        setOpaque(true);

        this.players = new ArrayList<>();
        this.imageCache = new HashMap<>();
        this.starBounds = new HashMap<>();
        this.playerBounds = new HashMap<>();
        this.selectableStars = new HashSet<>();
        this.selectablePlayers = new HashSet<>();

        loadImages();
        installMouseHandling();

        this.dropZoneTimer = new Timer(2000, e -> {
            currentDropCardImage = null;
            repaint();
            dropZoneTimer.stop();
        });
        this.dropZoneTimer.setRepeats(false);
    }

    private void loadImages() {
        bgImage = loadImage(ImgPath.Field);
        blackHoleImage = loadImage(ImgPath.Black_hole);
    }

    private void installMouseHandling() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                StarCard star = getStarAt(e.getPoint());

                if (star != null && selectableStars.contains(star) && onStarClicked != null) {
                    onStarClicked.accept(star);
                    return;
                }

                Player player = getPlayerAt(e.getPoint());

                if (player != null && selectablePlayers.contains(player) && onPlayerClicked != null) {
                    onPlayerClicked.accept(player);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean onTarget = false;
                StarCard star = getStarAt(e.getPoint());

                if (star != null && selectableStars.contains(star)) {
                    onTarget = true;
                } else {
                    Player player = getPlayerAt(e.getPoint());
                    onTarget = player != null && selectablePlayers.contains(player);
                }

                setCursor(Cursor.getPredefinedCursor(onTarget ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private StarCard getStarAt(java.awt.Point point) {
        for (Map.Entry<StarCard, Rectangle> entry : starBounds.entrySet()) {
            if (entry.getValue().contains(point)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Player getPlayerAt(java.awt.Point point) {
        for (Map.Entry<Player, Rectangle> entry : playerBounds.entrySet()) {
            if (entry.getValue().contains(point)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Rectangle getStarBounds(StarCard star) {
        return starBounds.get(star);
    }

    public Rectangle getPlayerBounds(Player player) {
        return playerBounds.get(player);
    }

    public void setSelectable(Set<StarCard> stars, Set<Player> players) {
        selectableStars.clear();
        selectablePlayers.clear();

        if (stars != null) {
            selectableStars.addAll(stars);
        }

        if (players != null) {
            selectablePlayers.addAll(players);
        }

        repaint();
    }

    public void clearSelectable() {
        setSelectable(null, null);
    }

    public void setOnStarClicked(Consumer<StarCard> onStarClicked) {
        this.onStarClicked = onStarClicked;
    }

    public void setOnPlayerClicked(Consumer<Player> onPlayerClicked) {
        this.onPlayerClicked = onPlayerClicked;
    }

    private Image loadImage(String path) {
        if (path == null) {
            return null;
        }

        Image img = new ImageIcon(path).getImage();

        if (img.getWidth(null) <= 0) {
            return null;
        }

        return img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics g2 = g.create();

        int w = getWidth();
        int h = getHeight();

        starBounds.clear();
        playerBounds.clear();

        drawBackground(g2, w, h);
        drawAllFields(g2, w, h);
        drawDropZone(g2, w, h);
        drawDropCard(g2, w, h);

        g2.dispose();
    }

    private void drawBackground(Graphics g2, int w, int h) {
        if (bgImage != null) {
            int iw = bgImage.getWidth(null);
            int ih = bgImage.getHeight(null);
            double scale = Math.max(w / (double) iw, h / (double) ih);
            int dw = (int) Math.ceil(iw * scale);
            int dh = (int) Math.ceil(ih * scale);

            g2.drawImage(bgImage, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
        } else {
            g2.setColor(new Color(252, 246, 240));
            g2.fillRect(0, 0, w, h);
        }

        g2.setColor(new Color(255, 252, 249, 170));
        g2.fillRect(0, 0, w, h);
    }

    private void drawAllFields(Graphics g2, int w, int h) {
        if (players == null) {
            return;
        }

        int hcw = horizontalCardWidth(w);
        int hch = (int) (hcw * 1.4);
        int hgap = 5;
        int rowWidth = 9 * hcw + 8 * hgap;

        for (int i = 0; i < players.size() && i < 4; i++) {
            Player p = players.get(i);

            if (p == null) {
                continue;
            }

            if (i == 0) {
                int x = (w - rowWidth) / 2;
                int y = h - hch - 14;

                drawFieldSlots(g2, p, x, y, hcw, hch, hgap, true);
                drawAvatar(g2, p, 16, h - 78, new Color(120, 160, 230));
            } else if (i == 1) {
                int x = (w - rowWidth) / 2;

                drawFieldSlots(g2, p, x, 14, hcw, hch, hgap, true);
                drawAvatar(g2, p, 16, 14, new Color(230, 150, 110));
            } else {
                int scw = 46;
                int sch = 64;
                int sgap = 6;
                int gridW = 3 * scw + 2 * sgap;
                int gridH = 3 * sch + 2 * sgap;
                int x = (i == 2) ? 16 : w - gridW - 16;
                int y = (h - gridH) / 2 + 40;

                drawFieldSlots(g2, p, x, y, scw, sch, sgap, false);

                int ax = (i == 2) ? 16 : w - 166;
                Color accent = (i == 2) ? new Color(140, 200, 150) : new Color(210, 140, 200);
                drawAvatar(g2, p, ax, 92, accent);
            }
        }
    }

    private void drawFieldSlots(Graphics g2, Player p, int startX, int startY,
                                int cw, int ch, int gap, boolean horizontal) {
        StarCard[] field = p.getField();

        if (field == null) {
            return;
        }

        for (int i = 0; i < field.length; i++) {
            int x;
            int y;

            if (horizontal) {
                x = startX + i * (cw + gap);
                y = startY;
            } else {
                x = startX + (i % 3) * (cw + gap);
                y = startY + (i / 3) * (ch + gap);
            }

            drawSlot(g2, x, y, cw, ch);

            if (field[i] != null) {
                drawStar(g2, p, field[i], x, y, cw, ch);

                Rectangle bounds = new Rectangle(x, y, cw, ch);
                starBounds.put(field[i], bounds);

                if (selectableStars.contains(field[i])) {
                    drawTargetGlow(g2, bounds);
                }
            }
        }
    }

    private int horizontalCardWidth(int w) {
        int sideReserve = 2 * (150 + 32);
        int maxByWidth = (w - sideReserve - 8 * 5) / 9;
        return Math.max(40, Math.min(76, maxByWidth));
    }

    private void drawTargetGlow(Graphics g2, Rectangle r) {
        g2.setColor(new Color(255, 170, 40, 235));
        g2.drawRect(r.x - 3, r.y - 3, r.width + 5, r.height + 5);
    }

    private void drawSlot(Graphics g2, int x, int y, int cw, int ch) {
        g2.setColor(new Color(255, 255, 255, 75));
        g2.fillRect(x, y, cw, ch);

        g2.setColor(new Color(190, 70, 70, 125));
        g2.drawRect(x, y, cw, ch);
    }

    private void drawStar(Graphics g2, Player owner, StarCard star, int x, int y, int cw, int ch) {
        Image img = getCardImage(star);

        if (img != null) {
            g2.drawImage(img, x, y, cw, ch, this);
        } else {
            g2.setColor(new Color(255, 253, 250));
            g2.fillRect(x, y, cw, ch);
            g2.setColor(new Color(180, 60, 60));
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 10));
            g2.drawString(star.getName(), x + 4, y + ch / 2);
        }

        boolean shielded = owner.isShielded(star);
        boolean cloaked = owner.isCloaked(star);

        if (cloaked) {
            g2.setColor(new Color(120, 70, 190, 95));
            g2.fillRect(x, y, cw, ch);
        }

        String hp = star.getHp() + "/" + star.getMaxHp();
        int fontSize = Math.max(9, ch / 10);
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();

        int pw = fm.stringWidth(hp) + 8;
        int ph = fm.getHeight() + 1;

        g2.setColor(new Color(180, 40, 40, 220));
        g2.fillRect(x + 3, y + ch - ph - 3, pw, ph);

        g2.setColor(Color.WHITE);
        g2.drawString(hp, x + 7, y + ch - 6);

        if (shielded) {
            g2.setColor(new Color(60, 145, 230, 230));
            g2.drawRect(x + 1, y + 1, cw - 2, ch - 2);
            drawStatusBadge(g2, x + cw - 19, y + 3, "S", new Color(40, 120, 210));
        }

        if (cloaked) {
            int bx = shielded ? x + cw - 37 : x + cw - 19;
            drawStatusBadge(g2, bx, y + 3, "C", new Color(125, 70, 185));
        }
    }

    private void drawStatusBadge(Graphics g2, int x, int y, String text, Color color) {
        int d = 16;

        g2.setColor(color);
        g2.fillRect(x, y, d, d);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Tahoma", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();

        int tx = x + (d - fm.stringWidth(text)) / 2;
        int ty = y + (d - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);
    }

    private void drawAvatar(Graphics g2, Player p, int x, int y, Color accent) {
        int w = 150;
        int h = 64;

        g2.setColor(new Color(255, 255, 255, 215));
        g2.fillRect(x, y, w, h);

        g2.setColor(new Color(190, 60, 60));
        g2.drawRect(x, y, w, h);

        g2.setColor(accent);
        g2.fillOval(x + 10, y + 12, 38, 38);

        String username = p.getUsername() == null ? "?" : p.getUsername();
        String initial = username.isEmpty() ? "?" : username.substring(0, 1).toUpperCase();

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial,
                x + 10 + (38 - fm.stringWidth(initial)) / 2,
                y + 12 + (38 - fm.getHeight()) / 2 + fm.getAscent());

        int stars = 0;

        for (StarCard star : p.getField()) {
            if (star != null) {
                stars++;
            }
        }

        g2.setColor(new Color(120, 30, 30));
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        g2.drawString(username, x + 56, y + 24);

        g2.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
        g2.drawString("AP " + p.getActionPoints() + "   STARS " + stars + "/9", x + 56, y + 44);

        Rectangle bounds = new Rectangle(x, y, w, h);
        playerBounds.put(p, bounds);

        if (selectablePlayers.contains(p)) {
            drawTargetGlow(g2, bounds);
        }
    }

    private void drawDropZone(Graphics g2, int w, int h) {
        int hcw = horizontalCardWidth(w);
        int hch = (int) (hcw * 1.4);
        int topRowBottom = 14 + hch;
        int ownRowTop = h - hch - 14;
        int spaceBetweenRows = ownRowTop - topRowBottom - 16;

        int zoneW = Math.min(240, Math.max(190, w / 4));
        int zoneH = Math.min(260, Math.max(170, spaceBetweenRows));
        int x = (w - zoneW) / 2;
        int y = (h - zoneH) / 2;

        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillRect(x, y, zoneW, zoneH);

        g2.setColor(new Color(190, 50, 50));
        g2.drawRect(x, y, zoneW, zoneH);

        if (blackHoleImage != null) {
            g2.drawImage(blackHoleImage, x + zoneW / 2 - 45, y + 34, 90, 90, this);
        }

        String label = "E - HORIZON ZONE";
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        g2.setColor(new Color(150, 40, 40));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (zoneW - fm.stringWidth(label)) / 2, y + zoneH - 16);
    }

    private void drawDropCard(Graphics g2, int w, int h) {
        if (currentDropCardImage == null) {
            return;
        }

        int cw = 100;
        int ch = 140;
        int x = (w - cw) / 2;
        int y = (h - ch) / 2;

        g2.drawImage(currentDropCardImage, x, y, cw, ch, this);

        g2.setColor(new Color(255, 180, 60));
        g2.drawRect(x, y, cw, ch);
    }

    private Image getCardImage(Card card) {
        if (card == null) {
            return null;
        }

        String key = card.getName();

        if (imageCache.containsKey(key)) {
            return imageCache.get(key);
        }

        String path = ImgPath.get(key);
        Image img = path == null ? null : loadImage(path);
        imageCache.put(key, img);

        return img;
    }

    public void playCardToDropZone(Card card, Image cardImg) {
        this.currentDropCardImage = (cardImg != null) ? cardImg : getCardImage(card);
        repaint();

        if (dropZoneTimer.isRunning()) {
            dropZoneTimer.restart();
        } else {
            dropZoneTimer.start();
        }
    }

    public void setPlayers(List<Player> players) {
        this.players = (players == null) ? new ArrayList<>() : players;
        repaint();
    }

    public List<Player> getPlayers() {
        return players;
    }
}
