import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public class CardButton extends JComponent {

    private final Card card;
    private final Image image;
    private final boolean faceDown;

    private boolean hovered;
    private boolean selected;
    private Runnable onClick;

    public CardButton(Card card, Image image, boolean faceDown) {
        this.card = card;
        this.image = image;
        this.faceDown = faceDown;
        this.hovered = false;
        this.selected = false;

        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) {
                    onClick.run();
                }
            }
        });
    }

    public CardButton(Card card, Image image) {
        this(card, image, false);
    }

    public static CardButton back() {
        return new CardButton(null, null, true);
    }

    public Card getCard() {
        return card;
    }

    public Image getImage() {
        return image;
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics g2 = g.create();

        int w = getWidth();
        int h = getHeight();
        int pad = 3;
        int cw = w - pad * 2;
        int ch = h - pad * 2;

        if (faceDown) {
            drawCardBack(g2, pad, pad, cw, ch);
        } else if (image != null) {
            g2.drawImage(image, pad, pad, cw, ch, this);
        } else {
            drawPlaceholder(g2, pad, pad, cw, ch);
        }

        if (hovered || selected) {
            g2.setColor(selected ? new Color(255, 170, 40) : new Color(255, 90, 90));
            g2.drawRect(pad, pad, cw - 1, ch - 1);
        }

        g2.dispose();
    }

    private void drawCardBack(Graphics g2, int x, int y, int w, int h) {
        g2.setColor(new Color(255, 253, 250));
        g2.fillRect(x, y, w, h);

        g2.setColor(new Color(205, 60, 60));
        g2.drawRect(x + 2, y + 2, w - 5, h - 5);

        if (w >= 50) {
            String text = "HORIZON";
            int size = Math.max(9, Math.min(w / 7, h / 8));
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, size));
            FontMetrics fm = g2.getFontMetrics();

            int tx = x + (w - fm.stringWidth(text)) / 2;
            int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(text, tx, ty);
        } else {
            String text = "HORIZON";
            int size = Math.max(7, Math.min(w / 4, h / 9));
            g2.setFont(new Font("Comic Sans MS", Font.BOLD, size));
            FontMetrics fm = g2.getFontMetrics();

            int step = Math.max(8, h / 8);
            int total = step * text.length();
            int startY = y + (h - total) / 2 + fm.getAscent();

            for (int i = 0; i < text.length(); i++) {
                String ch = String.valueOf(text.charAt(i));
                int tx = x + (w - fm.stringWidth(ch)) / 2;
                g2.drawString(ch, tx, startY + i * step);
            }
        }
    }

    private void drawPlaceholder(Graphics g2, int x, int y, int w, int h) {
        g2.setColor(new Color(255, 253, 250));
        g2.fillRect(x, y, w, h);

        g2.setColor(new Color(205, 60, 60));
        g2.drawRect(x + 1, y + 1, w - 3, h - 3);

        String text = card == null ? "CARD" : card.getName();
        int size = Math.max(9, Math.min(w / 8, 13));
        g2.setFont(new Font("Comic Sans MS", Font.BOLD, size));
        FontMetrics fm = g2.getFontMetrics();

        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + h / 2 + fm.getAscent() / 2;

        g2.drawString(text, tx, ty);
    }
}
