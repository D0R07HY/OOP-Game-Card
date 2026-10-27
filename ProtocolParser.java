import java.util.ArrayList;
import java.util.List;

public class ProtocolParser {

    
    public static class BoardUpdateData {
        public int turn = -1;
        public int phase = -1;
        public int winnerIndex = -1;
        public boolean blackHoleUsed = false;
        public boolean gameOver = false;
        public List<String> playerRecords = new ArrayList<>();
        public List<String> fieldRecords = new ArrayList<>();
        public List<String> dropRecords = new ArrayList<>();
    }

    
    public static List<Card> parseHand(String message) {
        List<Card> newHand = new ArrayList<>();
        if (message == null || message.isEmpty()) {
            return newHand;
        }

        String body = message.startsWith("HAND#") ? message.substring("HAND#".length()) : "";

        if (!body.isEmpty()) {
            for (String record : body.split("#", -1)) {
                String[] parts = record.split(",", -1);
                if (parts.length < 5) continue;

                Card card = CardFactory.create(
                        parts[0],
                        parts[1],
                        parts[2],
                        parseInt(parts[3], 0),
                        parseInt(parts[4], -1)
                );

                if (card != null) {
                    newHand.add(card);
                }
            }
        }
        return newHand;
    }

    
    public static BoardUpdateData parseBoard(String snapshot) {
        BoardUpdateData data = new BoardUpdateData();
        if (snapshot == null || snapshot.isEmpty()) {
            return data;
        }

        String[] tokens = snapshot.split("#");

        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("TURN:")) {
                data.turn = parseInt(token.substring("TURN:".length()), -1);
            } else if (token.startsWith("PHASE:")) {
                data.phase = parseInt(token.substring("PHASE:".length()), -1);
            } else if (token.startsWith("BH:")) {
                data.blackHoleUsed = "1".equals(token.substring("BH:".length()));
            } else if (token.startsWith("OVER:")) {
                data.gameOver = "1".equals(token.substring("OVER:".length()));
            } else if (token.startsWith("WINNER:")) {
                data.winnerIndex = parseInt(token.substring("WINNER:".length()), -1);
            } else if (token.startsWith("P:")) {
                data.playerRecords.add(token.substring("P:".length()));
            } else if (token.startsWith("F:")) {
                data.fieldRecords.add(token.substring("F:".length()));
            } else if (token.startsWith("D:")) {
                data.dropRecords.add(token.substring("D:".length()));
            }
        }
        return data;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
