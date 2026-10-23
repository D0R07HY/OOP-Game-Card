public class StarCard extends Card {

    private int hp;
    private int maxHp;

    public StarCard(String id, String name, int hp) {
        super(id, name, "STAR");
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

    @Override
    public void playEffect(Player owner, Player target, GameState state) {
        // วางดาวลงกระดาน ถ้าสนามยังไม่เต็ม 9 ดวง
        if (owner.getField().size() < 9) {
            owner.getField().add(this);
        }
    }
}
