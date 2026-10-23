import java.util.*;

public class Player {

    public static final int MAX_FIELD = 9;
    public static final int MAX_HAND = 7;
    public static final int ACTION_POINTS_PER_TURN = 2;

    private String username;
    private int actionPoints;
    private List<Card> hand;
    private List<StarCard> field;
    private List<Card> dropZone;
    private Deck deck;
    private Set<StarCard> shieldedStars;
    private Set<StarCard> cloakedStars;

    public Player(String username) {
        this.username = username;
        this.actionPoints = ACTION_POINTS_PER_TURN;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
        this.dropZone = new ArrayList<>();
        this.deck = new Deck();
        this.shieldedStars = new HashSet<>();
        this.cloakedStars = new HashSet<>();
    }

    public void resetActionPoints() {
        this.actionPoints = ACTION_POINTS_PER_TURN;
    }

    public boolean useActionPoint(int cost) {
        if (cost < 0 || this.actionPoints < cost) {
            return false;
        }
        this.actionPoints -= cost;
        return true;
    }

    public int getActionPoints()                    { return actionPoints; }

    public String getUsername()                     { return username; }

    public List<Card> getHand()                     { return hand; }

    public List<StarCard> getField()                { return field; }

    public List<Card> getDropZone()                 { return dropZone; }

    public Deck getDeck()                           { return deck; }

    public void addCardToHand(Card c) {
        if (c != null) {
            hand.add(c);
        }
    }

    public boolean removeCardFromHand(Card c)       { return hand.remove(c); }

    public boolean isHandOverLimit()                { return hand.size() > MAX_HAND; }

    public void discardToLimit(Card c) {
        if (hand.remove(c)) {
            addToDropZone(c);
        }
    }

    public boolean addStarToField(StarCard star) {
        if (star == null || field.size() >= MAX_FIELD) {
            return false;
        }
        field.add(star);
        return true;
    }

    public void removeStarFromField(StarCard star) {
        field.remove(star);
        shieldedStars.remove(star);
        cloakedStars.remove(star);
    }

    public boolean checkWinCondition()              { return field.size() >= MAX_FIELD; }

    public void addToDropZone(Card c) {
        if (c != null) {
            dropZone.add(c);
        }
    }

    public Card takeFromDropZone(String name) {
        for (Card c : dropZone) {
            if (c.getName().equals(name)) {
                dropZone.remove(c);
                return c;
            }
        }
        return null;
    }

    public boolean isShielded(StarCard star)        { return shieldedStars.contains(star); }

    public void addShield(StarCard star) {
        if (star != null) {
            shieldedStars.add(star);
        }
    }

    public void removeShield(StarCard star)         { shieldedStars.remove(star); }

    public boolean isCloaked(StarCard star)         { return cloakedStars.contains(star); }

    public void addCloak(StarCard star) {
        if (star != null) {
            cloakedStars.add(star);
        }
    }

    public void clearExpiredBuffs()                 { cloakedStars.clear(); }

    public boolean isProtected(StarCard star)       { return isShielded(star) || isCloaked(star); }

    public List<StarCard> getTargetableStars() {
        List<StarCard> result = new ArrayList<>();
        for (StarCard s : field) {
            if (!isCloaked(s)) {
                result.add(s);
            }
        }
        return result;
    }
}