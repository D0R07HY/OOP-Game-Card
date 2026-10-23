public class BlackHole extends SpecialCard 
{

    public BlackHole(String id, String imagePath) 
    {
        super(id, "Black Hole", SpecialCard.CATEGORY_SECRET, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || state == null) 
            {
                return;
            }
        if (state.isBlackHoleUsed()) 
            {
                return;
            }

        // ทำลายดาวทุกดวงบนสนามของทุกคน รวมถึงผู้ใช้การ์ด
        for (Player p : state.getPlayers()) 
            {
                for (StarCard star : p.getField()) 
                    {
                        if (star != null) 
                            {
                                p.removeStarFromField(star);
                                // TODO: ย้าย star ลง Drop Zone (Player ยังไม่มี dropZone)
                            }
            }
        }

        state.setBlackHoleUsed(true);
    }
}
