public abstract class Card {

    protected String id;
    protected String name;
    protected String type;
    protected String imagePath; // เพิ่มตัวแปรเก็บ path รูปภาพ

    protected Card(String id, String name, String type, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.imagePath = imagePath;
    }

    public String getId()   { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getImagePath() { return imagePath; } // เพิ่ม Getter

    public abstract void playEffect(Player owner, Player target, StarCard targetStar, GameState state);
}
