import java.io.*;
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


    public ClientHandler(Socket socket, ServerContext context) throws IOException {

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

                    context.processClientAction(message, this);
                } catch (RuntimeException e) {
                    sendMessage("ERROR#SERVER_ERROR#Unexpected server error");
                }
            }
        } catch (IOException e) {
        } finally {
            running = false;
            closeSocket();
            notifyRemoved();
        }
    }

    public synchronized void sendMessage(String message) {
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
