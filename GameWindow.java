import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;

public class GameWindow extends JFrame {

    private BoardPanel boardPanel;
    private JPanel myHandPanel;
    private JPanel topHandPanel;
    private JPanel leftHandPanel;
    private JPanel rightHandPanel;
    private StatusBarPanel statusBarPanel;

    private Player myPlayer;
    private List<Player> players;
    private final Map<Player, Integer> deckCounts = new HashMap<>();
    private final Map<Player, Integer> handCounts = new HashMap<>();
    private boolean gameOverAnnounced;
    private GameState gameState;
    private GameClient client;
    private Consumer<Card> onCardSelected;


    private final TargetingManager targetingManager = new TargetingManager();

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
        boardPanel.setOnStarClicked(star -> this.handleStarClick(star));
        boardPanel.setOnPlayerClicked(player -> this.handlePlayerClick(player));
        add(boardPanel, BorderLayout.CENTER);

        myHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        myHandPanel.setOpaque(false);
        myHandPanel.setPreferredSize(new Dimension(0, 150));
        myHandPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(200, 70, 70)));


        statusBarPanel = new StatusBarPanel();
        statusBarPanel.addEndTurnListener(e -> this.onEndTurn(e));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(statusBarPanel, BorderLayout.NORTH);
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
    }

    private JPanel createSidePanel() {
        return new SideHandPanel();
    }

        public void setMyPlayer(Player player) {
            this.myPlayer = player;
            if (boardPanel != null) {
                boardPanel.setLocalPlayer(player);
            }

            updateDeckLabel();
            updateMyHand(player == null ? new ArrayList<>() : player.getHand());
    }

    private void updateDeckLabel() {
        if (statusBarPanel == null) return;
        Integer remaining = myPlayer == null ? null : deckCounts.get(myPlayer);
        statusBarPanel.setDeckText(remaining == null ? "เด็คคงเหลือ: -" : "เด็คคงเหลือ: " + remaining + " ใบ");
    }

    public Player getMyPlayer() {
        return myPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getStatusText() {
        return statusBarPanel != null ? statusBarPanel.getStatusText() : "";
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
                cardBtn.setSelected(card == targetingManager.getPendingCard());
                cardBtn.setOnClick(() -> handleHandCardClick(card));
                myHandPanel.add(cardBtn);
            }
        }

        myHandPanel.revalidate();
        myHandPanel.repaint();
    }

    private void handleHandCardClick(Card card) {
        if (targetingManager.getPendingMode() == TargetingManager.PendingMode.COST_CARD) {
            resolveCostCard(card);
            return;
        }

        if (targetingManager.getPendingCard() == card) {
            cancelPending();
            setStatus("ยกเลิกการเลือกการ์ดแล้ว");
            return;
        }

        if (targetingManager.getPendingCard() != null) {
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
        String error = CardActionHandler.validateSpecialCard(special, myPlayer, gameState, otherHandCardCount(card));
        if (error != null) {
            setStatus(error);
            return;
        }

        String targetType = special.getTargetType();
        String prompt = special.getPromptMessage();

        if (SpecialCard.TARGET_ENEMY_STAR.equals(targetType)) {
            Set<StarCard> targets = CardActionHandler.getTargetableEnemyStars(players, myPlayer);
            if (targets.isEmpty()) {
                setStatus(special.getName() + ": ไม่มีดาวคู่แข่งที่เลือกได้");
                return;
            }
            beginTargeting(card, TargetingManager.PendingMode.ENEMY_STAR, targets, null, prompt);
        } else if (SpecialCard.TARGET_ALLY_STAR.equals(targetType)) {
            Set<StarCard> targets = CardActionHandler.getTargetableOwnStars(myPlayer);
            if (targets.isEmpty()) {
                setStatus(special.getName() + ": คุณยังไม่มีดาวบนสนาม");
                return;
            }
            beginTargeting(card, TargetingManager.PendingMode.ALLY_STAR, targets, null, prompt);
        } else if (SpecialCard.TARGET_ENEMY_PLAYER.equals(targetType)) {
            Set<Player> targets = new HashSet<>();
            for (Player player : players) {
                if (player != myPlayer && player.isActive()) {
                    if (special.getName().equals("Supernova")) {
                        if (hasTargetableStar(player)) targets.add(player);
                    } else if (special.getName().equals("Worm Hole")) {
                        if (handCountOf(player) > 0) targets.add(player);
                    } else {
                        targets.add(player);
                    }
                }
            }
            if (targets.isEmpty()) {
                setStatus(special.getName() + ": ไม่มีผู้เล่นเป้าหมายที่เลือกได้");
                return;
            }
            beginTargeting(card, TargetingManager.PendingMode.ENEMY_PLAYER, null, targets, prompt);
        } else if (SpecialCard.TARGET_NONE.equals(targetType)) {
            sendPlayToServer(card, null, null, null);
        } else {
            setStatus("ยังไม่ได้เชื่อม Logic ของการ์ด: " + special.getName());
        }
    }

    private void beginTargeting(Card card, TargetingManager.PendingMode mode, Set<StarCard> stars,
                                Set<Player> targetPlayers, String message) {
        targetingManager.begin(card, mode);
        boardPanel.setSelectable(stars, targetPlayers);
        updateMyHand(myPlayer.getHand());
        setStatus(message);
    }

    public void cancelPending() {
        targetingManager.reset();
        boardPanel.clearSelectable();

        if (myPlayer != null) {
            updateMyHand(myPlayer.getHand());
        }
    }

    private void handleStarClick(StarCard star) {
        if (targetingManager.getPendingCard() == null) return;

        Player owner = findOwner(star);
        if (owner == null) return;

        TargetingManager.PendingMode mode = targetingManager.getPendingMode();

        if (mode == TargetingManager.PendingMode.ENEMY_STAR) {
            if (owner == myPlayer) {
                setStatus("เลือกดาวของคู่แข่ง ไม่ใช่ดาวของตนเอง");
                return;
            }

            if (!owner.isActive()) {
                setStatus("ผู้เล่นนี้หลุดออกจากเกมแล้ว");
                return;
            }

            if (targetingManager.getPendingCard().getName().equals("Pulsar")) {
                if (otherHandCardCount(targetingManager.getPendingCard()) == 0) {
                    cancelPending();
                    setStatus("Pulsar: ไม่มีการ์ดใบอื่นให้ทิ้งแล้ว");
                    return;
                }

                targetingManager.setPendingStar(star);
                targetingManager.setPendingTarget(owner);
                targetingManager.setPendingMode(TargetingManager.PendingMode.COST_CARD);
                boardPanel.clearSelectable();
                updateMyHand(myPlayer.getHand());
                setStatus("Pulsar: เลือกการ์ดอีก 1 ใบในมือเพื่อทิ้งเป็นค่าการใช้");
                return;
            }

            sendPlayToServer(targetingManager.getPendingCard(), owner, star, null);
        } else if (mode == TargetingManager.PendingMode.ALLY_STAR) {
            if (owner != myPlayer) {
                setStatus("เลือกดาวของตนเองเท่านั้น");
                return;
            }

            sendPlayToServer(targetingManager.getPendingCard(), myPlayer, star, null);
        } else if (mode == TargetingManager.PendingMode.ENEMY_PLAYER) {
            handlePlayerClick(owner);
        }
    }

    private void handlePlayerClick(Player target) {
        if (targetingManager.getPendingCard() == null || targetingManager.getPendingMode() != TargetingManager.PendingMode.ENEMY_PLAYER) {
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

        if (targetingManager.getPendingCard().getName().equals("Worm Hole")) {
            if (handCountOf(target) <= 0) {
                setStatus("Worm Hole: ผู้เล่นนี้ไม่มีการ์ดในมือ");
                return;
            }

            targetingManager.setPendingTarget(target);
            targetingManager.setPendingMode(TargetingManager.PendingMode.COST_CARD);
            boardPanel.clearSelectable();
            updateMyHand(myPlayer.getHand());
            setStatus("Worm Hole: เลือกการ์ดในมือ 1 ใบเพื่อแลกกับ " + target.getUsername());
            return;
        }

        if (targetingManager.getPendingCard().getName().equals("Supernova")) {
            sendPlayToServer(targetingManager.getPendingCard(), target, null, null);
        }
    }

    private void resolveCostCard(Card cost) {
        Card pendingCard = targetingManager.getPendingCard();
        if (pendingCard == null || cost == null) return;

        if (cost == pendingCard) {
            cancelPending();
            setStatus("ยกเลิกการเล่นการ์ดแล้ว");
            return;
        }

        if (!myPlayer.getHand().contains(cost)) return;

        sendPlayToServer(pendingCard, targetingManager.getPendingTarget(), targetingManager.getPendingStar(), cost);
    }

    private void sendPlayToServer(Card card, Player target, StarCard targetStar, Card cost) {
        if (client == null) {
            setStatus("ไม่ได้เชื่อมต่อกับเซิร์ฟเวอร์");
            return;
        }

        String actionCommand = CardActionHandler.buildServerCommand(card, target, targetStar, cost, players);
        if (actionCommand == null) {
            setStatus("ไม่พบรหัสการ์ดสำหรับส่งไปเซิร์ฟเวอร์");
            return;
        }

        client.sendAction(actionCommand);
        cancelPending();
        setStatus("ส่งคำสั่งเล่นการ์ดไปยังเซิร์ฟเวอร์แล้ว...");

        if (onCardSelected != null) {
            onCardSelected.accept(card);
        }
    }

    private void showGameOver(Player winner) {
        if (gameOverAnnounced) return;
        gameOverAnnounced = true;

        if (statusBarPanel != null) {
            statusBarPanel.setEndTurnEnabled(false);
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

        if (client != null) {
            client.close();
        }
        new EntryWindow();
        dispose();
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

    private boolean hasTargetableStar(Player player) {
        for (StarCard star : player.getField()) {
            if (star != null && !player.isCloaked(star)) return true;
        }
        return false;
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
        if (statusBarPanel != null) {
            statusBarPanel.setStatusText(text);
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
        List<Card> newHand = ProtocolParser.parseHand(message);
        myPlayer.getHand().clear();
        myPlayer.getHand().addAll(newHand);
        updateMyHand(myPlayer.getHand());
    }

    private void applyBoardSnapshot(String snapshot) {
        ProtocolParser.BoardUpdateData data = ProtocolParser.parseBoard(snapshot);

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
        for (int i = 0; i < deckCountsByIndex.length; i++) deckCountsByIndex[i] = -1;

        for (String record : data.playerRecords) {
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

        for (String record : data.fieldRecords) {
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

            if (hp < maxHp) star.takeDamage(maxHp - hp);
            if (!player.placeStar(star)) continue;

            if ("1".equals(parts[5])) player.addShield(star);
            if ("1".equals(parts[6])) player.addCloak(star);
        }

        for (String record : data.dropRecords) {
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
            if (data.turn >= 0) gameState.setCurrentPlayerIndex(data.turn);
            if (data.phase >= 0) gameState.setCurrentPhase(data.phase);

            gameState.setBlackHoleUsed(data.blackHoleUsed);
            gameState.setGameOver(data.gameOver);

            if (data.winnerIndex >= 0 && data.winnerIndex < targetPlayers.size()) {
                gameState.setWinner(targetPlayers.get(data.winnerIndex));
            }

            if (data.gameOver) {
                showGameOver(gameState.getWinner());
            } else if (gameState.getCurrentPlayer() != null) {
                Player current = gameState.getCurrentPlayer();
                String phaseText = phaseName(gameState.getCurrentPhase());

                if (current == myPlayer) {
                    setStatus("ถึงเทิร์นของคุณ — AP " + current.getActionPoints() + " (เฟส " + phaseText + ")");
                } else {
                    setStatus("รอเทิร์นของ " + current.getUsername() + " (เฟส " + phaseText + ")");
                }
            }

            if (statusBarPanel != null) {
                boolean canEndTurn = !data.gameOver && myPlayer != null && myPlayer.isActive()
                        && gameState.getCurrentPlayer() == myPlayer;
                statusBarPanel.setEndTurnEnabled(canEndTurn);
            }

            if (!data.gameOver && myPlayer != null && !myPlayer.isActive()) {
                if (statusBarPanel != null) statusBarPanel.setEndTurnEnabled(false);
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
            int availableW = getWidth();
            int availableH = getHeight();

            int cardW = Math.max(40, availableW - 12);
            int cardH = (int) Math.round(cardW / 1.6);

            int totalH = count * cardH + (count - 1) * gap;
            if (totalH > availableH) {
                cardH = Math.max(20, (availableH - (count - 1) * gap) / count);
                cardW = (int) Math.round(cardH * 1.6);
                totalH = count * cardH + (count - 1) * gap;
            }

            int startX = (availableW - cardW) / 2;
            int startY = Math.max(0, (availableH - totalH) / 2);
            int y = startY;

            for (Component card : getComponents()) {
                card.setBounds(startX, y, cardW, cardH);
                y += cardH + gap;
            }
        }
    }
}
