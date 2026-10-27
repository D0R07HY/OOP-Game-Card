public class Pulsar extends SpecialCard 
{

    public Pulsar(String id, String imagePath) 
    {
        super(id, "Pulsar", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) 
    {
        // ต้องเลือกการ์ดที่จะทิ้งจาก GUI แล้วเรียก playEffect ตัวที่มี cost
    }

    public void playEffect(Player owner, Player target, StarCard targetStar, Card cost, GameState state) 
    {
        if (owner == null || target == null || targetStar == null || cost == null) 
            {
                return;
            }

        if (owner.isPulsarUsedThisTurn())
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

        if (target.isCloaked(targetStar)) 
            {
                return;
            }

        if (!owner.getHand().contains(cost)) 
            {
                return;
            }

        if (!owner.markPulsarUsed())
            {
                return;
            }

        owner.removeCardFromHand(cost);
        owner.addToDropZone(cost);

        target.removeStarFromField(targetStar);
        target.addToDropZone(targetStar);
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_ENEMY_STAR;
    }

    @Override
    public String getPromptMessage() {
        return "Pulsar: เลือกดาวคู่แข่งที่จะทำลาย";
    }
}
