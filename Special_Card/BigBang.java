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

        Card sun = owner.takeFromDropZone("The Sun");

        if (sun != null) 
            {
                if (sun instanceof StarCard) 
                    {
                        if (!owner.placeStar((StarCard) sun)) 
                            {
                                owner.addToDropZone(sun);
                            }
                    }
                else 
                    {
                        owner.addToDropZone(sun);
                    }
            }

        // TODO: ค้นหา "The Sun" จากกองการ์ดยังทำไม่ได้ เพราะ Deck ไม่มีเมธอดค้นหา

        for (StarCard star : owner.getField()) 
            {
                if (star != null) 
                    {
                        star.heal(star.getMaxHp());
                    }
            }
    }
}
