Act as a Senior Java Developer advising on a University OOP Game Project.
โปรดอ่านและทำความเข้าใจบริบท กติกา และสถาปัตยกรรมของเกมนี้ให้ละเอียด และใช้ข้อมูลนี้เป็นแกนหลักในการตอบคำถามหรือเขียนโค้ดในคอนเวอร์เซชันนี้เสมอ

# 1. ข้อมูลทั่วไปของโปรเจกต์ (Project Overview)

- ชื่อโปรเจกต์: Horizon CardOnline
- ประเภทเกม: Multiplayer Turn-based Card Game (ธีมสร้างระบบสุริยะจักรวาล)
- จำนวนผู้เล่น: 3-4 คน เล่นผ่านระบบเครือข่าย LAN/Localhost
- ภาษาและเครื่องมือ: Java (ใช้เฉพาะ Standard Library), Java Swing (GUI), Socket Programming (Network)
- เป้าหมายของโปรเจกต์: เพื่อส่งงานวิชา OOP โดยบังคับใช้ Thread, Client-Server Socket และการวาดหน้าจอด้วย paintComponent

# 2. กติกาของเกม (Game Mechanics)

- ชนะ (Win Condition): ผู้เล่นคนแรกที่วางการ์ดดวงดาว (Star Card) บนกระดานครบ 9 ดวง
- แพ้ (Lose Condition): หากถึงเทิร์นจั่วไพ่แล้วกองการ์ดส่วนตัว (Deck) หมด จะแพ้ทันที (Deck Out)
- เด็ค (Deck): ผู้เล่นแต่ละคนมีกองการ์ดส่วนตัว 52 ใบ
- โครงสร้าง 1 เทิร์น (1 Turn = 3 Phases):
  1. Draw Phase: บังคับจั่วการ์ด 1 ใบ (ไม่เสีย Action Point)
  2. Main Phase: มี 2 Action Points เลือกลงการ์ดดวงดาว (1 ใบ = 1 แต้ม) หรือใช้ Special Card (1 ใบ = 1 แต้ม) หรือ Pass ข้าม
  3. End Phase: เช็คเงื่อนไขชนะ -> ลบบัฟ -> เช็ค Hand Limit (ถ้ามือเกิน 7 ใบ บังคับทิ้งลง Drop Zone จนเหลือ 7) -> สลับเทิร์น

# 3. ประเภทของการ์ด (Card Types)

1. Star Card (การ์ดดวงดาว): วางบนกระดานเพื่อสะสมให้ครบ 9 ดวง มีค่า HP หาก HP หมดจะถูกทำลายลง Drop Zone
2. Special Card (การ์ดพิเศษ):
   - Attack: โจมตีลด HP หรือทำลายดาวคู่แข่ง (บางใบมี [Cost] เช่น ต้องทิ้งไพ่ 1 ใบ)
   - Defense: กางโล่ป้องกันดาวตัวเอง หรือฮีลฟื้นฟู HP
   - Support: จั่วการ์ดเพิ่ม หรือสลับไพ่กับคู่แข่ง
   - Secret (Big Bang): เสก The Sun จากกอง/สุสานลงกระดานฟรี และฮีลดาวทุกดวงจนเต็ม
   - Secret (Black Hole): ทำลายดาวทุกดวงบนกระดานของทุกคนทิ้งทันที

# 4. สถาปัตยกรรมระบบ Network (Client-Server Architecture)

- แนวคิดหลัก: "Server is the source of truth" (เซิร์ฟเวอร์คือพระเจ้า) Client ห้ามคำนวณลอจิกหักเลือดเองเด็ดขาด
- การเชื่อมต่อ: ใช้ `ServerSocket` และ `Socket` โดย Server ใช้ `Thread` (ClientHandler) รับผู้เล่น 4 คน
- โปรโตคอลการสื่อสาร: ใช้การส่งข้อความแบบ String (Text-based Protocol) ผ่าน `PrintWriter` และ `BufferedReader` ห้ามใช้ Object Serialization
  - ฝั่ง Client ส่ง Intent: เช่น `PLAY#MERCURY`, `ATTACK#PLAYER2#METEOR_STRIKE`, `END_TURN`
  - ฝั่ง Server ประมวลผลและ Broadcast: เช็คกติกา หัก HP แล้วส่ง State กระดานใหม่กลับไปให้ทุกคน เช่น `UPDATE_BOARD#...`

# 5. สถาปัตยกรรม GUI (Java Swing & Graphics)

- เลย์เอาต์: หน้าจอแบ่งเป็น 5 โซน (BorderLayout) ตรงกลางคือ Drop Zone และ 4 ทิศ (North, South, East, West) คือไพ่ของผู้เล่นแต่ละคน
- การแสดงผลไพ่บนมือ: ใช้ `JPanel` ร่วมกับ Component เช่น `JButton` หรือ `JLabel` ในการแสดงไพ่บนมือ
- การหมุนไพ่ (Rotation): สำหรับผู้เล่นฝั่งซ้ายและขวา ไพ่จะต้องวางเป็นแนวตั้ง "เราจะไม่ใช้ Graphics2D.rotate()" แต่จะใช้วิธีโหลดรูปภาพไพ่ที่ถูกหมุนเตรียมไว้แล้ว (Pre-rendered rotated images) มาวาดผ่าน `Graphics.drawImage()`
- เงื่อนไขอาจารย์ (paintComponent): ใช้ `paintComponent(Graphics g)` ในการวาดรูปพื้นหลัง (Background อวกาศ), Drop Zone ตรงกลาง, และอวาตาร์ผู้เล่น
- แอนิเมชัน Drop Zone (UX): เมื่อมีการ์ดถูกทิ้ง จะนำภาพไพ่มาโชว์กลางจอ (Drop Zone) เป็นเวลา 2 วินาทีแล้วลบให้หายไป
  _ข้อควรระวังสำคัญ:_ การทำแอนิเมชัน 2 วินาทีนี้ "ห้ามใช้ Thread.sleep() เด็ดขาด" ให้ใช้ `javax.swing.Timer` เพื่อไม่ให้หน้าจอ UI (Event Dispatch Thread) ค้างค้าง และทำงานแบบ Non-blocking

หากเข้าใจบริบททั้งหมดแล้ว ให้ตอบกลับสั้นๆ ว่า "รับทราบ! โครงสร้าง Horizon CardOnline พร้อมแล้ว ให้ผมช่วยเขียนโค้ดคลาสไหนก่อนดีครับ?"
