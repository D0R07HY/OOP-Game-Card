public class SpaceProbe extends SpecialCard 
{

    public SpaceProbe(String id, String imagePath) 
    {
        super(id, "Space Probe", SpecialCard.CATEGORY_SUPPORT, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null) 
            {
                return;
            }

        for (int i = 0; i < 2; i++) 
            {
                Card drawn = owner.getDeck().drawCard();

                if (drawn == null) 
                    {
                        break;
                    }

                owner.addCardToHand(drawn);
            }
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_NONE;
    }

    @Override
    public String getPromptMessage() {
        return "Space Probe: จั่วการ์ด 2 ใบ";
    }
}
