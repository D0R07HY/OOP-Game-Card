import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer implements ServerContext {

    private static final int MAX_PLAYERS = 4;
    public static final int DEFAULT_PORT = 5000;
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private GameState gameState;

    public GameServer() {
        this.clients = new ArrayList<>();
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
        if (action == null || action.isEmpty()) return;

        String[] parts = action.split("#");
        String command = parts[0];

        switch (command) {
            case "JOIN": {
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
                System.out.println("[Server] Player " + username + " joined the room (" + countPlayers() + "/" + MAX_PLAYERS + ")");
                broadcastLobby();
                break;
            }

            case "PLAY":
            case "ATTACK":
            case "DEFEND":
                if (gameState == null) {
                    sender.sendMessage("ERR#GAME_NOT_READY");
                    return;
                }
                // TODO: ใส่ลอจิกการเล่นการ์ด เช่น วางดาว โจมตี
                break;

            case "START":
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
                gameState = new GameState(buildPlayerList());
                System.out.println("[Server] Game started with " + countPlayers() + " players");
                broadcast("SYSTEM#GAME_START");
                break;

            case "END_TURN":
                if (gameState != null) {
                    gameState.nextTurn();
                    // อัปเดตกระดานทั้งหมดไปให้ผู้เล่นทุกคนหลังสลับเทิร์น
                    // broadcast(gameState.toProtocolString());
                }
                break;

            default:
                sender.sendMessage("ERR#UNKNOWN_COMMAND");
                break;
        }
    }

    @Override
    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);
        if (handler.getPlayer() != null) {
            System.out.println("[Server] Player " + handler.getPlayer().getUsername() + " left the game");
            broadcast("SYSTEM#" + handler.getPlayer().getUsername() + "_LEFT");
        }
        if (gameState == null) {
            broadcastLobby();
        }
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
