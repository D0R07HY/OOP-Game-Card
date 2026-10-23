import java.util.*;

public class Player {
    private String username;
    private int actionPoints;
    private List<Card> hand;
    private List<StarCard> field;
    private Deck deck;
    private boolean hasForceField;

    public Player(String username)
    {
        this.username = username;
        this.actionPoints = 2;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
        this.deck = new Deck();
        this.hasForceField = false;
    }

    public void resetActionPoints() {
        this.actionPoints = 2;
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

    public String getUsername()
    {
        return username;
    }

    public int getActionPoints()
    {
        return actionPoints;
    }

    public List<Card> getHand()
    {
        return hand;
    }

    public List<StarCard> getField()
    {
        return field;
    }

    public Deck getDeck() {
        return deck;
    }

    public boolean hasForceField(){
        return hasForceField;
    }

    public void addCardToHand(Card c) {
        hand.add(c);
    }

    public void removeCardFromHand(Card c) {
        hand.remove(c);
    }

    public void addStarToField(StarCard star) {
        field.add(star);
    }

    public void removeStarFromField(StarCard star) {
        field.remove(star);
    }

    public boolean checkWinCondition() {
        return field.size() >= 9;
    }

    public boolean isHandOverLimit() {
        return hand.size() > 7;
    }

    public void discardToLimit(Card c) {
        hand.remove(c);
    }
}
