import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

public class GameClient {
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private volatile GameWindow ui;
    private volatile Consumer<String> onMessage;

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
        if (in == null) {
            return;
        }

        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String message = line;

                    SwingUtilities.invokeLater(() -> dispatch(message));
                }
            } catch (IOException e) {
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private void dispatch(String message) {
        Consumer<String> listener = onMessage;

        if (listener != null) {
            listener.accept(message);
        } else if (ui != null) {
            ui.updateBoardState(message);
        }
    }

    public void close() {
        try {
            if (sk != null) {
                sk.close();
            }
        } catch (IOException ignored) {
        }
    }
}
