import java.io.*;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class GameClient {
    private Socket sk;
    private BufferedReader in;
    private PrintWriter out;
    private GameWindow ui;

    public void setUi(GameWindow ui) {
        this.ui = ui;
    }

    public void connect(String ip, int port) {
        try {
            sk = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(sk.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(sk.getOutputStream()), true);

            out.println("JOIN#Arty");
        } catch (IOException e) {
            e.printStackTrace();
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
                    String message = line;

                    if (ui != null) {
                        SwingUtilities.invokeLater(() -> ui.updateBoardState(message));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listener.start();
    }
}
