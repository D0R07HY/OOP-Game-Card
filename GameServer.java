import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// ข้อกำหนดของ ClientHandler (ไฟล์ที่เพื่อนจะเขียน):
//   public class ClientHandler extends Thread
//   public ClientHandler(Socket socket, GameServer server)
//   public void sendMessage(String msg)
//   public Player getPlayer()

public class GameServer {

    private static final int MAX_PLAYERS = 4;

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clients; // เก็บ Socket ของเพื่อน 4 คน
    private GameState gameState;              // เก็บสเตตัสวงไพ่ทั้งหมด

    public GameServer() {
        this.clients = new ArrayList<>();
        // GameState ต้องใช้ List<Player> เลยรอสร้างตอนผู้เล่นครบ 4 คนใน start()
        this.gameState = null;
    }

    // เปิดเซิร์ฟเวอร์และวนลูปรับคนเข้า accept()
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Horizon CardOnline server กำลังรอผู้เล่นที่ port " + port);

            while (clients.size() < MAX_PLAYERS) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                handler.start(); // ClientHandler extends Thread
                System.out.println("ผู้เล่นต่อเข้ามา " + clients.size() + "/" + MAX_PLAYERS);
            }

            // ผู้เล่นครบ -> สร้าง GameState และประกาศสเตตัสแรกให้ทุกคน
            List<Player> players = new ArrayList<>();
            for (ClientHandler handler : clients) {
                players.add(handler.getPlayer());
            }
            gameState = new GameState(players);
            System.out.println("ผู้เล่นครบ " + MAX_PLAYERS + " คน พร้อมเริ่มเกม");
            broadcast(gameState.toProtocolString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ส่งข้อความเช่น UPDATE_BOARD#... ไปให้ ClientHandler ทั้ง 4 ตัวพร้อมกัน
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // แกะข้อความที่ Client ส่งมา เช่น PLAY#MERCURY แล้วรันลอจิกเกม
    public synchronized void processClientAction(String action, ClientHandler sender) {
        if (action == null || action.isEmpty()) {
            return;
        }
        if (gameState == null) {
            if (sender != null) {
                sender.sendMessage("ERR#GAME_NOT_READY");
            }
            return;
        }

        String[] parts = action.split("#");
        String command = parts[0];

        switch (command) {
            case "PLAY":    // PLAY#MERCURY  -> วางดาวลงกระดาน (1 Action Point)
            case "ATTACK":  // ATTACK#PLAYER2#METEOR_STRIKE
            case "DEFEND":  // DEFEND#SHIELD
                // TODO: ยังต้องดึงลอจิกจาก Deck/Player มาต่อ ( GameState ยังไม่มีเมธอดรับการ์ด )
                break;

            case "END_TURN":
                // TODO: End Phase จริงต้องเช็คชนะ -> ลบบัฟ -> เช็ค Hand Limit ก่อนสลับเทิร์น
                gameState.nextTurn();
                break;

            default:
                if (sender != null) {
                    sender.sendMessage("ERR#UNKNOWN_COMMAND");
                }
                return;
        }

        // ส่งสเตตัสใหม่กลับให้ทุกคน เช่น STATE_UPDATE#TURN:0#PHASE:1#...
        broadcast(gameState.toProtocolString());
    }

    public static void main(String[] args) {
        new GameServer().start(6767);
    }
}
