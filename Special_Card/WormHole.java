import java.util.Random;

public class WormHole extends SpecialCard 
{

    private static final Random RANDOM = new Random();

    public WormHole(String id, String imagePath) 
    {
        super(id, "Worm Hole", SpecialCard.CATEGORY_SUPPORT, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || owner.getHand().isEmpty()) 
            {
                return;
            }

        playEffect(owner, target, owner.getHand().get(0), state);
    }

    public void playEffect(Player owner, Player target, Card giveCard, GameState state) 
    {
        if (owner == null || target == null || giveCard == null) 
            {
                return;
            }

        if (owner == target) 
            {
                return;
            }

        if (!owner.getHand().contains(giveCard)) 
            {
                return;
            }

        if (target.getHand().isEmpty()) 
            {
                return;
            }

        Card taken = target.getHand().remove(RANDOM.nextInt(target.getHand().size()));
        owner.getHand().remove(giveCard);

        owner.addCardToHand(taken);
        target.addCardToHand(giveCard);
    }
}
