public abstract class SpecialCard extends Card {

    // หมวดหมู่ของการ์ด (โค้ดเดิม)
    public static final String CATEGORY_ATTACK = "ATTACK";
    public static final String CATEGORY_DEFENSE = "DEFENSE";
    public static final String CATEGORY_SUPPORT = "SUPPORT";
    public static final String CATEGORY_SECRET = "SECRET";

    // ประเภทเป้าหมาย (เราสร้างตัวแปร String มารับค่าไว้ เพื่อจะได้เรียกใช้แบบพิมพ์ไม่ผิดพลาด)
    public static final String TARGET_ENEMY_STAR = "ENEMY_STAR";     // เป้าหมายคือดาวศัตรู
    public static final String TARGET_ALLY_STAR = "ALLY_STAR";       // เป้าหมายคือดาวตนเอง
    public static final String TARGET_ENEMY_PLAYER = "ENEMY_PLAYER"; // เป้าหมายคือตัวผู้เล่นศัตรู
    public static final String TARGET_NONE = "NONE";                 // ไม่ต้องการเป้าหมาย (เล่นได้เลย)
    public static final String TARGET_COST_CARD = "COST_CARD";       // เป้าหมายคือการ์ดในมือเพื่อทิ้ง

    private String category;

    public SpecialCard(String id, String name, String category, String imagePath) {
        // ส่งชนิด "SPECIAL" ไปให้คลาสแม่ (Card)
        super(id, name, "SPECIAL", imagePath);
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }

    // ---------------------------------------------------------
    // 2 เมธอดใหม่ (Abstract) ที่บังคับให้การ์ดพิเศษทุกใบต้องมี!
    // ---------------------------------------------------------

    // 1. ให้การ์ดบอกว่าตัวเองต้องการเป้าหมายประเภทไหน
    public abstract String getTargetType();

    // 2. ให้การ์ดส่งข้อความบอกผู้เล่นว่าต้องทำอะไร
    public abstract String getPromptMessage();
}
