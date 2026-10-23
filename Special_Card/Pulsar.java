public class Pulsar extends SpecialCard 
{

    public Pulsar(String id, String imagePath) 
    {
        super(id, "Pulsar", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || target == null || targetStar == null) 
            {
                return;
            }

        int pos = targetStar.getOrbitPosition();
        if (pos < 0 || pos >= target.getField().length) 
            {
                return;
            }

        if (target.getField()[pos] != targetStar) 
            {
                return;
            }

        if (target.isCloaked(targetStar)) 
            {
                return;
            }

        if (owner.getHand().isEmpty()) 
            {
                return;
            }

        Card cost = owner.getHand().remove(0);
        owner.addToDropZone(cost);

        target.removeStarFromField(targetStar);
        target.addToDropZone(targetStar);
    }
}
