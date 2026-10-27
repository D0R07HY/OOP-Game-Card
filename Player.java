import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player {
    public static final int MAX_FIELD = 9;
    public static final int MAX_HAND = 7;
    public static final int ACTION_POINTS_PER_TURN = 2;

    private String username;
    private int actionPoints;
    private List<Card> hand;
    private StarCard[] field;
    private List<Card> dropZone;
    private Deck deck;
    private boolean hasForceField;
    private boolean pulsarUsedThisTurn;
    private Set<StarCard> shieldedStars;
    private Set<StarCard> cloakedStars;

    public Player(String username) {
        this.username = username;
        this.actionPoints = ACTION_POINTS_PER_TURN;
        this.field = new StarCard[MAX_FIELD];
        this.hand = new ArrayList<>();
        this.dropZone = new ArrayList<>();
        this.deck = new Deck();
        this.hasForceField = false;
        this.pulsarUsedThisTurn = false;
        this.shieldedStars = new HashSet<>();
        this.cloakedStars = new HashSet<>();
    }

    public void resetActionPoints() {
        this.actionPoints = ACTION_POINTS_PER_TURN;
        this.pulsarUsedThisTurn = false;
    }

    public boolean hasUsedPulsarThisTurn() {
        return pulsarUsedThisTurn;
    }

    public void markPulsarUsed() {
        this.pulsarUsedThisTurn = true;
    }

    public boolean useActionPoint(int cost) {
        if (this.actionPoints >= cost) {
            this.actionPoints -= cost;
            return true;
        }
        return false;
    }

    public void setHasForceField(boolean hasForceField) {
        this.hasForceField = hasForceField;
    }

    public String getUsername() {
        return username;
    }

    public int getActionPoints() {
        return actionPoints;
    }

    public List<Card> getHand() {
        return hand;
    }

    public StarCard[] getField() {
        return field;
    }

    public List<Card> getDropZone() {
        return dropZone;
    }

    public Deck getDeck() {
        return deck;
    }

    public boolean hasForceField() {
        return hasForceField;
    }

    public void addCardToHand(Card c) {
        hand.add(c);
    }

    public void removeCardFromHand(Card c) {
        hand.remove(c);
    }

    public boolean placeStar(StarCard star) {
        int pos = star.getOrbitPosition();
        if (field[pos] == null) {
            field[pos] = star;
            return true;
        }
        return false;
    }

    public void removeStarFromField(StarCard star) {
        int pos = star.getOrbitPosition();
        if (pos >= 0 && pos < field.length && field[pos] == star) {
            field[pos] = null;
        }
        shieldedStars.remove(star);
        cloakedStars.remove(star);
    }

    public boolean checkWinCondition() {
        for (StarCard star : field) {
            if (star == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isHandOverLimit() {
        return hand.size() > MAX_HAND;
    }

    public void discardToLimit(Card c) {
        if (hand.remove(c)) {
            addToDropZone(c);
        }
    }

    public void addToDropZone(Card c) {
        if (c != null) {
            dropZone.add(c);
        }
    }

    public Card takeFromDropZone(String name) {
        if (name == null) {
            return null;
        }

        for (int i = 0; i < dropZone.size(); i++) {
            Card c = dropZone.get(i);
            if (name.equals(c.getName())) {
                return dropZone.remove(i);
            }
        }
        return null;
    }

    public boolean isShielded(StarCard star) {
        return shieldedStars.contains(star);
    }

    public void addShield(StarCard star) {
        if (star != null) {
            shieldedStars.add(star);
        }
    }

    public void removeShield(StarCard star) {
        shieldedStars.remove(star);
    }

    public boolean isCloaked(StarCard star) {
        return cloakedStars.contains(star);
    }

    public void addCloak(StarCard star) {
        if (star != null) {
            cloakedStars.add(star);
        }
    }

    public void clearExpiredBuffs() {
        cloakedStars.clear();
    }

    public boolean isProtected(StarCard star) {
        return isShielded(star) || isCloaked(star);
    }

    public List<StarCard> getTargetableStars() {
        List<StarCard> result = new ArrayList<>();
        for (StarCard star : field) {
            if (star != null && !isCloaked(star)) {
                result.add(star);
            }
        }
        return result;
    }
}
