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
    private Runnable changeListener;

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
        this.changeListener = null;
    }

    void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    private void fireStateChanged() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public void resetActionPoints() {
        this.actionPoints = ACTION_POINTS_PER_TURN;
        this.pulsarUsedThisTurn = false;
        fireStateChanged();
    }

    public boolean isPulsarUsedThisTurn() {
        return pulsarUsedThisTurn;
    }

    public boolean markPulsarUsed() {
        if (pulsarUsedThisTurn) {
            return false;
        }

        pulsarUsedThisTurn = true;
        fireStateChanged();
        return true;
    }

    public void setActionPoints(int actionPoints) {
        this.actionPoints = Math.max(0, actionPoints);
        fireStateChanged();
    }

    public boolean useActionPoint(int cost) {
        if (this.actionPoints >= cost) {
            this.actionPoints -= cost;
            fireStateChanged();
            return true;
        }
        return false;
    }

    public void setHasForceField(boolean hasForceField) {
        this.hasForceField = hasForceField;
        fireStateChanged();
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
        if (c == null) {
            return;
        }
        hand.add(c);
        fireStateChanged();
    }

    public void removeCardFromHand(Card c) {
        if (hand.remove(c)) {
            fireStateChanged();
        }
    }

    public boolean hasStarOnField(String name) {
        if (name == null) {
            return false;
        }

        for (StarCard star : field) {
            if (star != null && name.equals(star.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean placeStar(StarCard star) {
        if (star == null) {
            return false;
        }

        if (hasStarOnField(star.getName())) {
            return false;
        }

        int pos = star.getOrbitPosition();

        if (pos < 0 || pos >= field.length || field[pos] != null) {
            pos = findFirstEmptySlot();

            if (pos < 0) {
                return false;
            }
        }

        field[pos] = star;
        star.setOrbitPosition(pos);
        star.setChangeListener(this::fireStateChanged);
        fireStateChanged();
        return true;
    }

    public int findFirstEmptySlot() {
        for (int i = 0; i < field.length; i++) {
            if (field[i] == null) {
                return i;
            }
        }

        return -1;
    }

    public void removeStarFromField(StarCard star) {
        if (star == null) {
            return;
        }

        int pos = star.getOrbitPosition();

        if (pos >= 0 && pos < field.length && field[pos] == star) {
            field[pos] = null;
        }

        star.setChangeListener(null);
        shieldedStars.remove(star);
        cloakedStars.remove(star);
        fireStateChanged();
    }

    public void clearField() {
        for (int i = 0; i < field.length; i++) {
            if (field[i] != null) {
                field[i].setChangeListener(null);
                field[i] = null;
            }
        }

        shieldedStars.clear();
        cloakedStars.clear();
        fireStateChanged();
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
            fireStateChanged();
        }
    }

    public Card takeFromDropZone(String name) {
        if (name == null) {
            return null;
        }

        for (int i = 0; i < dropZone.size(); i++) {
            Card c = dropZone.get(i);
            if (name.equals(c.getName())) {
                Card taken = dropZone.remove(i);
                fireStateChanged();
                return taken;
            }
        }
        return null;
    }

    public boolean isShielded(StarCard star) {
        return shieldedStars.contains(star);
    }

    public void addShield(StarCard star) {
        if (star != null && shieldedStars.add(star)) {
            fireStateChanged();
        }
    }

    public void removeShield(StarCard star) {
        if (shieldedStars.remove(star)) {
            fireStateChanged();
        }
    }

    public boolean isCloaked(StarCard star) {
        return cloakedStars.contains(star);
    }

    public void addCloak(StarCard star) {
        if (star != null && cloakedStars.add(star)) {
            fireStateChanged();
        }
    }

    public void clearAllBuffs() {
        if (shieldedStars.isEmpty() && cloakedStars.isEmpty()) {
            return;
        }

        shieldedStars.clear();
        cloakedStars.clear();
        fireStateChanged();
    }

    public void clearExpiredBuffs() {
        if (!cloakedStars.isEmpty()) {
            cloakedStars.clear();
            fireStateChanged();
        }
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
