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

        for (Player p : state.getPlayers()) 
            {
                for (StarCard star : p.getField()) 
                    {
                        if (star != null) 
                            {
                                p.removeStarFromField(star);
                                p.addToDropZone(star);
                            }
                    }
            }

        state.setBlackHoleUsed(true);
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_NONE;
    }

    @Override
    public String getPromptMessage() {
        return "Black Hole: ทำลายดาวทุกดวงบนสนาม";
    }
}
