public class MeteorStrike extends SpecialCard {

    // กำหนดชื่อ "Meteor Strike" และประเภทหมวดหมู่ ATTACK ไว้ตายตัว
    public MeteorStrike(String id, String imagePath) {
        super(id, "Meteor Strike", SpecialCard.CATEGORY_ATTACK, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        // 1. ตรวจสอบความถูกต้องของเป้าหมาย
        if (target == null || targetStar == null) {
            System.out.println("[Meteor Strike] การร่ายเวทล้มเหลว: ไม่พบผู้เล่นหรือดาวเป้าหมาย");
            return;
        }

        // 2. แสดงข้อความ Log ในฝั่งเซิร์ฟเวอร์
        System.out.println("[Effect] " + owner.getUsername() + " ใช้ Meteor Strike โจมตี " + targetStar.getName() + " ของ " + target.getUsername());

        // 3. ทำความเสียหาย 2 หน่วย (อ้างอิงจากเมธอดที่เราเขียนไว้ใน StarCard)
        targetStar.takeDamage(2);
        System.out.println("-> HP ของ " + targetStar.getName() + " เหลือ " + targetStar.getHp() + "/" + targetStar.getMaxHp());

        // 4. เช็คว่าดาวถูกทำลายหรือไม่ ถ้าใช่ ให้ลบออกจากกระดานเป้าหมาย
        if (targetStar.isDestroyed()) {
            target.removeStarFromField(targetStar);
            System.out.println("-> " + targetStar.getName() + " ถูกทำลายระเบิดทิ้งออกจากสนาม!");
        }
    }
}
