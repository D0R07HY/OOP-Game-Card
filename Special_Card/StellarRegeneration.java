public class StellarRegeneration extends SpecialCard 
{

    public StellarRegeneration(String id, String imagePath) 
    {
        super(id, "Stellar Regeneration", SpecialCard.CATEGORY_DEFENSE, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || targetStar == null) 
            {
                return;
            }

        int pos = targetStar.getOrbitPosition();
        if (pos < 0 || pos >= owner.getField().length) 
            {
                return;
            }

        if (owner.getField()[pos] != targetStar) 
            {
                return;
            }

        targetStar.heal(3);
    }
}
