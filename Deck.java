import java.util.*;
public class Deck {
  private Stack<Card> cards;

  public Deck() {
        this.cards = new Stack<>();
        buildDeck();
        shuffle();
    }

  public void buildDeck(){
    for(int i = 1; i<=4; i++)
      cards.push(new StarCard("S03","Earth", 5,ImgPath.Earth,3));
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
