public class SpaceProbe extends SpecialCard 
{

    public SpaceProbe(String id, String imagePath) 
    {
        super(id, "Space Probe", SpecialCard.CATEGORY_SUPPORT, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        // TODO: ยังเขียนไม่ได้ — Deck ยังไม่มีเมธอด drawCard()
        // เมื่อมีแล้วจึงวนจั่ว 2 ใบด้วย owner.getDeck().drawCard() และ owner.addCardToHand(...)
    }
}
