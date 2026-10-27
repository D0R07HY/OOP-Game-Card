public class TargetingManager {

    public enum PendingMode {
        NONE, ENEMY_STAR, ALLY_STAR, ENEMY_PLAYER, COST_CARD
    }

    private Card pendingCard;
    private PendingMode pendingMode;
    private StarCard pendingStar;
    private Player pendingTarget;

    public TargetingManager() {
        reset();
    }

    // เริ่มต้นกระบวนการเล็งเป้าหมาย
    public void begin(Card card, PendingMode mode) {
        this.pendingCard = card;
        this.pendingMode = mode;
        this.pendingStar = null;
        this.pendingTarget = null;
    }

    // ล้างสถานะการเล็งเป้าหมายทั้งหมด (ยกเลิก)
    public void reset() {
        this.pendingCard = null;
        this.pendingMode = PendingMode.NONE;
        this.pendingStar = null;
        this.pendingTarget = null;
    }

    // ตรวจสอบว่ากำลังอยู่ในโหมดเล็งเป้าหมายอยู่หรือไม่
    public boolean isPending() {
        return pendingMode != PendingMode.NONE;
    }

    // Getters และ Setters
    public Card getPendingCard() {
        return pendingCard;
    }

    public void setPendingCard(Card pendingCard) {
        this.pendingCard = pendingCard;
    }

    public PendingMode getPendingMode() {
        return pendingMode;
    }

    public void setPendingMode(PendingMode pendingMode) {
        this.pendingMode = pendingMode;
    }

    public StarCard getPendingStar() {
        return pendingStar;
    }

    public void setPendingStar(StarCard pendingStar) {
        this.pendingStar = pendingStar;
    }

    public Player getPendingTarget() {
        return pendingTarget;
    }

    public void setPendingTarget(Player pendingTarget) {
        this.pendingTarget = pendingTarget;
    }
}
