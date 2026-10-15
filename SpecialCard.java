public class SpecialCard extends Card
{
    
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
        
    }

}
