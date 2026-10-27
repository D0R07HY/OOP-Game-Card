import java.util.*;

public class Deck {
  private Stack<Card> cards;

  public Deck() {
        this.cards = new Stack<>();
        buildDeck();
        shuffle();
    }

  public void buildDeck(){
    cards.clear();

    addStarCopies("Mercury", 1, 0);
    addStarCopies("Venus", 2, 1);
    addStarCopies("Jupiter", 4, 2);
    addStarCopies("Earth", 5, 3);
    addStarCopies("Mars", 4, 4);
    addStarCopies("Saturn", 4, 5);
    addStarCopies("Uranus", 3, 6);
    addStarCopies("Neptune", 3, 7);
    addStarCopies("The Sun", 5, 8);

    addSpecialCopies("Meteor Strike", 2);
    addSpecialCopies("Supernova", 2);
    addSpecialCopies("Force Field", 2);
    addSpecialCopies("Nebula Cloak", 2);
    addSpecialCopies("Stellar Regeneration", 2);
    addSpecialCopies("Space Probe", 2);
    addSpecialCopies("Pulsar", 1);
    addSpecialCopies("Worm Hole", 1);
    addSpecialCopies("Big Bang", 1);
    addSpecialCopies("Black Hole", 1);
  }

  private void addStarCopies(String name, int hp, int orbit) {
    for (int i = 1; i <= 4; i++) {
      cards.push(new StarCard(idFor(name, i), name, hp, ImgPath.get(name), orbit));
    }
  }

  private void addSpecialCopies(String name, int copies) {
    for (int i = 1; i <= copies; i++) {
      Card card = CardFactory.create(idFor(name, i), name, "SPECIAL", 0, -1);

      if (card != null) {
        cards.push(card);
      }
    }
  }

  private static String idFor(String name, int number) {
    return name.replace(" ", "").toUpperCase() + "-" + number;
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
