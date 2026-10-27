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
        // ต้องเลือกการ์ดที่จะแลกจาก GUI แล้วเรียก playEffect ตัวที่มี giveCard
    }

    public void playEffectWithCard(Player owner, Player target, Card giveCard, GameState state)
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

        Card taken = target.getHand().get(RANDOM.nextInt(target.getHand().size()));
        target.removeCardFromHand(taken);
        owner.removeCardFromHand(giveCard);

        owner.addCardToHand(taken);
        target.addCardToHand(giveCard);
    }
}
