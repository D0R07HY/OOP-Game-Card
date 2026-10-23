<<<<<<< HEAD
public class GameWindow {
    
=======
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class GameWindow extends JFrame {

    private BoardPanel boardPanel;
    private JPanel myHandPanel;
    private JPanel topHandPanel;
    private JPanel leftHandPanel;
    private JPanel rightHandPanel;

    public GameWindow() {
        setTitle("Horizon Card - Cosmic Card Online");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        myHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        myHandPanel.setPreferredSize(new Dimension(0, 160));
        myHandPanel.setBackground(new Color(20, 20, 40));
        add(myHandPanel, BorderLayout.SOUTH);

        topHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topHandPanel.setPreferredSize(new Dimension(0, 100));
        topHandPanel.setBackground(new Color(20, 20, 40));
        add(topHandPanel, BorderLayout.NORTH);

        leftHandPanel = new JPanel();
        leftHandPanel.setLayout(new BoxLayout(leftHandPanel, BoxLayout.Y_AXIS));
        leftHandPanel.setPreferredSize(new Dimension(100, 0));
        leftHandPanel.setBackground(new Color(20, 20, 40));
        add(leftHandPanel, BorderLayout.WEST);

        rightHandPanel = new JPanel();
        rightHandPanel.setLayout(new BoxLayout(rightHandPanel, BoxLayout.Y_AXIS));
        rightHandPanel.setPreferredSize(new Dimension(100, 0));
        rightHandPanel.setBackground(new Color(20, 20, 40));
        add(rightHandPanel, BorderLayout.EAST);
    }

    public void updateMyHand(List<Card> hand) {
        myHandPanel.removeAll();

        for (Card card : hand) {
            JButton cardBtn = new JButton(card.getName());
            cardBtn.setPreferredSize(new Dimension(90, 130));
            cardBtn.setBackground(Color.LIGHT_GRAY);

            cardBtn.addActionListener(e -> {
                System.out.println("[UI] กดการ์ด: " + card.getName());
                // TODO: ให้ GameClient ส่งคำสั่ง เช่น client.sendAction("PLAY#" + card.getId());
                boardPanel.playCardToDropZone(card, null);
            });

            myHandPanel.add(cardBtn);
        }

        myHandPanel.revalidate();
        myHandPanel.repaint();
    }

    public void updateOpponentHands(int topCount, int leftCount, int rightCount) {
        topHandPanel.removeAll();
        leftHandPanel.removeAll();
        rightHandPanel.removeAll();

        try {
            Icon cardBackV = new ImageIcon(getClass().getResource("/assets/card_back_v.png"));
            Icon cardBackH = new ImageIcon(getClass().getResource("/assets/card_back_h.png"));

            for (int i = 0; i < topCount; i++) topHandPanel.add(new JLabel(cardBackV));
            for (int i = 0; i < leftCount; i++) {
                JLabel lbl = new JLabel(cardBackH);
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                leftHandPanel.add(lbl);
            }
            for (int i = 0; i < rightCount; i++) {
                JLabel lbl = new JLabel(cardBackH);
                lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                rightHandPanel.add(lbl);
            }
        } catch (Exception e) {
            System.out.println("[UI] ไม่พบภาพหลังการ์ด");
        }

        topHandPanel.revalidate(); topHandPanel.repaint();
        leftHandPanel.revalidate(); leftHandPanel.repaint();
        rightHandPanel.revalidate(); rightHandPanel.repaint();
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }
>>>>>>> 5cd53019fb1c57c04b7b528978f1c443a3a56ec3
}
