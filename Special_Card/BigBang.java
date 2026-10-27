public class BigBang extends SpecialCard
{
    public BigBang(String id, String imagePath)
    {
        super(id, "Big Bang", SpecialCard.CATEGORY_SECRET, imagePath);

    }

    @Override
    public void playEffect(Player owner, Player target, StarCard targetStar, GameState state)
    {
        if (owner == null)
            {
                return;
            }

        Card sun = owner.takeFromDropZone("The Sun");

        if (sun == null)
            {
                sun = owner.getDeck().searchAndRemove("The Sun");
            }

        if (sun != null)
            {
                if (sun instanceof StarCard)
                    {
                        StarCard sunStar = (StarCard) sun;
                        StarCard[] field = owner.getField();
                        int emptyPos = -1;

                        for (int i = 0; i < field.length; i++)
                            {
                                if (field[i] == null)
                                    {
                                        emptyPos = i;
                                        break;
                                    }
                            }

                        if (emptyPos < 0)
                            {
                                owner.addToDropZone(sun);
                            }
                        else
                            {
                                sunStar.setOrbitPosition(emptyPos);

                                if (!owner.placeStar(sunStar))
                                    {
                                        owner.addToDropZone(sun);
                                    }
                            }
                    }
                else
                    {
                        owner.addToDropZone(sun);
                    }
            }

        for (StarCard star : owner.getField())
            {
                if (star != null)
                    {
                        star.heal(star.getMaxHp());
                    }
            }
    }
}
