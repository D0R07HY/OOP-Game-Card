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

        // สุ่มการ์ดจากมือเป้าหมายก่อน แล้วค่อยสลับ เพื่อไม่ให้หยิบใบที่เพิ่งให้ไป
        Card taken = target.getHand().remove(RANDOM.nextInt(target.getHand().size()));

        // TODO: ยังไม่มีพารามิเตอร์บอกว่าจะให้การ์ดใบไหน จึงใช้ใบแรกไปก่อน
        Card given = owner.getHand().remove(0);

        owner.addCardToHand(taken);
        target.addCardToHand(given);
    }
}
