import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer implements ServerContext {

    private static final int MAX_PLAYERS = 4;
    private static final int OPENING_HAND = 5;
    public static final int DEFAULT_PORT = 5000;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameState gameState;

    public GameServer() {
        this.clients = new CopyOnWriteArrayList<>();
        this.gameState = null;
    }

    public void startInBackground(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        System.out.println("[Server] Waiting for players on port " + port);

        Thread serverThread = new Thread(this::acceptLoop);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void acceptLoop() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                if (clients.size() >= MAX_PLAYERS) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("ERR#SERVER_FULL");
                    socket.close();
                    continue;
                }

                System.out.println("[Server] New connection from: " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                synchronized (this) {
                    clients.add(handler);
                }
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("[Server] Stopped accepting connections: " + e.getMessage());
        }
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    @Override
    public synchronized void processClientAction(String action, ClientHandler sender) {
        if (action == null || action.isEmpty()) {
            return;
        }

        String[] parts = action.split("#", -1);
        String command = parts[0];

        switch (command) {
            case "JOIN": {
                if (gameState != null) {
                    sender.sendMessage("ERR#ALREADY_STARTED");
                    break;
                }

                if (parts.length < 2) {
                    sender.sendMessage("ERR#INVALID_NAME");
                    break;
                }

                String username = parts[1].trim();
                if (username.isEmpty()) {
                    sender.sendMessage("ERR#INVALID_NAME");
                    break;
                }

                if (hasPlayerName(username)) {
                    sender.sendMessage("ERR#NAME_TAKEN");
                    break;
                }

                sender.setPlayer(new Player(username));
                System.out.println("[Server] Player " + username + " joined the room ("
                        + countPlayers() + "/" + MAX_PLAYERS + ")");
                broadcastLobby();
                break;
            }

            case "PLAY":
            case "ATTACK":
            case "DEFEND":
                handleCardAction(sender, parts);
                break;

            case "START": {
                if (gameState != null) {
                    sender.sendMessage("ERR#ALREADY_STARTED");
                    break;
                }
                if (!isHost(sender)) {
                    sender.sendMessage("ERR#NOT_HOST");
                    break;
                }
                if (countPlayers() < 2) {
                    sender.sendMessage("ERR#NEED_MORE_PLAYERS");
                    break;
                }

                List<Player> players = buildPlayerList();
                dealOpeningHands(players);
                gameState = new GameState(players);
                startTurn(gameState.getCurrentPlayer());
                gameState.addChangeListener(this::broadcastState);

                StringBuilder start = new StringBuilder("SYSTEM#GAME_START");

                for (Player player : players) {
                    start.append("#").append(player.getUsername());
                }

                System.out.println("[Server] Game started with " + players.size() + " players");
                broadcast(start.toString());
                broadcastState();
                break;
            }

            case "END_TURN":
                if (gameState == null) {
                    sender.sendMessage("ERR#GAME_NOT_READY");
                    break;
                }
                if (gameState.isGameOver()) {
                    sender.sendMessage("ERR#GAME_OVER");
                    break;
                }
                if (sender.getPlayer() != gameState.getCurrentPlayer()) {
                    sender.sendMessage("ERR#NOT_YOUR_TURN");
                    break;
                }

                Player current = gameState.getCurrentPlayer();

                // End Phase: เช็คชนะ -> เช็ค Hand Limit -> สลับเทิร์น
                // บัฟที่หมดอายุ (Nebula Cloak) จะถูกลบตอนเริ่มเทิร์นถัดไปของเจ้าของ
                // เพื่อให้ยังมีผลตลอดเทิร์นของคู่แข่งตามกติกาการ์ด
                gameState.setCurrentPhase(3);

                if (gameState.checkWinCondition(current)) {
                    break;
                }

                enforceHandLimit(current);

                gameState.nextTurn();
                startTurn(gameState.getCurrentPlayer());
                broadcastState();
                break;

            default:
                sender.sendMessage("ERR#UNKNOWN_COMMAND");
                break;
        }
    }

    private void handleCardAction(ClientHandler sender, String[] parts) {
        if (gameState == null) {
            sendError(sender, "GAME_NOT_READY");
            return;
        }

        if (gameState.isGameOver()) {
            sendError(sender, "GAME_OVER");
            return;
        }

        Player owner = sender.getPlayer();

        if (owner == null) {
            sendError(sender, "INVALID_PLAYER");
            return;
        }

        if (!owner.isActive()) {
            sendError(sender, "PLAYER_INACTIVE");
            return;
        }

        if (owner != gameState.getCurrentPlayer()) {
            sendError(sender, "NOT_YOUR_TURN");
            return;
        }

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            sendError(sender, "INVALID_ACTION");
            return;
        }

        Card card = findCardInHand(owner, parts[1].trim());

        if (card == null) {
            sendError(sender, "CARD_NOT_IN_HAND");
            return;
        }

        int targetIndex = parseInt(parts.length > 2 ? parts[2] : "", -1);
        int targetSlot = parseInt(parts.length > 3 ? parts[3] : "", -1);
        String costId = parts.length > 4 ? parts[4].trim() : "";

        Player target = null;

        if (targetIndex != -1) {
            if (targetIndex < 0 || targetIndex >= gameState.getPlayers().size()) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            target = gameState.getPlayers().get(targetIndex);

            if (target == null) {
                sendError(sender, "INVALID_TARGET");
                return;
            }
        }

        StarCard targetStar = null;

        if (targetSlot != -1) {
            if (target == null || targetSlot < 0 || targetSlot >= target.getField().length) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            targetStar = target.getField()[targetSlot];

            if (targetStar == null) {
                sendError(sender, "INVALID_TARGET");
                return;
            }
        }

        Card cost = null;

        if (!costId.isEmpty()) {
            cost = findCardInHand(owner, costId);

            if (cost == null || cost == card) {
                sendError(sender, "INVALID_COST");
                return;
            }
        }

        if (card instanceof StarCard) {
            playStar(sender, owner, (StarCard) card);
        } else if (card instanceof SpecialCard) {
            playSpecial(sender, owner, (SpecialCard) card, target, targetStar, cost);
        } else {
            sendError(sender, "UNSUPPORTED_CARD");
        }
    }

    private void playStar(ClientHandler sender, Player owner, StarCard star) {
        if (owner.hasStarOnField(star.getName())) {
            sendError(sender, "DUPLICATE_STAR");
            return;
        }

        if (owner.getActionPoints() < 1) {
            sendError(sender, "NOT_ENOUGH_AP");
            return;
        }

        int pos = star.getOrbitPosition();

        if (pos < 0 || pos >= owner.getField().length || owner.getField()[pos] != null) {
            pos = owner.findFirstEmptySlot();
        }

        if (pos < 0) {
            sendError(sender, "FIELD_FULL");
            return;
        }

        star.setOrbitPosition(pos);

        if (!owner.placeStar(star)) {
            sendError(sender, "CANNOT_PLAY_CARD");
            return;
        }

        owner.removeCardFromHand(star);
        owner.useActionPoint(1);
        gameState.checkWinCondition(owner);
        broadcastState();
    }

    private void playSpecial(ClientHandler sender, Player owner, SpecialCard card,
                             Player target, StarCard targetStar, Card cost) {
        String name = card.getName();

        if (target != null && !target.isActive()) {
            sendError(sender, "INVALID_TARGET");
            return;
        }

        if (name.equals("Meteor Strike")) {
            if (target == null || target == owner || targetStar == null) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (target.isCloaked(targetStar)) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, target, targetStar, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Supernova")) {
            if (target == null || target == owner || !hasTargetableStar(target)) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, target, null, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Pulsar")) {
            if (target == null || target == owner || targetStar == null || cost == null) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (target.isCloaked(targetStar)) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (owner.isPulsarUsedThisTurn()) {
                sendError(sender, "PULSAR_ALREADY_USED");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            ((Pulsar) card).playEffect(owner, target, targetStar, cost, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Force Field")
                || name.equals("Nebula Cloak")
                || name.equals("Stellar Regeneration")) {
            if (target != owner || targetStar == null) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            int pos = targetStar.getOrbitPosition();

            if (pos < 0 || pos >= owner.getField().length || owner.getField()[pos] != targetStar) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, owner, targetStar, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Space Probe")) {
            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, null, null, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Big Bang")) {
            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, null, null, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Black Hole")) {
            if (gameState.isBlackHoleUsed()) {
                sendError(sender, "BLACK_HOLE_ALREADY_USED");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            card.playEffect(owner, null, null, gameState);
            finishSpecial(owner, card);
        } else if (name.equals("Worm Hole")) {
            if (target == null || target == owner || cost == null || target.getHand().isEmpty()) {
                sendError(sender, "INVALID_TARGET");
                return;
            }

            if (!spendActionPoint(sender, owner)) {
                return;
            }

            ((WormHole) card).playEffectWithCard(owner, target, cost, gameState);
            finishSpecial(owner, card);
        } else {
            sendError(sender, "UNSUPPORTED_CARD");
        }
    }

    private boolean spendActionPoint(ClientHandler sender, Player owner) {
        if (owner.getActionPoints() < 1) {
            sendError(sender, "NOT_ENOUGH_AP");
            return false;
        }

        return owner.useActionPoint(1);
    }

    private void finishSpecial(Player owner, SpecialCard card) {
        owner.removeCardFromHand(card);

        if (!card.getName().equals("Black Hole")) {
            owner.addToDropZone(card);
        }

        gameState.checkWinCondition(owner);
        broadcastState();
    }

    private boolean hasTargetableStar(Player player) {
        for (StarCard star : player.getField()) {
            if (star != null && !player.isCloaked(star)) {
                return true;
            }
        }

        return false;
    }

    private Card findCardInHand(Player player, String cardId) {
        for (Card card : player.getHand()) {
            if (card.getId() != null && card.getId().equals(cardId)) {
                return card;
            }
        }

        return null;
    }

    private void sendError(ClientHandler sender, String code) {
        sender.sendMessage("ERR#" + code);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void dealOpeningHands(List<Player> players) {
        for (int round = 0; round < OPENING_HAND; round++) {
            for (Player player : players) {
                Card card = player.getDeck().drawCard();

                if (card != null) {
                    player.addCardToHand(card);
                }
            }
        }
    }

    // Draw Phase: จั่ว 1 ใบอัตโนมัติ; ถ้าเด็คหมดผู้เล่นคนนั้นแพ้ทันที (Deck Out)
    private boolean startTurn(Player player) {
        if (player == null || gameState == null) {
            return false;
        }

        gameState.setCurrentPhase(1);

        // เริ่มเทิร์นของเจ้าของ: บัฟป้องกันที่หมดอายุแล้ว (Nebula Cloak)
        // ถูกลบตรงนี้ เพื่อให้ cloak ยังมีผลตลอดเทิร์นของคู่แข่งก่อนหน้า
        player.clearExpiredBuffs();

        Card card = player.getDeck().drawCard();

        if (card != null) {
            player.addCardToHand(card);
            gameState.setCurrentPhase(2);
            return true;
        }

        player.setActive(false);

        if (gameState.getActivePlayerCount() <= 1) {
            gameState.setWinner(gameState.getFirstActivePlayer());
            gameState.setGameOver(true);
            return false;
        }

        gameState.nextTurn();
        return startTurn(gameState.getCurrentPlayer());
    }

    // End Phase: มือเกิน 7 ใบ บังคับทิ้งใบล่าสุดลง Drop Zone จนเหลือ 7
    private void enforceHandLimit(Player player) {
        if (player == null) {
            return;
        }

        int discarded = 0;

        while (player.getHand().size() > Player.MAX_HAND) {
            Card card = player.getHand().get(player.getHand().size() - 1);
            player.removeCardFromHand(card);
            player.addToDropZone(card);
            discarded++;
        }

        if (discarded > 0) {
            sendToPlayer(player, "SYSTEM#มือเกิน 7 ใบ: ทิ้ง " + discarded
                    + " ใบลง Drop Zone");
        }
    }

    private void sendToPlayer(Player player, String message) {
        for (ClientHandler client : clients) {
            if (client.getPlayer() == player) {
                client.sendMessage(message);
                return;
            }
        }
    }

    @Override
    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);

        Player player = handler.getPlayer();

        if (player != null) {
            System.out.println("[Server] Player " + player.getUsername() + " left the room");
            broadcast("SYSTEM#" + player.getUsername() + "_LEFT");

            if (gameState != null && gameState.getPlayers().contains(player)) {
                player.setActive(false);

                if (gameState.getActivePlayerCount() <= 1) {
                    gameState.setWinner(gameState.getFirstActivePlayer());
                    gameState.setGameOver(true);
                } else if (gameState.getCurrentPlayer() == player) {
                    gameState.nextTurn();
                    startTurn(gameState.getCurrentPlayer());
                }

                broadcastState();
            }
        }

        if (gameState == null) {
            broadcastLobby();
        }
    }

    private void broadcastState() {
        if (gameState == null) {
            return;
        }

        String snapshot = gameState.toProtocolString();

        for (ClientHandler client : clients) {
            Player player = client.getPlayer();

            if (player == null) {
                continue;
            }

            client.sendMessage(snapshot);
            client.sendMessage(toHandMessage(player));
        }
    }

    private String toHandMessage(Player player) {
        StringBuilder sb = new StringBuilder("HAND");

        for (Card card : player.getHand()) {
            sb.append("#").append(safeToken(card.getId())).append(",")
                    .append(safeToken(card.getName())).append(",")
                    .append(safeToken(card.getType())).append(",");

            if (card instanceof StarCard) {
                StarCard star = (StarCard) card;
                sb.append(star.getMaxHp()).append(",")
                        .append(star.getOrbitPosition());
            } else {
                sb.append("0,-1");
            }
        }

        return sb.toString();
    }

    private static String safeToken(String value) {
        if (value == null) {
            return "";
        }

        return value.replace(',', ' ').replace('#', ' ').replace(':', ' ');
    }

    private void broadcastLobby() {
        StringBuilder lobby = new StringBuilder("LOBBY");

        for (ClientHandler client : clients) {
            Player player = client.getPlayer();

            if (player != null) {
                lobby.append("#").append(player.getUsername());
            }
        }

        broadcast(lobby.toString());
    }

    private boolean hasPlayerName(String username) {
        for (ClientHandler client : clients) {
            Player player = client.getPlayer();

            if (player != null && player.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private boolean isHost(ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client.getPlayer() != null) {
                return client == sender;
            }
        }
        return false;
    }

    private int countPlayers() {
        int count = 0;

        for (ClientHandler client : clients) {
            if (client.getPlayer() != null) {
                count++;
            }
        }

        return count;
    }

    private List<Player> buildPlayerList() {
        List<Player> players = new ArrayList<>();

        for (ClientHandler client : clients) {
            if (client.getPlayer() != null) {
                players.add(client.getPlayer());
            }
        }

        return players;
    }
}
