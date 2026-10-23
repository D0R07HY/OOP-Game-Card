import java.util.ArrayList;
import java.util.List;

public class Player {
    private String username;
    private int actionPoints;
    private List<Card> hand;
    private StarCard[] field; // เปลี่ยนเป็น Array เพื่อเก็บดาวตามตำแหน่งวงโคจร
    private Deck deck;
    private boolean hasForceField;

    public Player(String username) {
        this.username = username;
        this.actionPoints = 2; // ตามกติกาของคุณเป้ กำหนด AP เริ่มต้นที่ 2
        this.field = new StarCard[9]; // จองพื้นที่ Array ไว้ 9 ช่อง (0-8)
        this.hand = new ArrayList<>();
        this.deck = new Deck();
        this.hasForceField = false;
    }

    public void resetActionPoints() {
        this.actionPoints = 2;
    }

    public boolean useActionPoint(int cost) {
        if (this.actionPoints >= cost) {
            this.actionPoints -= cost;
            return true;
        }
        return false;
    }

    public void setHasForceField(boolean hasForceField) {this.hasForceField = hasForceField;}

    public String getUsername() {
        return username;
    }

    public int getActionPoints() {
        return actionPoints;
    }

    public List<Card> getHand() {
        return hand;
    }

    public StarCard[] getField() {
        return field;
    }

    public Deck getDeck()                           {return deck;}

    public boolean hasForceField() {
        return hasForceField;
    }

    public void addCardToHand(Card c)               {hand.add(c);}

    public void removeCardFromHand(Card c)          {hand.remove(c);}

    // เมธอดสำหรับวางดาวลงช่องตาม orbitPosition ของดาว
    public boolean placeStar(StarCard star) {
        int pos = star.getOrbitPosition();
        if (field[pos] == null) {
            field[pos] = star;
            return true;
        }
        return false;
    }

    // เมธอดสำหรับลบดาวออกจาก Array
    public void removeStarFromField(StarCard star) {
        int pos = star.getOrbitPosition();
        if (pos >= 0 && pos < field.length && field[pos] == star) {
            field[pos] = null;
        }
    }

    // ตรวจสอบเงื่อนไขชนะ: ต้องมีดาวครบ 9 ดวง (Array ไม่เป็น null ทั้ง 9 ช่อง)
    public boolean checkWinCondition() {
        for (StarCard star : field) {
            if (star == null) {
                return false; // ถ้ามีช่องไหนว่างแม้แต่ช่องเดียว ถือว่ายังไม่ชนะ
            }
        }
        return true; // ช่องเต็มหมด แปลว่าชนะ
    }

    public boolean isHandOverLimit()                {return hand.size() > 7;}

    public void discardToLimit(Card c)              {hand.remove(c);}
}
