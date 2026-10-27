import java.util.*;

public class Deck {
  private Stack<Card> cards;

  public Deck() {
        this.cards = new Stack<>();
        buildDeck();
        shuffle();
    }

  public void buildDeck(){
    addStars("SUN",     "The Sun", 3, ImgPath.The_Sun, 2, 8);
    addStars("MERCURY", "Mercury", 3, ImgPath.Mercury, 3, 1);
    addStars("VENUS",   "Venus",   2, ImgPath.Venus,   3, 2);
    addStars("EARTH",   "Earth",   2, ImgPath.Earth,   4, 3);
    addStars("MARS",    "Mars",    3, ImgPath.Mars,    4, 4);
    addStars("JUPITER", "Jupiter", 1, ImgPath.Jupiter, 2, 5);
    addStars("SATURN",  "Saturn",  1, ImgPath.Saturn,  2, 6);
    addStars("URANUS",  "Uranus",  1, ImgPath.Uranus,  2, 7);
    addStars("NEPTUNE", "Neptune", 2, ImgPath.Neptune, 2, 0);


    for (int i = 1; i <= 4; i++)
      cards.push(new MeteorStrike("SP-METEOR-" + i, ImgPath.Meteor_Strike));
    for (int i = 1; i <= 4; i++)
      cards.push(new Supernova("SP-SUPERNOVA-" + i, ImgPath.Super_nova));
    for (int i = 1; i <= 3; i++)
      cards.push(new Pulsar("SP-PULSAR-" + i, ImgPath.pulsar));
    for (int i = 1; i <= 3; i++)
      cards.push(new ForceField("SP-FORCE-" + i, ImgPath.forc_field));
    for (int i = 1; i <= 3; i++)
      cards.push(new NebulaCloak("SP-CLOAK-" + i, ImgPath.nebular_cloak));
    for (int i = 1; i <= 3; i++)
      cards.push(new StellarRegeneration("SP-REGEN-" + i, ImgPath.stallar_regenneration));
    for (int i = 1; i <= 4; i++)
      cards.push(new SpaceProbe("SP-PROBE-" + i, ImgPath.space_prode));
    for (int i = 1; i <= 1; i++)
      cards.push(new WormHole("SP-WORM-" + i, ImgPath.worm_hole));
    for (int i = 1; i <= 1; i++)
      cards.push(new BigBang("SP-BIGBANG-" + i, ImgPath.Bigbang));

    cards.push(new BlackHole("SP-BLACKHOLE-1", ImgPath.Black_hole));
  }

  
  private void addStars(String code, String name, int hp, String image, int count, int orbit) {
    for (int i = 1; i <= count; i++) {
      cards.push(new StarCard(code + "-" + i, name, hp, image, orbit));
    }
  }

  public void shuffle(){
    if (this.cards != null && !this.cards.isEmpty())
      Collections.shuffle(this.cards);
  }

  public boolean isEmpty() {
    return cards.isEmpty();
  }

  public int size() {
    return cards.size();
  }

  public Card drawCard() {
    if (cards.isEmpty()) {
      return null;
    }
    return cards.pop();
  }

  public Card searchAndRemove(String name) {
    if (name == null) {
      return null;
    }
    for (int i = 0; i < cards.size(); i++) {
      if (name.equals(cards.get(i).getName())) {
        return cards.remove(i);
      }
    }
    return null;
  }
}
