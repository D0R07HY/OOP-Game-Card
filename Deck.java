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
}
