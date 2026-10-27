import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameWindow extends JFrame {

    private enum PendingMode {
        NONE, ENEMY_STAR, ALLY_STAR, ENEMY_PLAYER, COST_CARD
    }

    private BoardPanel boardPanel;
    private JPanel myHandPanel;
    private JPanel topHandPanel;
    private JPanel leftHandPanel;
    private JPanel rightHandPanel;
    private JLabel statusLabel;
    private JButton endTurnButton;

    private Player myPlayer;
    private List<Player> players;
    private GameState gameState;
    private GameClient client;
    private Consumer<Card> onCardSelected;

    private Card pendingCard;
    private PendingMode pendingMode;
    private StarCard pendingStar;
    private Player pendingTarget;

    public GameWindow() {
        setTitle("Horizon CardOnline");

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int windowWidth = Math.min(1560, Math.max(1280, screen.width - 60));
        int windowHeight = Math.min(920, Math.max(720, screen.height - 70));

        setSize(windowWidth, windowHeight);
        setMinimumSize(new Dimension(Math.min(1280, windowWidth), 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(252, 246, 240));

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        boardPanel = new BoardPanel();
        boardPanel.setOnStarClicked(this::handleStarClick);
        boardPanel.setOnPlayerClicked(this::handlePlayerClick);
        add(boardPanel, BorderLayout.CENTER);

        myHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        myHandPanel.setOpaque(false);
        myHandPanel.setPreferredSize(new Dimension(0, 150));
        myHandPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(200, 70, 70)));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(createStatusBar(), BorderLayout.NORTH);
        southPanel.add(myHandPanel, BorderLayout.CENTER);
        southPanel.setPreferredSize(new Dimension(0, 188));
        add(southPanel, BorderLayout.SOUTH);

        topHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        topHandPanel.setOpaque(false);
        topHandPanel.setPreferredSize(new Dimension(0, 104));
        topHandPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 70, 70)));
        add(topHandPanel, BorderLayout.NORTH);

        leftHandPanel = createSidePanel();
        leftHandPanel.setPreferredSize(new Dimension(202, 0));
        leftHandPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(200, 70, 70)));
        add(leftHandPanel, BorderLayout.WEST);

        rightHandPanel = createSidePanel();
        rightHandPanel.setPreferredSize(new Dimension(202, 0));
        rightHandPanel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(200, 70, 70)));
        add(rightHandPanel, BorderLayout.EAST);

        pendingMode = PendingMode.NONE;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 16));

        statusLabel = new JLabel("เลือกการ์ดจากมือเพื่อเล่น");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        statusLabel.setForeground(new Color(120, 30, 30));
        bar.add(statusLabel, BorderLayout.CENTER);

        endTurnButton = new JButton("จบเทิร์น");
        endTurnButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
        endTurnButton.addActionListener(this::onEndTurn);
        bar.add(endTurnButton, BorderLayout.EAST);

        return bar;
    }

    private JPanel createSidePanel() {
        return new SideHandPanel();
    }

    public void setMyPlayer(Player player) {
        this.myPlayer = player;
        updateMyHand(player == null ? new ArrayList<>() : player.getHand());
    }

    public Player getMyPlayer() {
        return myPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getStatusText() {
        return statusLabel == null ? "" : statusLabel.getText();
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        boardPanel.setPlayers(players);

        if (gameState == null && players != null) {
            gameState = new GameState(players);
        }
    }

    public void setOnCardSelected(Consumer<Card> onCardSelected) {
        this.onCardSelected = onCardSelected;
    }

    public void setGameClient(GameClient client) {
        this.client = client;
    }

    public void updateMyHand(List<Card> hand) {
        myHandPanel.removeAll();

        int count = hand == null ? 0 : hand.size();
        int cardW = 92;
        int cardH = 129;

        if (count > 0) {
            int available = myHandPanel.getWidth();

            if (available < 300) {
                available = Math.max(620, getWidth() - 240);
            }

            int usable = Math.max(0, available - 8 * (count + 1));
            cardW = Math.max(52, Math.min(92, usable / count));
            cardH = Math.min(129, Math.max(72, (int) Math.round(cardW * 1.4)));
        }

        if (hand != null) {
            for (Card card : hand) {
                Image img = loadCardImage(card);
                CardButton cardBtn = new CardButton(card, img);
                cardBtn.setPreferredSize(new Dimension(cardW, cardH));
                cardBtn.setToolTipText(card.getName());
                cardBtn.setSelected(card == pendingCard);

                cardBtn.setOnClick(() -> handleHandCardClick(card));

                myHandPanel.add(cardBtn);
            }
        }

        myHandPanel.revalidate();
        myHandPanel.repaint();
    }

    private void handleHandCardClick(Card card) {
        System.out.println("[UI] กดการ์ด: " + card.getName());

        if (pendingMode == PendingMode.COST_CARD) {
            resolveCostCard(card);
            return;
        }

        if (pendingCard == card) {
            cancelPending();
            setStatus("ยกเลิกการเลือกการ์ดแล้ว");
            return;
        }

        if (pendingCard != null) {
            cancelPending();
        }

        startCardPlay(card);
    }

    private void startCardPlay(Card card) {
        if (myPlayer == null || gameState == null || players == null) {
            setStatus("ยังไม่มีเกมที่พร้อมเล่น");
            return;
        }

        if (client != null && gameState.getCurrentPlayer() != myPlayer) {
            setStatus("ยังไม่ถึงเทิร์นของคุณ");
            return;
        }

        if (!myPlayer.getHand().contains(card)) {
            return;
        }

        if (myPlayer.getActionPoints() <= 0) {
            setStatus("AP หมดแล้ว (0/2) — กด \"จบเทิร์น\" เพื่อเริ่มเทิร์นใหม่");
            updateMyHand(myPlayer.getHand());
            return;
        }

        if (card instanceof StarCard) {
            placeStarCard((StarCard) card);
            return;
        }

        if (!(card instanceof SpecialCard)) {
            setStatus("ไม่รู้จักชนิดการ์ดนี้: " + card.getName());
            return;
        }

        String name = card.getName();

        if (name.equals("Meteor Strike")) {
            Set<StarCard> targets = enemyStars();

            if (targets.isEmpty()) {
                setStatus("Meteor Strike: ไม่มีดาวคู่แข่งที่เลือกได้");
                return;
            }

            beginTargeting(card, PendingMode.ENEMY_STAR, targets, null,
                    "Meteor Strike: เลือกดาวของคู่แข่ง 1 ดวง (ดาเมจ 2)");
        } else if (name.equals("Supernova")) {
            Set<Player> targets = new HashSet<>();

            for (int i = 1; i < players.size(); i++) {
                if (hasTargetableStar(players.get(i))) {
                    targets.add(players.get(i));
                }
            }

            if (targets.isEmpty()) {
                setStatus("Supernova: ไม่มีผู้เล่นเป้าหมายที่เลือกได้");
                return;
            }

            beginTargeting(card, PendingMode.ENEMY_PLAYER, null, targets,
                    "Supernova: เลือกผู้เล่นเป้าหมาย (ดาเมจทุกดาว 1)");
        } else if (name.equals("Pulsar")) {
            if (myPlayer.isPulsarUsedThisTurn()) {
                setStatus("Pulsar ใช้ได้ 1 ครั้งต่อเทิร์นเท่านั้น");
                return;
            }

            if (otherHandCardCount(card) == 0) {
                setStatus("Pulsar: ต้องมีการ์ดอีก 1 ใบในมือเพื่อทิ้งเป็นค่าการใช้");
                return;
            }

            Set<StarCard> targets = enemyStars();

            if (targets.isEmpty()) {
                setStatus("Pulsar: ไม่มีดาวคู่แข่งที่เลือกได้");
                return;
            }

            beginTargeting(card, PendingMode.ENEMY_STAR, targets, null,
                    "Pulsar: เลือกดาวคู่แข่งที่จะทำลาย");
        } else if (name.equals("Force Field")) {
            Set<StarCard> targets = ownStars();

            if (targets.isEmpty()) {
                setStatus("Force Field: คุณยังไม่มีดาวบนสนาม");
                return;
            }

            beginTargeting(card, PendingMode.ALLY_STAR, targets, null,
                    "Force Field: เลือกดาวของตนเองเพื่อติดโล่ 1 ครั้ง");
        } else if (name.equals("Nebula Cloak")) {
            Set<StarCard> targets = ownStars();

            if (targets.isEmpty()) {
                setStatus("Nebula Cloak: คุณยังไม่มีดาวบนสนาม");
                return;
            }

            beginTargeting(card, PendingMode.ALLY_STAR, targets, null,
                    "Nebula Cloak: เลือกดาวของตนเองเพื่อซ่อนจากเป้าหมายโจมตี");
        } else if (name.equals("Stellar Regeneration")) {
            Set<StarCard> targets = ownStars();

            if (targets.isEmpty()) {
                setStatus("Stellar Regeneration: คุณยังไม่มีดาวบนสนาม");
                return;
            }

            beginTargeting(card, PendingMode.ALLY_STAR, targets, null,
                    "Stellar Regeneration: เลือกดาวของตนเองเพื่อฟื้นฟู 3 HP");
        } else if (name.equals("Space Probe")) {
            playNoTarget(card, "Space Probe: จั่วการ์ด 2 ใบ");
        } else if (name.equals("Big Bang")) {
            playNoTarget(card, "Big Bang: นำ The Sun ลงสนามและฟื้นฟู HP ดาวทุกดวง");
        } else if (name.equals("Black Hole")) {
            if (gameState.isBlackHoleUsed()) {
                setStatus("Black Hole ถูกใช้ไปแล้วในเกมนี้");
                return;
            }

            playNoTarget(card, "Black Hole: ทำลายดาวทุกดวงบนสนาม");
        } else if (name.equals("Worm Hole")) {
            if (otherHandCardCount(card) == 0) {
                setStatus("Worm Hole: ต้องมีการ์ดอีก 1 ใบในมือเพื่อใช้แลก");
                return;
            }

            Set<Player> targets = new HashSet<>();

            for (int i = 1; i < players.size(); i++) {
                if (!players.get(i).getHand().isEmpty()) {
                    targets.add(players.get(i));
                }
            }

            if (targets.isEmpty()) {
                setStatus("Worm Hole: ไม่มีคู่แข่งที่มีการ์ดในมือให้แลก");
                return;
            }

            beginTargeting(card, PendingMode.ENEMY_PLAYER, null, targets,
                    "Worm Hole: เลือกผู้เล่นเป้าหมายที่จะแลกการ์ด");
        } else {
            setStatus("ยังไม่ได้เชื่อม Logic ของการ์ด: " + name);
        }
    }

    private void beginTargeting(Card card, PendingMode mode, Set<StarCard> stars,
                                Set<Player> targetPlayers, String message) {
        pendingCard = card;
        pendingMode = mode;
        pendingStar = null;
        pendingTarget = null;

        boardPanel.setSelectable(stars, targetPlayers);
        updateMyHand(myPlayer.getHand());
        setStatus(message);
    }

    public void cancelPending() {
        pendingCard = null;
        pendingMode = PendingMode.NONE;
        pendingStar = null;
        pendingTarget = null;
        boardPanel.clearSelectable();

        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }
    }

    private void handleStarClick(StarCard star) {
        if (pendingCard == null) {
            return;
        }

        Player owner = findOwner(star);

        if (owner == null) {
            return;
        }

        if (pendingMode == PendingMode.ENEMY_STAR) {
            if (owner == myPlayer) {
                setStatus("เลือกดาวของคู่แข่ง ไม่ใช่ดาวของตนเอง");
                return;
            }

            if (pendingCard.getName().equals("Pulsar")) {
                if (otherHandCardCount(pendingCard) == 0) {
                    cancelPending();
                    setStatus("Pulsar: ไม่มีการ์ดใบอื่นให้ทิ้งแล้ว");
                    return;
                }

                pendingStar = star;
                pendingTarget = owner;
                pendingMode = PendingMode.COST_CARD;
                boardPanel.clearSelectable();
                updateMyHand(myPlayer.getHand());
                setStatus("Pulsar: เลือกการ์ดอีก 1 ใบในมือเพื่อทิ้งเป็นค่าการใช้");
                return;
            }

            if (sendPlayToServer(pendingCard, owner, star, null)) {
                return;
            }

            if (!myPlayer.useActionPoint(1)) {
                cancelPending();
                setStatus("AP ไม่พอสำหรับเล่นการ์ด");
                return;
            }

            pendingCard.playEffect(myPlayer, owner, star, gameState);
            finishPlay(pendingCard, "Meteor Strike: " + star.getName()
                    + " ของ " + owner.getUsername() + " ถูกโจมตี");
        } else if (pendingMode == PendingMode.ALLY_STAR) {
            if (owner != myPlayer) {
                setStatus("เลือกดาวของตนเองเท่านั้น");
                return;
            }

            if (sendPlayToServer(pendingCard, myPlayer, star, null)) {
                return;
            }

            if (!myPlayer.useActionPoint(1)) {
                cancelPending();
                setStatus("AP ไม่พอสำหรับเล่นการ์ด");
                return;
            }

            String cardName = pendingCard.getName();
            pendingCard.playEffect(myPlayer, myPlayer, star, gameState);

            if (cardName.equals("Stellar Regeneration")) {
                finishPlay(pendingCard, "Stellar Regeneration: ฟื้นฟู " + star.getName()
                        + " เป็น " + star.getHp() + "/" + star.getMaxHp());
            } else if (cardName.equals("Force Field")) {
                finishPlay(pendingCard, "Force Field: " + star.getName() + " ได้รับโล่ 1 ครั้ง");
            } else {
                finishPlay(pendingCard, "Nebula Cloak: " + star.getName()
                        + " ถูกซ่อนจนถึงเทิร์นถัดไป");
            }
        } else if (pendingMode == PendingMode.ENEMY_PLAYER) {
            handlePlayerClick(owner);
        }
    }

    private void handlePlayerClick(Player target) {
        if (pendingCard == null || pendingMode != PendingMode.ENEMY_PLAYER) {
            return;
        }

        if (target == myPlayer) {
            setStatus("เลือกผู้เล่นคู่แข่งเท่านั้น");
            return;
        }

        if (pendingCard.getName().equals("Worm Hole")) {
            if (target.getHand().isEmpty()) {
                setStatus("Worm Hole: ผู้เล่นนี้ไม่มีการ์ดในมือ");
                return;
            }

            pendingTarget = target;
            pendingMode = PendingMode.COST_CARD;
            boardPanel.clearSelectable();
            updateMyHand(myPlayer.getHand());
            setStatus("Worm Hole: เลือกการ์ดในมือ 1 ใบเพื่อแลกกับ "
                    + target.getUsername());
            return;
        }

        if (pendingCard.getName().equals("Supernova")) {
            if (sendPlayToServer(pendingCard, target, null, null)) {
                return;
            }

            if (!myPlayer.useActionPoint(1)) {
                cancelPending();
                setStatus("AP ไม่พอสำหรับเล่นการ์ด");
                return;
            }

            pendingCard.playEffect(myPlayer, target, null, gameState);
            finishPlay(pendingCard, "Supernova: ดาวทุกดวงของ "
                    + target.getUsername() + " เสีย HP 1");
        }
    }

    private void resolveCostCard(Card cost) {
        if (pendingCard == null || cost == null) {
            return;
        }

        if (cost == pendingCard) {
            cancelPending();
            setStatus("ยกเลิกการเล่นการ์ดแล้ว");
            return;
        }

        if (!myPlayer.getHand().contains(cost)) {
            return;
        }

        if (client != null) {
            sendPlayToServer(pendingCard, pendingTarget, pendingStar, cost);
            return;
        }

        if (pendingCard.getName().equals("Pulsar")) {
            if (!myPlayer.useActionPoint(1)) {
                cancelPending();
                setStatus("AP ไม่พอสำหรับเล่นการ์ด");
                return;
            }

            String targetName = pendingStar.getName();
            ((Pulsar) pendingCard).playEffect(myPlayer, pendingTarget, pendingStar, cost, gameState);
            finishPlay(pendingCard, "Pulsar: ทำลาย " + targetName
                    + " โดยทิ้ง " + cost.getName());
        } else if (pendingCard.getName().equals("Worm Hole")) {
            if (!myPlayer.useActionPoint(1)) {
                cancelPending();
                setStatus("AP ไม่พอสำหรับเล่นการ์ด");
                return;
            }

            String targetName = pendingTarget.getUsername();
            ((WormHole) pendingCard).playEffectWithCard(myPlayer, pendingTarget, cost, gameState);
            finishPlay(pendingCard, "Worm Hole: แลก " + cost.getName()
                    + " กับการ์ดสุ่มจากมือ " + targetName);
        }
    }

    private void placeStarCard(StarCard star) {
        if (sendPlayToServer(star, null, null, null)) {
            return;
        }

        int pos = star.getOrbitPosition();

        if (myPlayer.hasStarOnField(star.getName())) {
            setStatus("วาง " + star.getName() + " ไม่ได้: มี " + star.getName()
                    + " บนสนามแล้ว (แต่ละดวงวางได้ 1 ใบ)");
            return;
        }

        if (pos < 0 || pos >= myPlayer.getField().length
                || myPlayer.getField()[pos] != null) {
            pos = myPlayer.findFirstEmptySlot();

            if (pos < 0) {
                setStatus("วาง " + star.getName() + " ไม่ได้: กระดานเต็ม 9 ช่อง");
                return;
            }

            star.setOrbitPosition(pos);
        }

        if (!myPlayer.useActionPoint(1)) {
            setStatus("AP ไม่พอสำหรับวางดาว");
            return;
        }

        star.playEffect(myPlayer, myPlayer, null, gameState);
        myPlayer.removeCardFromHand(star);

        refreshAll();
        setStatus("วาง " + star.getName() + " ที่ช่อง " + pos
                + " — AP เหลือ " + myPlayer.getActionPoints());
        notifyCardPlayed(star);
        checkWin();
    }

    private void playNoTarget(Card card, String message) {
        if (sendPlayToServer(card, null, null, null)) {
            return;
        }

        if (!myPlayer.useActionPoint(1)) {
            setStatus("AP ไม่พอสำหรับเล่นการ์ด");
            return;
        }

        card.playEffect(myPlayer, null, null, gameState);
        finishPlay(card, message);
    }

    private boolean sendPlayToServer(Card card, Player target, StarCard targetStar, Card cost) {
        if (client == null) {
            return false;
        }

        if (card == null || card.getId() == null) {
            setStatus("ไม่พบรหัสการ์ดสำหรับส่งไปเซิร์ฟเวอร์");
            return true;
        }

        int targetIndex = -1;

        if (target != null && players != null) {
            targetIndex = players.indexOf(target);
        }

        int targetSlot = targetStar == null ? -1 : targetStar.getOrbitPosition();
        String costId = cost == null || cost.getId() == null ? "" : cost.getId();
        String command = "PLAY";

        if (card instanceof SpecialCard) {
            String category = ((SpecialCard) card).getCategory();

            if (SpecialCard.CATEGORY_ATTACK.equals(category)) {
                command = "ATTACK";
            } else if (SpecialCard.CATEGORY_DEFENSE.equals(category)) {
                command = "DEFEND";
            }
        }

        client.sendAction(command + "#" + card.getId() + "#" + targetIndex + "#"
                + targetSlot + "#" + costId);
        cancelPending();
        setStatus("ส่งคำสั่งเล่นการ์ดไปยังเซิร์ฟเวอร์แล้ว...");
        return true;
    }

    private void finishPlay(Card card, String message) {
        myPlayer.removeCardFromHand(card);

        if (!(card instanceof BlackHole)) {
            myPlayer.addToDropZone(card);
        }

        boardPanel.playCardToDropZone(card, loadCardImage(card));

        pendingCard = null;
        pendingMode = PendingMode.NONE;
        pendingStar = null;
        pendingTarget = null;
        boardPanel.clearSelectable();

        refreshAll();
        setStatus(message + " — AP เหลือ " + myPlayer.getActionPoints());
        notifyCardPlayed(card);
        checkWin();
    }

    private void notifyCardPlayed(Card card) {
        if (onCardSelected != null) {
            onCardSelected.accept(card);
        }
    }

    private void checkWin() {
        if (myPlayer != null && myPlayer.checkWinCondition()) {
            setStatus("คุณชนะแล้ว! ดาวบนสนามครบ 9 ดวง");
        }
    }

    private void refreshAll() {
        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }

        if (players != null) {
            int topCount = players.size() > 1 ? players.get(1).getHand().size() : 0;
            int leftCount = players.size() > 2 ? players.get(2).getHand().size() : 0;
            int rightCount = players.size() > 3 ? players.get(3).getHand().size() : 0;
            updateOpponentHands(topCount, leftCount, rightCount);
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private Set<StarCard> enemyStars() {
        Set<StarCard> result = new HashSet<>();

        if (players == null) {
            return result;
        }

        for (int i = 1; i < players.size(); i++) {
            Player p = players.get(i);

            for (StarCard star : p.getField()) {
                if (star != null && !p.isCloaked(star)) {
                    result.add(star);
                }
            }
        }

        return result;
    }

    private Set<StarCard> ownStars() {
        Set<StarCard> result = new HashSet<>();

        if (myPlayer == null) {
            return result;
        }

        for (StarCard star : myPlayer.getField()) {
            if (star != null) {
                result.add(star);
            }
        }

        return result;
    }

    private boolean hasTargetableStar(Player player) {
        for (StarCard star : player.getField()) {
            if (star != null && !player.isCloaked(star)) {
                return true;
            }
        }
        return false;
    }

    private int otherHandCardCount(Card excluded) {
        if (myPlayer == null) {
            return 0;
        }

        int count = 0;

        for (Card card : myPlayer.getHand()) {
            if (card != excluded) {
                count++;
            }
        }

        return count;
    }

    private Player findOwner(StarCard star) {
        if (players == null) {
            return null;
        }

        for (Player p : players) {
            for (StarCard fieldStar : p.getField()) {
                if (fieldStar == star) {
                    return p;
                }
            }
        }

        return null;
    }

    private void onEndTurn(ActionEvent e) {
        endTurn();
    }

    public void endTurn() {
        if (myPlayer == null) {
            return;
        }

        cancelPending();

        if (client != null) {
            if (gameState != null && gameState.getCurrentPlayer() != myPlayer) {
                setStatus("ยังไม่ถึงเทิร์นของคุณ");
                return;
            }

            client.sendAction("END_TURN");
            setStatus("ส่งคำสั่งจบเทิร์นแล้ว กำลังรอเซิร์ฟเวอร์...");
            return;
        }

        myPlayer.resetActionPoints();
        myPlayer.clearExpiredBuffs();

        refreshAll();
        setStatus("เทิร์นใหม่: ได้ 2 AP — คลุม Nebula ของคุณหมดอายุแล้ว");
    }

    private void setStatus(String text) {
        System.out.println("[UI] " + text);

        if (statusLabel != null) {
            statusLabel.setText(text);
        }
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
            leftHandPanel.add(CardButton.back());
        }

        for (int i = 0; i < rightCount; i++) {
            rightHandPanel.add(CardButton.back());
        }

        topHandPanel.revalidate();
        topHandPanel.repaint();
        leftHandPanel.revalidate();
        leftHandPanel.repaint();
        rightHandPanel.revalidate();
        rightHandPanel.repaint();
    }

    public void updateBoardState(String state) {
        System.out.println("[UI] อัปเดตกระดานด้วยข้อมูล: " + state);

        if (state == null || state.isEmpty()) {
            return;
        }

        if (state.startsWith("SYSTEM#")) {
            setStatus(state.substring("SYSTEM#".length()).replace('_', ' '));
            return;
        }

        if (state.startsWith("ERR#")) {
            setStatus("เซิร์ฟเวอร์: " + state.substring("ERR#".length()).replace('#', ' '));
            return;
        }

        if (state.equals("HAND") || state.startsWith("HAND#")) {
            applyHandSnapshot(state);
            return;
        }

        if (!state.startsWith("UPDATE_BOARD#")) {
            return;
        }

        applyBoardSnapshot(state);
    }

    private void applyHandSnapshot(String message) {
        if (myPlayer == null) {
            return;
        }

        List<Card> newHand = new ArrayList<>();
        String body = message.startsWith("HAND#")
                ? message.substring("HAND#".length()) : "";

        if (!body.isEmpty()) {
            for (String record : body.split("#", -1)) {
                String[] parts = record.split(",", -1);

                if (parts.length < 5) {
                    continue;
                }

                Card card = CardFactory.create(parts[0], parts[1], parts[2],
                        parseInt(parts[3], 0), parseInt(parts[4], -1));

                if (card != null) {
                    newHand.add(card);
                }
            }
        }

        myPlayer.getHand().clear();
        myPlayer.getHand().addAll(newHand);
        updateMyHand(myPlayer.getHand());
    }

    private void applyBoardSnapshot(String snapshot) {
        String[] tokens = snapshot.split("#");
        int turn = -1;
        int phase = -1;
        int winnerIndex = -1;
        boolean blackHoleUsed = false;
        boolean gameOver = false;
        List<String> playerRecords = new ArrayList<>();
        List<String> fieldRecords = new ArrayList<>();
        List<String> dropRecords = new ArrayList<>();

        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];

            if (token.startsWith("TURN:")) {
                turn = parseInt(token.substring("TURN:".length()), -1);
            } else if (token.startsWith("PHASE:")) {
                phase = parseInt(token.substring("PHASE:".length()), -1);
            } else if (token.startsWith("BH:")) {
                blackHoleUsed = "1".equals(token.substring("BH:".length()));
            } else if (token.startsWith("OVER:")) {
                gameOver = "1".equals(token.substring("OVER:".length()));
            } else if (token.startsWith("WINNER:")) {
                winnerIndex = parseInt(token.substring("WINNER:".length()), -1);
            } else if (token.startsWith("P:")) {
                playerRecords.add(token.substring("P:".length()));
            } else if (token.startsWith("F:")) {
                fieldRecords.add(token.substring("F:".length()));
            } else if (token.startsWith("D:")) {
                dropRecords.add(token.substring("D:".length()));
            }
        }

        List<Player> targetPlayers = players;

        if ((targetPlayers == null || targetPlayers.isEmpty()) && gameState != null) {
            targetPlayers = gameState.getPlayers();
        }

        if (targetPlayers == null) {
            return;
        }

        cancelPending();

        int[] oldDropCounts = new int[targetPlayers.size()];

        for (int i = 0; i < targetPlayers.size(); i++) {
            Player player = targetPlayers.get(i);

            if (player != null) {
                oldDropCounts[i] = player.getDropZone().size();
                player.clearField();
                player.getDropZone().clear();
            }
        }

        int[] handCounts = new int[targetPlayers.size()];

        for (String record : playerRecords) {
            String[] parts = record.split(",", -1);

            if (parts.length < 6) {
                continue;
            }

            int index = parseInt(parts[0], -1);

            if (index < 0 || index >= targetPlayers.size()) {
                continue;
            }

            Player player = targetPlayers.get(index);

            if (player == null) {
                continue;
            }

            player.setActionPoints(parseInt(parts[2], player.getActionPoints()));
            handCounts[index] = Math.max(0, parseInt(parts[3], player.getHand().size()));
        }

        for (String record : fieldRecords) {
            String[] parts = record.split(",", -1);

            if (parts.length < 7) {
                continue;
            }

            int playerIndex = parseInt(parts[0], -1);
            int slot = parseInt(parts[1], -1);

            if (playerIndex < 0 || playerIndex >= targetPlayers.size()) {
                continue;
            }

            Player player = targetPlayers.get(playerIndex);

            if (player == null || slot < 0 || slot >= player.getField().length) {
                continue;
            }

            int maxHp = Math.max(1, parseInt(parts[4], 1));
            int hp = Math.max(0, Math.min(maxHp, parseInt(parts[3], maxHp)));
            String cardName = parts[2];
            StarCard star = new StarCard("NET-" + playerIndex + "-" + slot,
                    cardName, maxHp, ImgPath.get(cardName), slot);

            if (hp < maxHp) {
                star.takeDamage(maxHp - hp);
            }

            if (!player.placeStar(star)) {
                continue;
            }

            if ("1".equals(parts[5])) {
                player.addShield(star);
            }

            if ("1".equals(parts[6])) {
                player.addCloak(star);
            }
        }

        for (String record : dropRecords) {
            String[] parts = record.split(",", -1);

            if (parts.length < 6) {
                continue;
            }

            int playerIndex = parseInt(parts[0], -1);

            if (playerIndex < 0 || playerIndex >= targetPlayers.size()) {
                continue;
            }

            Player player = targetPlayers.get(playerIndex);

            if (player == null) {
                continue;
            }

            Card card = CardFactory.create(parts[1], parts[2], parts[3],
                    parseInt(parts[4], 0), parseInt(parts[5], -1));

            if (card != null) {
                player.getDropZone().add(card);
            }
        }

        for (int i = 0; i < targetPlayers.size(); i++) {
            Player player = targetPlayers.get(i);

            if (player == null) {
                continue;
            }

            int newDropCount = player.getDropZone().size();

            if (newDropCount > oldDropCounts[i] && newDropCount > 0) {
                Card lastDrop = player.getDropZone().get(newDropCount - 1);
                boardPanel.playCardToDropZone(lastDrop, loadCardImage(lastDrop));
            }
        }

        if (gameState != null) {
            if (turn >= 0) {
                gameState.setCurrentPlayerIndex(turn);
            }

            if (phase >= 0) {
                gameState.setCurrentPhase(phase);
            }

            gameState.setBlackHoleUsed(blackHoleUsed);
            gameState.setGameOver(gameOver);

            if (winnerIndex >= 0 && winnerIndex < targetPlayers.size()) {
                gameState.setWinner(targetPlayers.get(winnerIndex));
            }

            if (!gameOver && gameState.getCurrentPlayer() != null) {
                Player current = gameState.getCurrentPlayer();

                if (current == myPlayer) {
                    setStatus("ถึงเทิร์นของคุณ — AP " + current.getActionPoints());
                } else {
                    setStatus("รอเทิร์นของ " + current.getUsername());
                }
            }
        }

        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }

        if (targetPlayers.size() >= 2) {
            int topCount = handCounts[1];
            int leftCount = targetPlayers.size() > 2 ? handCounts[2] : 0;
            int rightCount = targetPlayers.size() > 3 ? handCounts[3] : 0;
            updateOpponentHands(topCount, leftCount, rightCount);
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
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

        StarCard damagedMars = star("Mars", 4, 4);
        damagedMars.takeDamage(3);
        me.placeStar(damagedMars);

        me.placeStar(star("Jupiter", 4, 2));

        north.placeStar(star("Venus", 2, 1));
        north.placeStar(star("Saturn", 4, 5));

        west.placeStar(star("Mercury", 1, 0));
        west.placeStar(star("Uranus", 3, 6));

        east.placeStar(star("Neptune", 3, 7));
        east.placeStar(star("The Sun", 5, 8));

        me.addToDropZone(star("The Sun", 5, 8));

        me.addCardToHand(star("Neptune", 3, 0));
        me.addCardToHand(star("Mercury", 1, 1));
        me.addCardToHand(new MeteorStrike("DEMO-METEOR", ImgPath.get("Meteor Strike")));
        me.addCardToHand(new Supernova("DEMO-SUPERNOVA", ImgPath.get("Super Nova")));
        me.addCardToHand(new Pulsar("DEMO-PULSAR", ImgPath.get("Pulsar")));
        me.addCardToHand(new ForceField("DEMO-FORCE", ImgPath.get("Force Field")));
        me.addCardToHand(new NebulaCloak("DEMO-CLOAK", ImgPath.get("Nebula Cloak")));
        me.addCardToHand(new StellarRegeneration("DEMO-REGEN", ImgPath.get("Stellar Regeneration")));
        me.addCardToHand(new SpaceProbe("DEMO-PROBE", ImgPath.get("Space Probe")));
        me.addCardToHand(new WormHole("DEMO-WORM", ImgPath.get("Worm Hole")));
        me.addCardToHand(new BigBang("DEMO-BIGBANG", ImgPath.get("Big Bang")));
        me.addCardToHand(new BlackHole("DEMO-BLACKHOLE", ImgPath.get("Black Hole")));

        north.addCardToHand(star("Venus", 2, 0));
        north.addCardToHand(star("Earth", 5, 1));
        north.addCardToHand(new MeteorStrike("NOVA-METEOR", ImgPath.get("Meteor Strike")));
        north.addCardToHand(new SpaceProbe("NOVA-PROBE", ImgPath.get("Space Probe")));
        north.addCardToHand(new ForceField("NOVA-FORCE", ImgPath.get("Force Field")));

        west.addCardToHand(star("Mercury", 1, 0));
        west.addCardToHand(star("Mars", 4, 1));
        west.addCardToHand(new Supernova("ORBIT-SUPERNOVA", ImgPath.get("Super Nova")));
        west.addCardToHand(new NebulaCloak("ORBIT-CLOAK", ImgPath.get("Nebula Cloak")));
        west.addCardToHand(new StellarRegeneration("ORBIT-REGEN", ImgPath.get("Stellar Regeneration")));

        east.addCardToHand(star("Neptune", 3, 0));
        east.addCardToHand(star("Jupiter", 4, 1));
        east.addCardToHand(new Pulsar("COMET-PULSAR", ImgPath.get("Pulsar")));
        east.addCardToHand(new WormHole("COMET-WORM", ImgPath.get("Worm Hole")));
        east.addCardToHand(new BigBang("COMET-BIGBANG", ImgPath.get("Big Bang")));

        List<Player> players = new ArrayList<>();
        players.add(me);
        players.add(north);
        players.add(west);
        players.add(east);

        setPlayers(players);
        setMyPlayer(me);
        gameState = new GameState(players);

        updateOpponentHands(5, 5, 5);
        setStatus("เดโมพร้อมเล่น — เลือกการ์ดในมือ (AP 2, กดจบเทิร์นเพื่อรีเซ็ต)");
    }

    private static class SideHandPanel extends JPanel {

        SideHandPanel() {
            setOpaque(false);
            setLayout(null);
        }

        @Override
        public void doLayout() {
            int count = getComponentCount();

            if (count == 0) {
                return;
            }

            int gap = 6;
            int cardW = Math.min(110, Math.max(52, getWidth() - gap * 2));
            int cardH = (int) Math.round(cardW * 1.4);
            int totalH = count * cardH + (count - 1) * gap;

            if (totalH > getHeight()) {
                cardH = Math.max(1, (getHeight() - (count - 1) * gap) / count);
                cardW = (int) Math.round(cardH / 1.4);
                totalH = count * cardH + (count - 1) * gap;
            }

            int startX = (getWidth() - cardW) / 2;
            int startY = Math.max(0, (getHeight() - totalH) / 2);
            int y = startY;

            for (Component card : getComponents()) {
                card.setBounds(startX, y, cardW, cardH);
                y += cardH + gap;
            }
        }
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
