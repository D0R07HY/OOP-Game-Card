public abstract class SpecialCard extends Card {

    public static final String CATEGORY_ATTACK = "ATTACK";
    public static final String CATEGORY_DEFENSE = "DEFENSE";
    public static final String CATEGORY_SUPPORT = "SUPPORT";
    public static final String CATEGORY_SECRET = "SECRET";

    private String category;

    public SpecialCard(String id, String name, String category) {
        super(id, name, "SPECIAL");
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }
}
