public class NebulaCloak extends SpecialCard 
{

    public NebulaCloak(String id, String imagePath) 
    {
        super(id, "Nebula Cloak", SpecialCard.CATEGORY_DEFENSE, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        // TODO: ยังเขียนไม่ได้ — ต้องมีสถานะการซ่อนรายดวงใน Player ก่อน
        // และต้องมีการเช็คสถานะนี้ในการ์ดโจมตีทุกใบ
    }
}
