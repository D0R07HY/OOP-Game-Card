public class NebulaCloak extends SpecialCard 
{

    public NebulaCloak(String id, String imagePath) 
    {
        super(id, "Nebula Cloak", SpecialCard.CATEGORY_DEFENSE, imagePath);
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

        owner.addCloak(targetStar);
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_ALLY_STAR;
    }

    @Override
    public String getPromptMessage() {
        return "Nebula Cloak: เลือกดาวของตนเองเพื่อซ่อนจากเป้าหมายโจมตี";
    }
}
