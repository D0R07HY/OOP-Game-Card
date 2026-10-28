public class BigBang extends SpecialCard {

    public BigBang(String id, String imagePath) {
        super(id, "Big Bang", SpecialCard.CATEGORY_SECRET, imagePath);
    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state) {
        if (owner == null) return;

        Card sun = owner.takeFromDropZone("The Sun");

        if (sun == null) {
            sun = owner.getDeck().searchAndRemove("The Sun");
        }

        if (sun != null) {
            if (sun instanceof StarCard) {
                StarCard sunStar = (StarCard) sun;
                if (!owner.placeStar(sunStar)) {
                    owner.addToDropZone(sun);
                }
            } else {
                owner.addToDropZone(sun);
            }
        }

        for (StarCard star : owner.getField()) {
            if (star != null) {
                star.heal(star.getMaxHp());
            }
        }
    }

    @Override
    public String getTargetType() {
        return SpecialCard.TARGET_NONE;
    }

    @Override
    public String getPromptMessage() {
        return "Big Bang: นำ The Sun ลงสนามช่องแรกและฟื้นฟู HP ดาวทุกดวง";
    }
}
