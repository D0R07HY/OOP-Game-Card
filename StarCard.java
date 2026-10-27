public class StarCard extends Card {

    private int hp;
    private int maxHp;
    private int orbitPosition;
    private Runnable changeListener;

    // เพิ่ม imagePath
    public StarCard(String id, String name, int hp, String imagePath, int orbitPosition) {
        super(id, name, "STAR", imagePath);
        this.maxHp = hp;
        this.hp = hp;
        this.orbitPosition = orbitPosition;
        this.changeListener = null;
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

    public void setOrbitPosition(int orbitPosition) {
        this.orbitPosition = orbitPosition;
    }

    void setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
    }

    private void fireStateChanged() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        if (amount == 0) {
            return;
        }
        this.hp -= amount;
        if (this.hp < 0) {
            this.hp = 0;
        }
        fireStateChanged();
    }

    public void heal(int amount) {
        if (amount < 0) {
            amount = 0;
        }
        if (amount == 0) {
            return;
        }
        this.hp += amount;
        if (this.hp > maxHp) {
            this.hp = maxHp;
        }
        fireStateChanged();
    }

    public boolean isDestroyed() {
        return hp <= 0;
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        if (owner.placeStar(this)) {
            System.out.println("[Effect] " + owner.getUsername() + " placed " + this.getName() + " at orbit " + orbitPosition);
        } else {
            System.out.println("[Effect] Failed! The orbit position for " + this.getName() + " is already occupied");
        }
    }
}
