public class StarCard extends Card {

    private int hp;
    private int maxHp;

    // เพิ่ม imagePath
    public StarCard(String id, String name, int hp, String imagePath) {
        super(id, name, "STAR", imagePath);
        this.maxHp = hp;
        this.hp = hp;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        this.hp -= amount;
        if (this.hp < 0) {
            this.hp = 0;
        }
    }

    public void heal(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        this.hp += amount;
        if (this.hp > maxHp) {
            this.hp = maxHp;
        }
    }

    public boolean isDestroyed() {
        return hp <= 0;
    }

    // อัปเดตพารามิเตอร์ให้ตรงกับคลาสแม่
    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        // วางดาวลงกระดานของตัวเอง ถ้าสนามยังไม่เต็ม 9 ดวง
        // (สำหรับการ์ดดาว target และ targetStar มักจะไม่ได้ใช้ เพราะมันคือการร่ายลงสนามตัวเอง)
        if (owner.getField().size() < 9) {
            owner.getField().add(this);
            System.out.println("[Effect] " + owner.getUsername() + " วางดาว " + this.getName() + " ลงบนสนาม");
        } else {
             System.out.println("[Effect] วางดาวไม่ได้ สนามของ " + owner.getUsername() + " เต็มแล้ว");
        }
    }
}
