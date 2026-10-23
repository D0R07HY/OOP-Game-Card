import java.util.List;

public class GameState {
    private List<Player> players;
    private int currentPlayerIndex;
    private int currentPhase;
    private boolean isGameOver;
    private Player winner;
    private boolean blackHoleUsed;

    public GameState(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0;
        this.currentPhase = 1;
        this.isGameOver = false;
        this.winner = null;
        this.blackHoleUsed = false;
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
    }


    public void advancePhase() {
        if (currentPhase < 3)
            currentPhase++;
        else
            nextTurn();

    }

    public boolean checkWinCondition(Player player) {
        if (player.getField().length >= 9) {
            this.isGameOver = true;
            this.winner = player;
            return true;
        }
        return false;
    }

    public void eliminatePlayer(Player player) {
        players.remove(player);

        if (players.size() == 1) {
            this.isGameOver = true;
            this.winner = players.get(0);
        } else if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
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
        this.blackHoleUsed = blackHoleUsed;
    }

    public String toProtocolString() {
        StringBuilder sb = new StringBuilder();
        sb.append("STATE_UPDATE#");
        sb.append("TURN:").append(currentPlayerIndex).append("#");
        sb.append("PHASE:").append(currentPhase).append("#");
        sb.append("BLACK_HOLE:").append(blackHoleUsed ? "1" : "0").append("#");
        sb.append("PLAYERS_COUNT:").append(players.size());
        return sb.toString();
    }
}
