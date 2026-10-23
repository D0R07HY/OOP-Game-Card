public class ForceField extends SpecialCard 
{

    public ForceField(String id, String imagePath) 
    {
        super(id, "Force Field", SpecialCard.CATEGORY_DEFENSE, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        // TODO: ยังเขียนไม่ได้ — ต้องมีสถานะโล่รายดวงใน Player ก่อน
        // ตอนนี้ Player มีเพียง boolean hasForceField ซึ่งไม่รู้ว่าดาวดวงไหนมีโล่
    }
}
