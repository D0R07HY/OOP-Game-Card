import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class BoardPanel extends JPanel {
    private Image bgImage;
    private Image blackHoleImage;

    private Card currentDropCard;
    private Image currentDropCardImage;
    private Timer dropZoneTimer;

    private List<Player> players;

    public BoardPanel() {
        this.players = new ArrayList<>();
        this.currentDropCard = null;
        this.currentDropCardImage = null;

        loadImages();

        // ตั้งเวลา 2 วินาที (2000 ms) ให้ไพ่ที่เพิ่งเล่นหายไปจาก Drop Zone
        this.dropZoneTimer = new Timer(2000, e -> {
            currentDropCard = null;
            currentDropCardImage = null;
            repaint();
            dropZoneTimer.stop();
        });
        this.dropZoneTimer.setRepeats(false);
    }

    private void loadImages() {
        try {
            bgImage = new ImageIcon(getClass().getResource("/assets/space_bg.png")).getImage();
            blackHoleImage = new ImageIcon(getClass().getResource("/assets/blackhole.png")).getImage();
        } catch (Exception e) {
            System.out.println("[BoardPanel] รูปภาพไม่พบ จะใช้สีพื้นหลังแทน");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();

        // วาดอวกาศ
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, width, height, this);
        } else {
            g.setColor(new Color(15, 15, 30));
            g.fillRect(0, 0, width, height);
        }

        // วาด Drop Zone
        int dropZoneSize = 160;
        int centerX = (width - dropZoneSize) / 2;
        int centerY = (height - dropZoneSize) / 2;
        g.setColor(new Color(255, 255, 255, 80));
        g.drawRect(centerX - 10, centerY - 10, dropZoneSize + 20, dropZoneSize + 20);

        if (blackHoleImage != null) {
            g.drawImage(blackHoleImage, centerX, centerY, dropZoneSize, dropZoneSize, this);
        } else {
            g.setColor(new Color(40, 20, 60));
            g.fillOval(centerX, centerY, dropZoneSize, dropZoneSize);
        }

        // วาดไพ่ที่ถูกโยนลง Drop Zone
        if (currentDropCardImage != null) {
            int cardW = 90;
            int cardH = 130;
            g.drawImage(currentDropCardImage, (width - cardW) / 2, (height - cardH) / 2, cardW, cardH, this);
        }

        drawPlayerAvatars(g, width, height);
    }

    private void drawPlayerAvatars(Graphics g, int w, int h) {
        if (players == null || players.isEmpty()) return;

        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        int[][] positions = {
            { w / 2 - 40, h - 70 }, // South (เรา)
            { w / 2 - 40, 20 },     // North (บน)
            { 20, h / 2 - 40 },     // West (ซ้าย)
            { w - 100, h / 2 - 40 } // East (ขวา)
        };

        for (int i = 0; i < players.size() && i < 4; i++) {
            Player p = players.get(i);
            int x = positions[i][0];
            int y = positions[i][1];

            g.setColor(new Color(100, 150, 255));
            g.fillOval(x, y, 40, 40);
            g.setColor(Color.WHITE);
            g.drawString(p.getUsername(), x, y + 55);
            g.drawString("AP: " + p.getActionPoints(), x, y + 70);
        }
    }

    public void playCardToDropZone(Card card, Image cardImg) {
        this.currentDropCard = card;
        this.currentDropCardImage = cardImg;
        repaint();
        if (dropZoneTimer.isRunning()) {
            dropZoneTimer.restart();
        } else {
            dropZoneTimer.start();
        }
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        repaint();
    }
}
