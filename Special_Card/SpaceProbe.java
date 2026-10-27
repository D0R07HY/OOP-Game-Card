public class SpaceProbe extends SpecialCard 
{

    public SpaceProbe(String id, String imagePath) 
    {
        super(id, "Space Probe", SpecialCard.CATEGORY_SUPPORT, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null) 
            {
                return;
            }

        for (int i = 0; i < 2; i++) 
            {
                Card drawn = owner.getDeck().drawCard();

                if (drawn == null) 
                    {
                        break;
                    }

                owner.addCardToHand(drawn);
            }
    }
}
