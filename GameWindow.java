import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.*;

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
    private JLabel deckLabel;
    private JButton endTurnButton;

    private Player myPlayer;
    private List<Player> players;
    private final Map<Player, Integer> deckCounts = new HashMap<>();
    private final Map<Player, Integer> handCounts = new HashMap<>();
    private boolean gameOverAnnounced;
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

        deckLabel = new JLabel("เด็คคงเหลือ: -");
        deckLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        deckLabel.setForeground(new Color(120, 30, 30));
        bar.add(deckLabel, BorderLayout.WEST);

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
        updateDeckLabel();
        updateMyHand(player == null ? new ArrayList<>() : player.getHand());
    }

    private void updateDeckLabel() {
        if (deckLabel == null) {
            return;
        }

        Integer remaining = myPlayer == null ? null : deckCounts.get(myPlayer);
        deckLabel.setText(remaining == null
                ? "เด็คคงเหลือ: -"
                : "เด็คคงเหลือ: " + remaining + " ใบ");
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
        updateDeckLabel();
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

        if (gameState.isGameOver()) {
            setStatus("เกมจบแล้ว");
            return;
        }

        if (!myPlayer.isActive()) {
            setStatus("คุณแพ้แล้ว — ออกจากเกม (Deck Out)");
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
            sendPlayToServer(card, null, null, null);
            return;
        }

        if (!(card instanceof SpecialCard)) {
            setStatus("ไม่รู้จักชนิดการ์ดนี้: " + card.getName());
            return;
        }

        SpecialCard special = (SpecialCard) card;
        String name = special.getName();
        String targetType = special.getTargetType();
        String prompt = special.getPromptMessage();

        if (name.equals("Pulsar")) {
            if (myPlayer.isPulsarUsedThisTurn()) {
                setStatus("Pulsar ใช้ได้ 1 ครั้งต่อเทิร์นเท่านั้น");
                return;
            }
            if (otherHandCardCount(card) == 0) {
                setStatus("Pulsar: ต้องมีการ์ดอีก 1 ใบในมือเพื่อทิ้งเป็นค่าการใช้");
                return;
            }
        } else if (name.equals("Black Hole")) {
            if (gameState.isBlackHoleUsed()) {
                setStatus("Black Hole ถูกใช้ไปแล้วในเกมนี้");
                return;
            }
        } else if (name.equals("Worm Hole")) {
            if (otherHandCardCount(card) == 0) {
                setStatus("Worm Hole: ต้องมีการ์ดอีก 1 ใบในมือเพื่อใช้แลก");
                return;
            }
        }

        if (SpecialCard.TARGET_ENEMY_STAR.equals(targetType)) {
            Set<StarCard> targets = enemyStars();
            if (targets.isEmpty()) {
                setStatus(name + ": ไม่มีดาวคู่แข่งที่เลือกได้");
                return;
            }
            beginTargeting(card, PendingMode.ENEMY_STAR, targets, null, prompt);
        } else if (SpecialCard.TARGET_ALLY_STAR.equals(targetType)) {
            Set<StarCard> targets = ownStars();
            if (targets.isEmpty()) {
                setStatus(name + ": คุณยังไม่มีดาวบนสนาม");
                return;
            }
            beginTargeting(card, PendingMode.ALLY_STAR, targets, null, prompt);
        } else if (SpecialCard.TARGET_ENEMY_PLAYER.equals(targetType)) {
            Set<Player> targets = new HashSet<>();
            for (Player player : players) {
                if (player != myPlayer && player.isActive()) {
                    if (name.equals("Supernova")) {
                        if (hasTargetableStar(player)) {
                            targets.add(player);
                        }
                    } else if (name.equals("Worm Hole")) {
                        if (handCountOf(player) > 0) {
                            targets.add(player);
                        }
                    } else {
                        targets.add(player);
                    }
                }
            }
            if (targets.isEmpty()) {
                setStatus(name + ": ไม่มีผู้เล่นเป้าหมายที่เลือกได้");
                return;
            }
            beginTargeting(card, PendingMode.ENEMY_PLAYER, null, targets, prompt);
        } else if (SpecialCard.TARGET_NONE.equals(targetType)) {
            sendPlayToServer(card, null, null, null);
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

            if (!owner.isActive()) {
                setStatus("ผู้เล่นนี้หลุดออกจากเกมแล้ว");
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

            sendPlayToServer(pendingCard, owner, star, null);
        } else if (pendingMode == PendingMode.ALLY_STAR) {
            if (owner != myPlayer) {
                setStatus("เลือกดาวของตนเองเท่านั้น");
                return;
            }

            sendPlayToServer(pendingCard, myPlayer, star, null);
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

        if (!target.isActive()) {
            setStatus("ผู้เล่นนี้หลุดออกจากเกมแล้ว");
            return;
        }

        if (pendingCard.getName().equals("Worm Hole")) {
            if (handCountOf(target) <= 0) {
                setStatus("Worm Hole: ผู้เล่นนี้ไม่มีการ์ดในมือ");
                return;
            }

            pendingTarget = target;
            pendingMode = PendingMode.COST_CARD;
            boardPanel.clearSelectable();
            updateMyHand(myPlayer.getHand());
            setStatus("Worm Hole: เลือกการ์ดในมือ 1 ใบเพื่อแลกกับ " + target.getUsername());
            return;
        }

        if (pendingCard.getName().equals("Supernova")) {
            sendPlayToServer(pendingCard, target, null, null);
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

        sendPlayToServer(pendingCard, pendingTarget, pendingStar, cost);
    }

    private void sendPlayToServer(Card card, Player target, StarCard targetStar, Card cost) {
        if (client == null) {
            setStatus("ไม่ได้เชื่อมต่อกับเซิร์ฟเวอร์");
            return;
        }

        if (card == null || card.getId() == null) {
            setStatus("ไม่พบรหัสการ์ดสำหรับส่งไปเซิร์ฟเวอร์");
            return;
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

        if (onCardSelected != null) {
            onCardSelected.accept(card);
        }
    }

    private void showGameOver(Player winner) {
        if (gameOverAnnounced) {
            return;
        }

        gameOverAnnounced = true;

        if (endTurnButton != null) {
            endTurnButton.setEnabled(false);
        }

        String message;
        if (winner == null) {
            message = "เกมจบแล้ว — ไม่มีผู้เล่นเหลืออยู่";
        } else if (winner == myPlayer) {
            message = "คุณชนะ!";
        } else {
            message = winner.getUsername() + " ชนะ";
        }

        setStatus(message);
        JOptionPane.showMessageDialog(this, message, "จบเกม", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAll() {
        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }

        if (players != null) {
            int topCount = players.size() > 1 ? handCountOf(players.get(1)) : 0;
            int leftCount = players.size() > 2 ? handCountOf(players.get(2)) : 0;
            int rightCount = players.size() > 3 ? handCountOf(players.get(3)) : 0;
            updateOpponentHands(topCount, leftCount, rightCount);
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private Set<StarCard> enemyStars() {
        Set<StarCard> result = new HashSet<>();
        if (players == null) return result;

        for (Player p : players) {
            if (p == null || p == myPlayer || !p.isActive()) continue;
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
        if (myPlayer == null) return result;

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
        if (myPlayer == null) return 0;
        int count = 0;
        for (Card card : myPlayer.getHand()) {
            if (card != excluded) count++;
        }
        return count;
    }

    private int handCountOf(Player player) {
        if (player == null || !player.isActive()) return 0;
        Integer count = handCounts.get(player);
        return count != null ? count : player.getHand().size();
    }

    private Player findOwner(StarCard star) {
        if (players == null) return null;
        for (Player p : players) {
            for (StarCard fieldStar : p.getField()) {
                if (fieldStar == star) return p;
            }
        }
        return null;
    }

    private void onEndTurn(ActionEvent e) {
        endTurn();
    }

    public void endTurn() {
        if (myPlayer == null) return;

        if (gameState != null && gameState.isGameOver()) {
            setStatus("เกมจบแล้ว");
            return;
        }

        if (!myPlayer.isActive()) {
            setStatus("คุณแพ้แล้ว — ออกจากเกม (Deck Out)");
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
        }
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

        if (state == null || state.isEmpty()) return;

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

        if (!state.startsWith("UPDATE_BOARD#")) return;

        applyBoardSnapshot(state);
    }

    private void applyHandSnapshot(String message) {
        if (myPlayer == null) return;

        List<Card> newHand = new ArrayList<>();
        String body = message.startsWith("HAND#") ? message.substring("HAND#".length()) : "";

        if (!body.isEmpty()) {
            for (String record : body.split("#", -1)) {
                String[] parts = record.split(",", -1);
                if (parts.length < 5) continue;

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

        if (targetPlayers == null) return;

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

        int[] handCountsByIndex = new int[targetPlayers.size()];
        int[] deckCountsByIndex = new int[targetPlayers.size()];

        for (int i = 0; i < deckCountsByIndex.length; i++) {
            deckCountsByIndex[i] = -1;
        }

        for (String record : playerRecords) {
            String[] parts = record.split(",", -1);
            if (parts.length < 6) continue;

            int index = parseInt(parts[0], -1);
            if (index < 0 || index >= targetPlayers.size()) continue;

            Player player = targetPlayers.get(index);
            if (player == null) continue;

            player.setActionPoints(parseInt(parts[2], player.getActionPoints()));
            player.setActive(parts.length < 7 || !"0".equals(parts[6]));
            handCountsByIndex[index] = Math.max(0, parseInt(parts[3], player.getHand().size()));
            deckCountsByIndex[index] = Math.max(0, parseInt(parts[4], -1));
        }

        deckCounts.clear();
        handCounts.clear();

        for (int i = 0; i < deckCountsByIndex.length; i++) {
            if (targetPlayers.get(i) == null) continue;

            handCounts.put(targetPlayers.get(i), handCountsByIndex[i]);
            if (deckCountsByIndex[i] >= 0) {
                deckCounts.put(targetPlayers.get(i), deckCountsByIndex[i]);
            }
        }

        boardPanel.setDeckCounts(deckCounts);
        updateDeckLabel();

        for (String record : fieldRecords) {
            String[] parts = record.split(",", -1);
            if (parts.length < 7) continue;

            int playerIndex = parseInt(parts[0], -1);
            int slot = parseInt(parts[1], -1);

            if (playerIndex < 0 || playerIndex >= targetPlayers.size()) continue;

            Player player = targetPlayers.get(playerIndex);
            if (player == null || slot < 0 || slot >= player.getField().length) continue;

            int maxHp = Math.max(1, parseInt(parts[4], 1));
            int hp = Math.max(0, Math.min(maxHp, parseInt(parts[3], maxHp)));
            String cardName = parts[2];
            StarCard star = new StarCard("NET-" + playerIndex + "-" + slot,
                    cardName, maxHp, ImgPath.get(cardName), slot);

            if (hp < maxHp) {
                star.takeDamage(maxHp - hp);
            }

            if (!player.placeStar(star)) continue;

            if ("1".equals(parts[5])) player.addShield(star);
            if ("1".equals(parts[6])) player.addCloak(star);
        }

        for (String record : dropRecords) {
            String[] parts = record.split(",", -1);
            if (parts.length < 6) continue;

            int playerIndex = parseInt(parts[0], -1);
            if (playerIndex < 0 || playerIndex >= targetPlayers.size()) continue;

            Player player = targetPlayers.get(playerIndex);
            if (player == null) continue;

            Card card = CardFactory.create(parts[1], parts[2], parts[3],
                    parseInt(parts[4], 0), parseInt(parts[5], -1));

            if (card != null) {
                player.getDropZone().add(card);
            }
        }

        for (int i = 0; i < targetPlayers.size(); i++) {
            Player player = targetPlayers.get(i);
            if (player == null) continue;

            int newDropCount = player.getDropZone().size();
            if (newDropCount > oldDropCounts[i] && newDropCount > 0) {
                Card lastDrop = player.getDropZone().get(newDropCount - 1);
                boardPanel.playCardToDropZone(lastDrop, loadCardImage(lastDrop));
            }
        }

        if (gameState != null) {
            if (turn >= 0) gameState.setCurrentPlayerIndex(turn);
            if (phase >= 0) gameState.setCurrentPhase(phase);

            gameState.setBlackHoleUsed(blackHoleUsed);
            gameState.setGameOver(gameOver);

            if (winnerIndex >= 0 && winnerIndex < targetPlayers.size()) {
                gameState.setWinner(targetPlayers.get(winnerIndex));
            }

            if (gameOver) {
                showGameOver(gameState.getWinner());
            } else if (gameState.getCurrentPlayer() != null) {
                Player current = gameState.getCurrentPlayer();
                String phaseText = phaseName(gameState.getCurrentPhase());

                if (current == myPlayer) {
                    setStatus("ถึงเทิร์นของคุณ — AP " + current.getActionPoints()
                            + " (เฟส " + phaseText + ")");
                } else {
                    setStatus("รอเทิร์นของ " + current.getUsername()
                            + " (เฟส " + phaseText + ")");
                }
            }

            if (endTurnButton != null) {
                boolean canEndTurn = !gameOver && myPlayer != null && myPlayer.isActive()
                        && gameState.getCurrentPlayer() == myPlayer;
                endTurnButton.setEnabled(canEndTurn);
            }

            if (!gameOver && myPlayer != null && !myPlayer.isActive()) {
                if (endTurnButton != null) {
                    endTurnButton.setEnabled(false);
                }
                setStatus("คุณแพ้แล้ว — เด็คหมด (Deck Out)");
            }
        }

        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }

        if (targetPlayers.size() >= 2) {
            int topCount = handCountsByIndex[1];
            int leftCount = targetPlayers.size() > 2 ? handCountsByIndex[2] : 0;
            int rightCount = targetPlayers.size() > 3 ? handCountsByIndex[3] : 0;
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

    private static String phaseName(int phase) {
        if (phase == 1) return "จั่ว";
        if (phase == 3) return "จบ";
        return "เล่น";
    }

    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    private Image loadCardImage(Card card) {
        if (card == null) return null;
        String path = ImgPath.get(card.getName());
        if (path == null) return null;

        Image img = new ImageIcon(path).getImage();
        return img.getWidth(null) <= 0 ? null : img;
    }

    private static class SideHandPanel extends JPanel {
        SideHandPanel() {
            setOpaque(false);
            setLayout(null);
        }

        @Override
        public void doLayout() {
            int count = getComponentCount();
            if (count == 0) return;

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
}
