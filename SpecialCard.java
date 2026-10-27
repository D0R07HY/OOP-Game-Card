public abstract class SpecialCard extends Card {

    
    public static final String CATEGORY_ATTACK = "ATTACK";
    public static final String CATEGORY_DEFENSE = "DEFENSE";
    public static final String CATEGORY_SUPPORT = "SUPPORT";
    public static final String CATEGORY_SECRET = "SECRET";

    
    public static final String TARGET_ENEMY_STAR = "ENEMY_STAR";     
    public static final String TARGET_ALLY_STAR = "ALLY_STAR";       
    public static final String TARGET_ENEMY_PLAYER = "ENEMY_PLAYER"; 
    public static final String TARGET_NONE = "NONE";                 
    public static final String TARGET_COST_CARD = "COST_CARD";       

    private String category;

    public SpecialCard(String id, String name, String category, String imagePath) {
        
        super(id, name, "SPECIAL", imagePath);
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }

    
    public abstract String getTargetType();

    
    public abstract String getPromptMessage();
}
