+ class Deck 

—-----------------------------  
private Stack\<Card\> cards   
—------------------------------  
`public void buildDeck()` (เมธอดสำหรับยัดไพ่ทั้ง 52 ใบเตรียมไว้ตอนเริ่มเกม)  
`public void shuffle()` (ใช้ `Collections.shuffle(cards)` เพื่อสับไพ่)  
`public Card drawCard()` (ดึงไพ่ใบบนสุดออกแจก ถ้ากองหมด `cards.isEmpty()` จะโยน Exception หรือส่งสัญญาณว่าผู้เล่นคนนี้แพ้แบบ Deck Out)

- Pae

\++++++

abstract class Card   
—-----------------------------

* `protected String id` (เช่น "C01")  
* `protected String name` (เช่น "Meteor Strike")  
* `protected String type` (เช่น "SPECIAL", "STAR")

—------------------------------

public abstract void playEffect(Player owner, Player target, GameState state) 

- IT

\++++++

class StarCard extends Card 

—-------------------------------------

`private int hp`

`private int maxHp`

—--------------------------------------

* `public void takeDamage(int amount)`  
  * `public void heal(int amount)`  
  * `public boolean isDestroyed()` (เช็คว่า HP \<= 0 หรือไม่ ถ้าใช่ให้ส่งไป Drop Zone)  
- MOK

\++++++++++++++++++++++++++

class SpecialCard extends Card 

—-------------------------------------------

private String category 

—----------------------------------------

@Override public void playEffect(...) 

- IT

\++++++++++++++++++++++++

class Player 

—----------------------

private String username 

private int actionPoints (เริ่มต้นเทิร์นมี 2\) 

private ArrayList\<Card\> hand (ไพ่บนมือ) 

private ArrayList\<StarCard\> field (ดาวบนกระดาน สูงสุด 9\) 

private boolean hasForceField (สถานะโล่ป้องกัน)

—--------------------------------------------------

`public void addCardToHand(Card c)`

`public void removeCardFromHand(Card c)`

`public boolean checkWinCondition()` (รีเทิร์น true ถ้า `field.size() == 9`)

`public boolean isHandOverLimit()` 

*(ข้างในเมธอดนี้แค่เขียนรีเทิร์นค่า `hand.size() > 7` ครับ)* 

public void discardToLimit(Card c) (สำหรับทิ้งการ์ดลง Drop Zone) 

- all

\+++++++++++++++++++++++++

class GameServer 

—-----------------------------------

`private ServerSocket serverSocket`

`private ArrayList<ClientHandler> clients` (เก็บ Socket ของเพื่อน 4 คน)

`private GameState gameState` (เก็บสเตตัสวงไพ่ทั้งหมด)

—-------------------------------------

`public void start(int port)` (เปิดเซิร์ฟเวอร์และวนลูปรับคนเข้า `accept()`)

`public synchronized void broadcast(String message)` (ส่งข้อความเช่น `UPDATE_BOARD#...` ไปให้ ClientHandler ทั้ง 4 ตัวพร้อมกัน)

`public synchronized void processClientAction(String action, ClientHandler sender)` (แกะข้อความที่ Client ส่งมา เช่น `PLAY#MERCURY` และรันลอจิกเกม)

\-MOK

\+++++++++++++++++++++++++++++

class ClientHandler extends Thread 

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

`private Socket socket`

`private BufferedReader in`

`private PrintWriter out`

`private Player player` (อ้างอิงถึงข้อมูลผู้เล่นคนนี้)

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

`@Override public void run()` (วนลูป `while(true) { String msg = in.readLine(); ... }` รอรับคำสั่ง)

`public void sendMessage(String msg)` (ส่งข้อความผ่าน `out.println()`)

- MOK

\+++++++++++++++++++++++++++++++++++++++

class GameState 

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

`private ArrayList<Player> playerList`

`private int currentPlayerIndex` (เช็คว่าตอนนี้เทิร์นใคร)

`private int currentPhase` (1: Draw, 2: Main, 3: End)

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_

`public void nextTurn()` (สลับเทิร์นและจัดการ Hand Limit)

`public boolean isPlayerTurn(Player p)`

- PAE

\+++++++++++++++++++++++++++++

`class GameClient` 

`_____________________`

`private Socket socket`

`private BufferedReader in`

`private PrintWriter out`

`private GameWindow ui (ตัวแปรชี้ไปยังหน้าจอเกม เพื่อสั่งวาดภาพใหม่)`

`__________________________`

`public void connect(String ip, int port)`

`public void sendAction(String actionMsg) (ส่งคำสั่งไปให้ Server เช่น เมาส์คลิกไพ่ปุ๊บ เรียกใช้ sendAction("PLAY#SUN"))`

`public void startListening() (เปิด Thread มารอรับคำสั่ง broadcast จาก Server ตลอดเวลา)`

- `IT`

`++++++++++++++++++++++++++`

`class GameWindow extends JFrame` 

`_________________________________-`

`private BoardPanel boardPanel`

`private JPanel myHandPanel, private JPanel leftHandPanel`

`private JPanel rightHandPanel, private JPanel topHandPanel`

`_________________________________`

`public void updateBoardState(String stateData) (รับข้อมูลจาก GameClient มาแปลผล แล้วสั่ง repaint())` 

- `Ai + all`

`++++++++++++++++++++++++++++++++++++++=`

`class BoardPanel extends JPanel` 

`_____________________________________`

`private Image backgroundImage`

`private Card dropZoneCard (เก็บข้อมูลไพ่ที่เพิ่งถูกทิ้งลงมา)`

`private Timer dropZoneTimer (Swing Timer ตั้งเวลา 2000ms)`

`________________________________________`

`@Override protected void paintComponent(Graphics g) (วาดพื้นหลังอวกาศ และถ้า dropZoneCard ไม่ใช่ null ให้ใช้ g.drawImage วาดไพ่ตรงกลางจอ)`

`public void showDropZoneCard(Card c) (เซ็ตค่า dropZoneCard = c; repaint(); dropZoneTimer.start(); พอครบ 2 วินาที Timer จะทำงานเพื่อเซ็ต dropZoneCard = null; repaint();)`

- `PAE`

`++++++++++++++++++++++++++++++++++++++`

**`class CardButton extends JButton`** `(ปุ่มรูปการ์ดบนมือ)` 

`________________________________________________`

`private Card cardData` 

`________________________________________________`

`public CardButton(Card c) (คอนสตรัคเตอร์ โหลดรูปภาพแนวตั้งแนวนอนมาแปะบนปุ่ม และใส่ ActionListener รอรับคลิก)`

`-all`

