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
        if (owner == null || target == null) 
            {
                return;
            }

        if (owner.getHand().isEmpty() || target.getHand().isEmpty()) 
            {
                return;
            }

        Card taken = target.getHand().remove(RANDOM.nextInt(target.getHand().size()));
        Card given = owner.getHand().remove(0);

        owner.addCardToHand(taken);
        target.addCardToHand(given);
    }
}
