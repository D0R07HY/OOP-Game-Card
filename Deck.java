import java.util.*;

/**
 * [แก้] เด็คส่วนตัว 52 ใบต่อผู้เล่น ตามกติกา
 *
 * สัดส่วน: ดาว 34 ใบ + Special 18 ใบ
 * - ดาว 34 ใบ (~65%) ทำให้จั่วดาวได้ต่อเนื่องและยังมีเหลือหลังถูกทำลาย
 * - Attack 7 / Defense 6 / Support 3 / Secret 2 = 18 ใบ (~35%)
 *   ให้มีการโต้ตอบแต่ไม่ท่วมมือ
 * - จำนวนดาวแต่ละชนิดอิงความหายากในเอกสาร:
 *   The Sun 2, Jupiter/Saturn 3, Uranus/Neptune 4, Earth/Venus 4, Mars/Mercury 5
 */
public class Deck {
  private Stack<Card> cards;

  public Deck() {
        this.cards = new Stack<>();
        buildDeck();
        shuffle();
    }

  public void buildDeck(){
    // ---- Star Cards: 34 ใบ ----
    // เดิมมีแค่ The Sun 1 ใบ + Earth 4 ใบ ทำให้วางดาวครบ 9 ช่องไม่ได้
    addStars("SUN",     "The Sun", 5, ImgPath.The_Sun, 2, 8);
    addStars("MERCURY", "Mercury", 1, ImgPath.Mercury, 5, 1);
    addStars("VENUS",   "Venus",   2, ImgPath.Venus,   4, 2);
    addStars("EARTH",   "Earth",   2, ImgPath.Earth,   4, 3);
    addStars("MARS",    "Mars",    1, ImgPath.Mars,    5, 4);
    addStars("JUPITER", "Jupiter", 4, ImgPath.Jupiter, 3, 5);
    addStars("SATURN",  "Saturn",  4, ImgPath.Saturn,  3, 6);
    addStars("URANUS",  "Uranus",  3, ImgPath.Uranus,  4, 7);
    addStars("NEPTUNE", "Neptune", 3, ImgPath.Neptune, 4, 0);

    // ---- Special Cards: 18 ใบ ----
    // 7 Attack / 6 Defense / 3 Support / 2 Secret
    for (int i = 1; i <= 3; i++)
      cards.push(new MeteorStrike("SP-METEOR-" + i, ImgPath.Meteor_Strike));
    for (int i = 1; i <= 2; i++)
      cards.push(new Supernova("SP-SUPERNOVA-" + i, ImgPath.Super_nova));
    for (int i = 1; i <= 2; i++)
      cards.push(new Pulsar("SP-PULSAR-" + i, ImgPath.pulsar));
    for (int i = 1; i <= 2; i++)
      cards.push(new ForceField("SP-FORCE-" + i, ImgPath.forc_field));
    for (int i = 1; i <= 2; i++)
      cards.push(new NebulaCloak("SP-CLOAK-" + i, ImgPath.nebular_cloak));
    for (int i = 1; i <= 2; i++)
      cards.push(new StellarRegeneration("SP-REGEN-" + i, ImgPath.stallar_regenneration));
    for (int i = 1; i <= 2; i++)
      cards.push(new SpaceProbe("SP-PROBE-" + i, ImgPath.space_prode));
    cards.push(new WormHole("SP-WORM-1", ImgPath.worm_hole));
    cards.push(new BigBang("SP-BIGBANG-1", ImgPath.Bigbang));
    cards.push(new BlackHole("SP-BLACKHOLE-1", ImgPath.Black_hole));
  }

  // helper: เพิ่มดาวชนิดเดียวกันหลายใบ แต่ id ต้องไม่ซ้ำเพราะ client ใช้ id สั่ง PLAY
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
