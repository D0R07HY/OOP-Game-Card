import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final ServerContext context;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile boolean running;
    private boolean removed;
    private Player player;

    // แก้ไข Constructor ให้รับ ServerContext
    public ClientHandler(Socket socket, ServerContext context) throws IOException {
        if (socket == null || context == null) {
            throw new IllegalArgumentException("socket and context are required");
        }

        this.socket = socket;
        this.context = context;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        this.running = true;
        this.removed = false;
        this.player = null;
    }

    @Override
    public void run() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                message = message.trim();
                if (message.isEmpty()) {
                    continue;
                }

                try {
                    // โยนข้อความไปให้ GameServer ทำงานต่อ
                    context.processClientAction(message, this);
                } catch (RuntimeException e) {
                    sendMessage("ERROR#SERVER_ERROR#Unexpected server error");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection lost: " + socket.getRemoteSocketAddress());
        } finally {
            running = false;
            closeSocket();
            notifyRemoved();
        }
    }

    public void sendMessage(String message) {
        if (message != null && !socket.isClosed()) {
            out.println(message);
        }
    }

    public void disconnect() {
        running = false;
        closeSocket();
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Socket getSocket() {
        return socket;
    }

    private void notifyRemoved() {
        synchronized (this) {
            if (removed) {
                return;
            }
            removed = true;
        }
        context.removeClient(this);
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
