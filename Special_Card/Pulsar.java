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

        if (owner.getHand().isEmpty()) 
            {
                return;
            }

        // Cost: ทิ้งการ์ด 1 ใบจากมือ
        Card cost = owner.getHand().remove(0);
        // TODO: ย้าย cost ลง Drop Zone (Player ยังไม่มี dropZone)

        // ทำลายดาวทันทีโดยไม่สน HP ที่เหลือ
        target.removeStarFromField(targetStar);
        // TODO: ย้าย targetStar ลง Drop Zone (Player ยังไม่มี dropZone)
    }
}
