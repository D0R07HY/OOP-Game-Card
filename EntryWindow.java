import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class EntryWindow extends JFrame {

    private final JTextField nameField;
    private final JButton joinButton;
    private final JButton createButton;

    public EntryWindow() {
        setTitle("Horizon CardOnline");
        setSize(380, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Horizon CardOnline", JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        nameField = new JTextField();

        JPanel form = new JPanel(new GridLayout(2, 1, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 5, 40));
        form.add(new JLabel("Player name:"));
        form.add(nameField);

        joinButton = new JButton("Join");
        createButton = new JButton("Create Room");
        joinButton.setEnabled(false);
        createButton.setEnabled(false);

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(joinButton);
        buttons.add(createButton);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtons();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtons();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtons();
            }
        });

        joinButton.addActionListener(e -> joinRoom());
        createButton.addActionListener(e -> createRoom());
        setVisible(true);
        setLocationRelativeTo(null);
    }

    private void updateButtons() {
        String name = nameField.getText().trim();
        boolean valid = !name.isEmpty() && !name.contains("#");
        joinButton.setEnabled(valid);
        createButton.setEnabled(valid);
    }

    private void createRoom() {
        String username = nameField.getText().trim();
        if (username.isEmpty()) {
            return;
        }

        String portText = JOptionPane.showInputDialog(
                this,
                "Port to host on:",
                String.valueOf(GameServer.DEFAULT_PORT));

        if (portText == null) {
            return;
        }

        int port = parsePort(portText);
        if (port < 0) {
            JOptionPane.showMessageDialog(this, "Invalid port (use 1024-65535)");
            return;
        }

        GameServer server = new GameServer();
        try {
            server.startInBackground(port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot create room: port " + port + " is already in use");
            return;
        }

        openWaitingRoom(true, "127.0.0.1", port, username, getLocalIp());
    }

    private void joinRoom() {
        String username = nameField.getText().trim();
        if (username.isEmpty()) {
            return;
        }

        JTextField ipField = new JTextField("127.0.0.1");
        JTextField portField = new JTextField(String.valueOf(GameServer.DEFAULT_PORT));

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Room IP:"));
        panel.add(ipField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Join Room", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String ip = ipField.getText().trim();
        int port = parsePort(portField.getText().trim());

        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the room IP");
            return;
        }
        if (port < 0) {
            JOptionPane.showMessageDialog(this, "Invalid port (use 1024-65535)");
            return;
        }

        openWaitingRoom(false, ip, port, username, ip);
    }

    private void openWaitingRoom(boolean isHost, String connectIp, int port, String username, String displayIp) {
        GameClient client = new GameClient();
        WaitingRoomWindow room = new WaitingRoomWindow(client, isHost, displayIp, port, username);
        client.setOnMessage(room::handleMessage);

        if (!client.connect(connectIp, port, username)) {
            room.dispose();
            JOptionPane.showMessageDialog(this, "Cannot connect to room: " + connectIp + ":" + port);
            return;
        }

        room.setVisible(true);
        dispose();
    }

    private int parsePort(String text) {
        try {
            int port = Integer.parseInt(text.trim());
            if (port >= 1024 && port <= 65535) {
                return port;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private String getLocalIp() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            return ip.contains(":") ? "127.0.0.1" : ip;
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    public static void main(String[] args) {
        new EntryWindow();
    }
}
