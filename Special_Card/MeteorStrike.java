package Special_Card;
import GameState;
import Player;
import SpecialCard;
import StarCard;


public class MeteorStrike extends SpecialCard {

    // กำหนดชื่อ "Meteor Strike" และประเภทหมวดหมู่ ATTACK ไว้ตายตัว
    public MeteorStrike(String id, String imagePath) {
        super(id, "Meteor Strike", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        // 1. ตรวจสอบความถูกต้องของเป้าหมาย
        if (target == null || targetStar == null)
            return;

        targetStar.takeDamage(2);

        if (targetStar.isDestroyed())
            target.removeStarFromField(targetStar);
    }
}
