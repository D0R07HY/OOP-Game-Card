public class Supernova extends SpecialCard 
{

    public Supernova(String id, String imagePath) 
    {
        super(id, "Supernova", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || target == null) 
            {
                return;
            }

        for (StarCard star : target.getField()) 
            {
                if (star == null) 
                    {
                        continue;
                    }

                if (target.isCloaked(star)) 
                    {
                        continue;
                    }

                if (target.isShielded(star)) 
                    {
                        target.removeShield(star);
                        continue;
                    }

                star.takeDamage(1);

                if (star.isDestroyed()) 
                    {
                        target.removeStarFromField(star);
                        target.addToDropZone(star);
                    }
            }
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_ENEMY_PLAYER;
    }

    @Override
    public String getPromptMessage() {
        return "Supernova: เลือกผู้เล่นเป้าหมาย (ดาเมจทุกดาว 1)";
    }
}
