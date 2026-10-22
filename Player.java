import java.util.ArrayList;

public class Player {
    private String username;
    private int actionPoints;
    private ArrayList<Card> hand;
    private ArrayList<StarCard> field;
    private boolean hasForceField;

    public Player(String username)
    {
        this.username = username;
        this.actionPoints = 2;
        this.hand = new ArrayList<>();
        this.field = new ArrayList<>();
        this.hasForceField = false;
    }

    public String getUsername()
    {
        return username;
    }

    public int getActionPoints()
    {
        return actionPoints;
    }

    public ArrayList<Card> getHand()
    {
        return hand;
    }

    public ArrayList<StarCard> getField()
    {
        return field;
    }

    public boolean hasForceField()
    {
        return hasForceField;
    }

    public void addCardToHand(Card c)
    {
        hand.add(c);
    }

    public void removeCardFromHand(Card c)
    {
        hand.remove(c);
    }

    public boolean checkWinCondition()
    {
        return field.size() == 9;
    }

    public boolean isHandOverLimit()
    {
        return hand.size() > 7;
    }

    public void discardToLimit(Card c)
    {
        hand.remove(c);
    }

}
