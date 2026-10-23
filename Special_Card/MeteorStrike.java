public class MeteorStrike extends SpecialCard 
{

    public MeteorStrike(String id, String imagePath) 
    {
        super(id, "Meteor Strike", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        if (owner == null || target == null || targetStar == null) 
            {
                return;
            }

        int pos = targetStar.getOrbitPosition();
        if (pos < 0 || pos >= target.getField().length) 
            {
                return;
            }
        if (target.getField()[pos] != targetStar) 
            {
                return;
            }

        targetStar.takeDamage(2);

        if (targetStar.isDestroyed()) 
            {
                target.removeStarFromField(targetStar);
                // TODO: ย้าย targetStar ลง Drop Zone (Player ยังไม่มี dropZone)
            }
    }
}
