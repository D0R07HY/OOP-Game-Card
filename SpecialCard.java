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
                if (target == null || target.getField().isEmpty())
                    {
                        return;
                    }
                if(target.hasForceField() == true)
                    {
                        target.setHasForceField(false);
                    }
                    break;

            case "Supernova":
                if(target == null || target.getField().isEmpty())
                    {
                        return;
                    }            
                break;
            
            case "Pulsar":
                if(target == null || target.getField().isEmpty())
                    {
                        return;
                    }
                if(owner.getHand().isEmpty())
                    {
                        return;
                    }

                owner.removeCardFromHand(owner.getHand().get(0));
                target.removeStarFromField(target.getField().get(0));

                break;

            case "Force Field":
                if(owner.getField().isEmpty())
                    { 
                        return;       
                    }
                
                owner.setHasForceField(true);

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
