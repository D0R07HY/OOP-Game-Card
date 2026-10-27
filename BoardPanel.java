import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public BoardPanel() {
        setOpaque(true);

        this.players = new ArrayList<>();
        this.imageCache = new HashMap<>();

        loadImages();

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

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        drawBackground(g2, w, h);
        drawAllFields(g2, w, h);
        drawDropZone(g2, w, h);
        drawDropCard(g2, w, h);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
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

    private void drawAllFields(Graphics2D g2, int w, int h) {
        if (players == null) {
            return;
        }

        int hcw = Math.min(58, Math.max(40, (w - 280) / 9));
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

    private void drawFieldSlots(Graphics2D g2, Player p, int startX, int startY,
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
                drawStar(g2, field[i], x, y, cw, ch);
            }
        }
    }

    private void drawSlot(Graphics2D g2, int x, int y, int cw, int ch) {
        g2.setColor(new Color(255, 255, 255, 75));
        g2.fillRoundRect(x, y, cw, ch, 10, 10);

        g2.setColor(new Color(190, 70, 70, 125));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10, new float[]{4f, 4f}, 0));
        g2.drawRoundRect(x, y, cw, ch, 10, 10);
    }

    private void drawStar(Graphics2D g2, StarCard star, int x, int y, int cw, int ch) {
        Image img = getCardImage(star);

        g2.setColor(new Color(60, 20, 20, 55));
        g2.fillRoundRect(x + 2, y + 2, cw, ch, 8, 8);

        if (img != null) {
            g2.drawImage(img, x, y, cw, ch, this);
        } else {
            g2.setColor(new Color(255, 253, 250));
            g2.fillRoundRect(x, y, cw, ch, 8, 8);
            g2.setColor(new Color(180, 60, 60));
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, 10));
            g2.drawString(star.getName(), x + 4, y + ch / 2);
        }

        String hp = star.getHp() + "/" + star.getMaxHp();
        int fontSize = Math.max(9, ch / 10);
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();

        int pw = fm.stringWidth(hp) + 8;
        int ph = fm.getHeight() + 1;

        g2.setColor(new Color(180, 40, 40, 220));
        g2.fillRoundRect(x + 3, y + ch - ph - 3, pw, ph, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(hp, x + 7, y + ch - 6);
    }

    private void drawAvatar(Graphics2D g2, Player p, int x, int y, Color accent) {
        int w = 150;
        int h = 64;

        g2.setColor(new Color(70, 20, 20, 35));
        g2.fillRoundRect(x + 3, y + 4, w, h, 14, 14);

        g2.setColor(new Color(255, 255, 255, 215));
        g2.fillRoundRect(x, y, w, h, 14, 14);

        g2.setColor(new Color(190, 60, 60));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 14, 14);

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
    }

    private void drawDropZone(Graphics2D g2, int w, int h) {
        int zoneW = Math.min(240, Math.max(190, w / 4));
        int zoneH = Math.min(260, Math.max(210, h - 130));
        int x = (w - zoneW) / 2;
        int y = (h - zoneH) / 2;

        g2.setColor(new Color(90, 30, 30, 35));
        g2.fillRoundRect(x + 6, y + 8, zoneW, zoneH, 26, 26);

        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillRoundRect(x, y, zoneW, zoneH, 26, 26);

        g2.setColor(new Color(190, 50, 50));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, zoneW, zoneH, 26, 26);

        g2.setColor(new Color(200, 90, 90, 140));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10, new float[]{6f, 6f}, 0));
        g2.drawOval(x + zoneW / 2 - 62, y + 24, 124, 124);

        if (blackHoleImage != null) {
            g2.drawImage(blackHoleImage, x + zoneW / 2 - 45, y + 34, 90, 90, this);
        }

        String label = "E - HORIZON ZONE";
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, 13));
        g2.setColor(new Color(150, 40, 40));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (zoneW - fm.stringWidth(label)) / 2, y + zoneH - 16);
    }

    private void drawDropCard(Graphics2D g2, int w, int h) {
        if (currentDropCardImage == null) {
            return;
        }

        int cw = 100;
        int ch = 140;
        int x = (w - cw) / 2;
        int y = (h - ch) / 2;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(x + 5, y + 7, cw, ch, 12, 12);
        g2.drawImage(currentDropCardImage, x, y, cw, ch, this);

        g2.setColor(new Color(255, 180, 60));
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(x, y, cw, ch, 12, 12);
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
