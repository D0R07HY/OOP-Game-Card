public abstract class SpecialCard extends Card {

    public static final String CATEGORY_ATTACK = "ATTACK";
    public static final String CATEGORY_DEFENSE = "DEFENSE";
    public static final String CATEGORY_SUPPORT = "SUPPORT";
    public static final String CATEGORY_SECRET = "SECRET";

    private String category;

    // แก้ Constructor ให้รับข้อมูลตั้งต้นของการ์ด
    public SpecialCard(String id, String name, String category, String imagePath) {
        super(id, name, "SPECIAL", imagePath);
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }
}
