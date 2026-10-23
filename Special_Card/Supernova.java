public class Supernova extends SpecialCard 
{

    public Supernova(String id, String imagePath) 
    {
        super(id, "Supernova", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || target == null) 
            {
                return;
            }

        // TODO: ยังไม่รองรับ Force Field / Nebula Cloak เพราะ Player ยังไม่มีสถานะรายดวง

        for (StarCard star : target.getField()) 
            {
                if (star == null) 
                    {
                        continue;
                    }

            star.takeDamage(1);

            if (star.isDestroyed()) 
                {
                    target.removeStarFromField(star);
                    // TODO: ย้าย star ลง Drop Zone (Player ยังไม่มี dropZone)
                }
        }
    }
}
