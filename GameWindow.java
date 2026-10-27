import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.*;

public class GameWindow extends JFrame {

    private BoardPanel boardPanel;
    private JPanel myHandPanel;
    private JPanel topHandPanel;
    private JPanel leftHandPanel;
    private JPanel rightHandPanel;

    private Player myPlayer;
    private Consumer<Card> onCardSelected;

    public GameWindow() {
        setTitle("Horizon CardOnline");
        setSize(1280, 760);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(252, 246, 240));

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        myHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        myHandPanel.setOpaque(false);
        myHandPanel.setPreferredSize(new Dimension(0, 155));
        myHandPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(200, 70, 70)));
        add(myHandPanel, BorderLayout.SOUTH);

        topHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        topHandPanel.setOpaque(false);
        topHandPanel.setPreferredSize(new Dimension(0, 104));
        topHandPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 70, 70)));
        add(topHandPanel, BorderLayout.NORTH);

        leftHandPanel = createSidePanel();
        leftHandPanel.setPreferredSize(new Dimension(110, 0));
        leftHandPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(200, 70, 70)));
        add(leftHandPanel, BorderLayout.WEST);

        rightHandPanel = createSidePanel();
        rightHandPanel.setPreferredSize(new Dimension(110, 0));
        rightHandPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(200, 70, 70)));
        add(rightHandPanel, BorderLayout.EAST);
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    public void setMyPlayer(Player player) {
        this.myPlayer = player;
        updateMyHand(player == null ? new ArrayList<>() : player.getHand());
    }

    public void setPlayers(List<Player> players) {
        boardPanel.setPlayers(players);
    }

    public void setOnCardSelected(Consumer<Card> onCardSelected) {
        this.onCardSelected = onCardSelected;
    }

    public void updateMyHand(List<Card> hand) {
        myHandPanel.removeAll();

        if (hand != null) {
            for (Card card : hand) {
                Image img = loadCardImage(card);
                CardButton cardBtn = new CardButton(card, img);
                cardBtn.setPreferredSize(new Dimension(92, 129));
                cardBtn.setToolTipText(card.getName());

                cardBtn.setOnClick(() -> {
                    System.out.println("[UI] Card clicked: " + card.getName());

                    if (myPlayer != null && myPlayer.getHand().contains(card)) {
                        myPlayer.removeCardFromHand(card);
                    }

                    boardPanel.playCardToDropZone(card, img);

                    if (myPlayer != null) {
                        updateMyHand(myPlayer.getHand());
                    }

                    if (onCardSelected != null) {
                        onCardSelected.accept(card);
                    }
                });

                myHandPanel.add(cardBtn);
            }
        }

        myHandPanel.revalidate();
        myHandPanel.repaint();
    }

    public void updateOpponentHands(int topCount, int leftCount, int rightCount) {
        topHandPanel.removeAll();
        leftHandPanel.removeAll();
        rightHandPanel.removeAll();

        for (int i = 0; i < topCount; i++) {
            CardButton back = CardButton.back();
            back.setPreferredSize(new Dimension(60, 84));
            back.setMaximumSize(new Dimension(60, 84));
            topHandPanel.add(back);
        }

        for (int i = 0; i < leftCount; i++) {
            if (i > 0) {
                leftHandPanel.add(Box.createVerticalStrut(5));
            }

            CardButton back = CardButton.back();
            back.setPreferredSize(new Dimension(48, 67));
            back.setMaximumSize(new Dimension(48, 67));
            back.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftHandPanel.add(back);
        }

        for (int i = 0; i < rightCount; i++) {
            if (i > 0) {
                rightHandPanel.add(Box.createVerticalStrut(5));
            }

            CardButton back = CardButton.back();
            back.setPreferredSize(new Dimension(48, 67));
            back.setMaximumSize(new Dimension(48, 67));
            back.setAlignmentX(Component.CENTER_ALIGNMENT);
            rightHandPanel.add(back);
        }

        topHandPanel.revalidate();
        topHandPanel.repaint();
        leftHandPanel.revalidate();
        leftHandPanel.repaint();
        rightHandPanel.revalidate();
        rightHandPanel.repaint();
    }

    public void updateBoardState(String state) {
        System.out.println("[UI] Update board with state: " + state);
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    private Image loadCardImage(Card card) {
        if (card == null) {
            return null;
        }

        String path = ImgPath.get(card.getName());

        if (path == null) {
            return null;
        }

        Image img = new ImageIcon(path).getImage();

        if (img.getWidth(null) <= 0) {
            return null;
        }

        return img;
    }

    public void loadDemo() {
        Player me = new Player("YOU");
        Player north = new Player("NOVA");
        Player west = new Player("ORBIT");
        Player east = new Player("COMET");

        me.placeStar(star("Earth", 5, 3));
        me.placeStar(star("Mars", 1, 4));
        me.placeStar(star("Jupiter", 4, 2));

        north.placeStar(star("Venus", 2, 1));
        north.placeStar(star("Saturn", 4, 5));

        west.placeStar(star("Mercury", 1, 0));
        west.placeStar(star("Uranus", 3, 6));

        east.placeStar(star("Neptune", 3, 7));
        east.placeStar(star("The Sun", 5, 8));

        me.addCardToHand(star("Earth", 5, 0));
        me.addCardToHand(star("Mars", 1, 0));
        me.addCardToHand(new MeteorStrike("DEMO-METEOR", ImgPath.get("Meteor Strike")));
        me.addCardToHand(new ForceField("DEMO-FORCE", ImgPath.get("Force Field")));
        me.addCardToHand(new NebulaCloak("DEMO-CLOAK", ImgPath.get("Nebula Cloak")));
        me.addCardToHand(new SpaceProbe("DEMO-PROBE", ImgPath.get("Space Probe")));

        List<Player> players = new ArrayList<>();
        players.add(me);
        players.add(north);
        players.add(west);
        players.add(east);

        boardPanel.setPlayers(players);
        setMyPlayer(me);
        updateOpponentHands(5, 5, 4);
    }

    private static StarCard star(String name, int hp, int orbit) {
        return new StarCard("DEMO-" + name + "-" + orbit, name, hp, ImgPath.get(name), orbit);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.loadDemo();
            window.setVisible(true);
        });
    }
}
