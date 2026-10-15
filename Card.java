public abstract class Card 
{

    protected String id;
    protected String name;
    protected String type;

    protected Card(String id, String name, String type) 
    {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId()   {return id;}
    public String getName() { return name;}
    public String getType() {return type;}

    public abstract void playEffect(Player owner, Player target, GameState state);

}
