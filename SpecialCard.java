public class SpecialCard extends Card
{
    
    public static final String category_attack = "ATTACK";
    public static final String category_defense = "DEFENSE";
    public static final String category_support = "SUPPORT";
    public static final String category_secret = "SECRET";


    private String category;

    public SpecialCard(String id, String name, String category)
    {
        super(id , name,"SPECIAL");
        this.category = category;
    }

    public String getCategory() {return this.category;}

    @Override
    public void playEffect(Player owner, Player target, GameState state) 
    {
        if(owner == null)
        {
            return ;
        }

        switch (name)
        {
            case "Meteor Strike":

                break;

            case "Supernova":

                break;

            case "Pulsar":

                break;

            case "Force Field":

                break;
            
            case "Nebula Cloak":

                break;

            case "Stellar Regeneration":

                break;

            case "Space Probe":
                
                break;

            case "Worm Hole":

                break;

            case "Big Bang":

                break;

            case "Black Hole":

                break;

            default:

                break;
        }

    }

}
