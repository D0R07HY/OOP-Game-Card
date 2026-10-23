public class BigBang extends SpecialCard 
{

    public BigBang(String id, String imagePath) 
    {
        super(id, "Big Bang", SpecialCard.CATEGORY_SECRET, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null) 
            {
                return;
            }

        // TODO: ค้นหา "The Sun" จากกองการ์ดหรือ Drop Zone แล้ววางลงสนามฟรี
        // ยังเขียนไม่ได้เพราะ Deck ไม่มีเมธอดค้นหา และ Player ไม่มี dropZone

        for (StarCard star : owner.getField()) 
            {
                if (star != null) 
                    {
                        star.heal(star.getMaxHp());
                    }
        }
    }
}
