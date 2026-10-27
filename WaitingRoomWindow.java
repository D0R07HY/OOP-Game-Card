import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.*;
import javax.swing.*;

public class WaitingRoomWindow extends JFrame {

    private final GameClient client;
    private final boolean isHost;
    private final String username;
    private final List<String> lobbyNames;
    private final JLabel countLabel;
    private final JTextArea namesArea;
    private final JButton startButton;

    public WaitingRoomWindow(GameClient client, boolean isHost, String displayIp, int port, String username) {
        this.client = client;
        this.isHost = isHost;
        this.username = username;
        this.lobbyNames = new ArrayList<>();

        setTitle(isHost ? "My Room" : "Joined Room");
        setSize(380, 340);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        countLabel = new JLabel("Players in room: 0/4", JLabel.CENTER);

        JLabel roomLabel = new JLabel(roomText(isHost, displayIp, port), JLabel.CENTER);

        JPanel top = new JPanel(new GridLayout(2, 1, 2, 2));
        top.setBorder(BorderFactory.createEmptyBorder(12, 10, 0, 10));
        top.add(countLabel);
        top.add(roomLabel);

        namesArea = new JTextArea();
        namesArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(namesArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));

        startButton = new JButton("Start Game");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> client.sendAction("START"));

        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        if (isHost) {
            bottom.add(startButton);
        } else {
            bottom.add(new JLabel("Waiting for the host to start..."));
        }

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    private String roomText(boolean isHost, String displayIp, int port) {
        if (isHost) {
            return "IP: " + displayIp + "   Port: " + port + "   (use 127.0.0.1 on this PC)";
        }
        return "Connected: " + displayIp + ":" + port;
    }

    public void handleMessage(String message) {
        if (message == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("LOBBY#")) {
                updateLobby(message);
            } else if (message.startsWith("SYSTEM#GAME_START")) {
                startGame(message);
            } else if (message.startsWith("ERR#")) {
                JOptionPane.showMessageDialog(this, errorText(message));

                if (message.equals("ERR#SERVER_FULL")
                        || message.equals("ERR#NAME_TAKEN")
                        || message.equals("ERR#ALREADY_STARTED")) {
                    dispose();
                }
            }
        });
    }

    private void updateLobby(String message) {
        String[] parts = message.split("#");
        StringBuilder names = new StringBuilder();

        lobbyNames.clear();

        for (int i = 1; i < parts.length; i++) {
            String name = parts[i].trim();

            if (name.isEmpty()) {
                continue;
            }

            lobbyNames.add(name);
            names.append(lobbyNames.size()).append(". ").append(name).append("\n");
        }

        int playerCount = lobbyNames.size();
        namesArea.setText(names.toString());
        countLabel.setText("Players in room: " + playerCount + "/4");
        startButton.setEnabled(isHost && playerCount >= 2);
    }

    private void startGame(String message) {
        String[] parts = message.split("#", -1);
        List<String> names = new ArrayList<>();

        for (int i = 2; i < parts.length; i++) {
            String name = parts[i].trim();

            if (!name.isEmpty()) {
                names.add(name);
            }
        }

        if (names.isEmpty()) {
            names.addAll(lobbyNames);
        }

        if (names.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot start game: player list is empty");
            return;
        }

        List<Player> players = new ArrayList<>();
        Player me = null;

        for (String name : names) {
            Player player = new Player(name);
            players.add(player);

            if (name.equals(username)) {
                me = player;
            }
        }

        if (me == null) {
            me = players.get(0);
        }

        GameWindow window = new GameWindow();
        window.setPlayers(players);
        window.setMyPlayer(me);
        window.setGameClient(client);

        client.setUi(window);
        client.setOnMessage(window::updateBoardState);

        window.setVisible(true);
        dispose();
    }

    private String errorText(String message) {
        if (message.equals("ERR#SERVER_FULL")) {
            return "Room is full (max 4 players)";
        }
        if (message.equals("ERR#NAME_TAKEN")) {
            return "That name is already taken";
        }
        if (message.equals("ERR#NOT_HOST")) {
            return "Only the host can start the game";
        }
        if (message.equals("ERR#NEED_MORE_PLAYERS")) {
            return "Need at least 2 players";
        }
        if (message.equals("ERR#ALREADY_STARTED")) {
            return "The game has already started";
        }
        if (message.equals("ERR#INVALID_NAME")) {
            return "Invalid player name";
        }
        return message;
    }
}
