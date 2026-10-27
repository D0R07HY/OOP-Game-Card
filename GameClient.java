import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class GameClient {
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private GameWindow ui;
    private Consumer<String> onMessage;

    public void setUi(GameWindow ui) {
        this.ui = ui;
    }

    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public boolean connect(String ip, int port, String username) {
        try {
            sk = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(sk.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream(), StandardCharsets.UTF_8), true);

            out.println("JOIN#" + username);
            startListening();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void sendAction(String actionMsg) {
        if (out != null) {
            out.println(actionMsg);
        }
    }

    public void startListening() {
        if (in == null) return;

        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("[Server] " + line);
                    if (onMessage != null) {
                        onMessage.accept(line);
                    } else if (ui != null) {
                        String message = line;
                        SwingUtilities.invokeLater(() -> ui.updateBoardState(message));
                    }
                }
            } catch (IOException e) {
                System.out.println("[Client] Connection closed");
            }
        });
        listener.setDaemon(true);
        listener.start();
    }
}
