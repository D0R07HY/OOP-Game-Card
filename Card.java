abstract class Card 
{

    protected String id;
    protected String name;
    protected String type;

    public abstract void playEffect(Player owner, Player target, GameState state);

}
