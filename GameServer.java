import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer implements ServerContext {

    private static final int MAX_PLAYERS = 4;
    private List<ClientHandler> clients;
    private GameState gameState;

    public GameServer() {
        this.clients = new ArrayList<>();
        this.gameState = null;
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] กำลังรอผู้เล่นที่ port " + port);

            // ใช้ while(true) เพื่อให้เซิร์ฟเวอร์ยังทำงานอยู่เสมอ (เผื่อมีคนหลุดแล้วต้องรับใหม่)
            while (true) {
                Socket socket = serverSocket.accept();

                // ป้องกันคนเข้าเกิน 4 คน
                if (clients.size() >= MAX_PLAYERS) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("ERR#SERVER_FULL");
                    socket.close();
                    continue;
                }

                System.out.println("[Server] มีการเชื่อมต่อใหม่จาก: " + socket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            case "JOIN":
                if (parts.length > 1) {
                    String username = parts[1];
                    sender.setPlayer(new Player(username)); // สร้าง Player เมื่อได้รับชื่อแล้ว
                    System.out.println("[Server] ผู้เล่น " + username + " เข้าห้อง (" + clients.size() + "/" + MAX_PLAYERS + ")");
                    broadcast("SYSTEM#" + username + "_JOINED");

                    checkAndStartGame(); // ตรวจสอบว่าคนครบหรือยัง
                }
                break;

            case "PLAY":
            case "ATTACK":
            case "DEFEND":
                if (gameState == null) {
                    sender.sendMessage("ERR#GAME_NOT_READY");
                    return;
                }
                // TODO: ใส่ลอจิกการเล่นการ์ด เช่น วางดาว โจมตี
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
            System.out.println("[Server] ผู้เล่น " + handler.getPlayer().getUsername() + " ออกจากเกม");
            broadcast("SYSTEM#" + handler.getPlayer().getUsername() + "_LEFT");
        }
    }

    private void checkAndStartGame() {
        if (clients.size() == MAX_PLAYERS) {
            List<Player> players = new ArrayList<>();
            for (ClientHandler client : clients) {
                if (client.getPlayer() == null) return;
                players.add(client.getPlayer());
            }
            // สร้าง GameState เมื่อทุกคนส่งชื่อมาครบแล้ว
            gameState = new GameState(players);
            System.out.println("[Server] ผู้เล่นครบ " + MAX_PLAYERS + " คน!");
            broadcast("SYSTEM#GAME_START");
        }
    }

}
