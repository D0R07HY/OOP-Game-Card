public class StarCard extends Card {

    private int hp;
    private int maxHp;
    private int orbitPosition;

    // เพิ่ม imagePath
    public StarCard(String id, String name, int hp, String imagePath, int orbitPosition) {
        super(id, name, "STAR", imagePath);
        this.maxHp = hp;
        this.hp = hp;
        this.orbitPosition = orbitPosition;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getOrbitPosition(){
        return this.orbitPosition;
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

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        // โยนหน้าที่การจัดวางไปให้ Player เป็นคนจัดการ เพื่อเช็คว่าช่องนั้นว่างหรือไม่
        if (owner.placeStar(this)) {
            System.out.println("[Effect] " + owner.getUsername() + " วางดาว " + this.getName() + " ลงในตำแหน่งวงโคจรที่ " + orbitPosition);
        } else {
            System.out.println("[Effect] ร่ายไม่สำเร็จ! ตำแหน่งวงโคจรของ " + this.getName() + " มีดาวอยู่แล้ว");
        }
    }
}
