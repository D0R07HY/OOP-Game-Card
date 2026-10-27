public class ForceField extends SpecialCard 
{

    public ForceField(String id, String imagePath) 
    {
        super(id, "Force Field", SpecialCard.CATEGORY_DEFENSE, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || targetStar == null) 
            {
                return;
            }

        int pos = targetStar.getOrbitPosition();
        if (pos < 0 || pos >= owner.getField().length) 
            {
                return;
            }

        if (owner.getField()[pos] != targetStar) 
            {
                return;
            }

        owner.addShield(targetStar);
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_ALLY_STAR;
    }

    @Override
    public String getPromptMessage() {
        return "Force Field: เลือกดาวของตนเองเพื่อติดโล่ 1 ครั้ง";
    }
}
