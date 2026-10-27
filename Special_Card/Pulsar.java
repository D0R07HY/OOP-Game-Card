public class Pulsar extends SpecialCard 
{

    public Pulsar(String id, String imagePath) 
    {
        super(id, "Pulsar", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || owner.getHand().isEmpty()) 
            {
                return;
            }

        playEffect(owner, target, targetStar, owner.getHand().get(0), state);
    }

    public void playEffect(Player owner, Player target, StarCard targetStar, Card cost, GameState state) 
    {
        if (owner == null || target == null || targetStar == null || cost == null) 
            {
                return;
            }

        if (owner.hasUsedPulsarThisTurn()) 
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

        if (!owner.getHand().contains(cost)) 
            {
                return;
            }

        owner.getHand().remove(cost);
        owner.addToDropZone(cost);

        target.removeStarFromField(targetStar);
        target.addToDropZone(targetStar);
        owner.markPulsarUsed();
    }
}
