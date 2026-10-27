import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private int currentPhase;
    private boolean isGameOver;
    private Player winner;
    private boolean blackHoleUsed;
    private final List<Runnable> changeListeners;

    public GameState(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.currentPhase = 1;
        this.isGameOver = false;
        this.winner = null;
        this.blackHoleUsed = false;
        this.changeListeners = new ArrayList<>();
        bindPlayerListeners();
    }

    private void bindPlayerListeners() {
        if (players == null) {
            return;
        }

        for (Player player : players) {
            if (player != null) {
                player.setChangeListener(this::fireStateChanged);
            }
        }
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void fireStateChanged() {
        for (Runnable listener : new ArrayList<>(changeListeners)) {
            listener.run();
        }
    }

    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty()) return null;
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        if (isGameOver) return;

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        currentPhase = 1;
        Player nextPlayer = getCurrentPlayer();
        if (nextPlayer != null)
            nextPlayer.resetActionPoints();
        fireStateChanged();
    }


    public void advancePhase() {
        if (currentPhase < 3)
            currentPhase++;
        else
            nextTurn();

    }

    public boolean checkWinCondition(Player player) {
        if (player != null && player.checkWinCondition()) {
            this.isGameOver = true;
            this.winner = player;
            fireStateChanged();
            return true;
        }
        return false;
    }

    public void eliminatePlayer(Player player) {
        if (player == null || !players.remove(player)) {
            return;
        }

        if (players.size() == 1) {
            this.isGameOver = true;
            this.winner = players.get(0);
        } else if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }

        fireStateChanged();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Player getWinner() {
        return winner;
    }

    public boolean isBlackHoleUsed() {
        return blackHoleUsed;
    }

    public void setBlackHoleUsed(boolean blackHoleUsed) {
        if (this.blackHoleUsed != blackHoleUsed) {
            this.blackHoleUsed = blackHoleUsed;
            fireStateChanged();
        }
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        if (players == null || players.isEmpty()) {
            this.currentPlayerIndex = 0;
        } else {
            this.currentPlayerIndex = Math.max(0,
                    Math.min(currentPlayerIndex, players.size() - 1));
        }
    }

    public void setCurrentPhase(int currentPhase) {
        this.currentPhase = Math.max(1, Math.min(3, currentPhase));
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    public void setWinner(Player winner) {
        this.winner = winner;

        if (winner != null) {
            this.isGameOver = true;
        }
    }

    public String toProtocolString() {
        StringBuilder sb = new StringBuilder("UPDATE_BOARD");
        sb.append("#TURN:").append(currentPlayerIndex);
        sb.append("#PHASE:").append(currentPhase);
        sb.append("#BH:").append(blackHoleUsed ? "1" : "0");
        sb.append("#OVER:").append(isGameOver ? "1" : "0");
        sb.append("#WINNER:").append(players == null ? -1 : players.indexOf(winner));

        if (players == null) {
            return sb.toString();
        }

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            if (player == null) {
                continue;
            }

            sb.append("#P:").append(i).append(",")
                    .append(safeToken(player.getUsername())).append(",")
                    .append(player.getActionPoints()).append(",")
                    .append(player.getHand().size()).append(",")
                    .append(player.getDeck().size()).append(",")
                    .append(player.getDropZone().size());

            StarCard[] field = player.getField();

            for (int slot = 0; slot < field.length; slot++) {
                StarCard star = field[slot];

                if (star == null) {
                    continue;
                }

                sb.append("#F:").append(i).append(",")
                        .append(slot).append(",")
                        .append(safeToken(star.getName())).append(",")
                        .append(star.getHp()).append(",")
                        .append(star.getMaxHp()).append(",")
                        .append(player.isShielded(star) ? "1" : "0").append(",")
                        .append(player.isCloaked(star) ? "1" : "0");
            }

            for (Card card : player.getDropZone()) {
                sb.append("#D:").append(i).append(",")
                        .append(safeToken(card.getId())).append(",")
                        .append(safeToken(card.getName())).append(",")
                        .append(safeToken(card.getType())).append(",");

                if (card instanceof StarCard) {
                    StarCard star = (StarCard) card;
                    sb.append(star.getMaxHp()).append(",")
                            .append(star.getOrbitPosition());
                } else {
                    sb.append("0,-1");
                }
            }
        }

        return sb.toString();
    }

    private static String safeToken(String value) {
        if (value == null) {
            return "";
        }

        return value.replace(',', ' ').replace('#', ' ').replace(':', ' ');
    }
}
