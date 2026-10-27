import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardActionHandler {

    // ตรวจสอบเงื่อนไขการเล่นการ์ดพิเศษเบื้องต้น
    public static String validateSpecialCard(SpecialCard special, Player myPlayer, GameState gameState, int otherHandCount) {
        String name = special.getName();

        if (name.equals("Pulsar")) {
            if (myPlayer.isPulsarUsedThisTurn()) {
                return "Pulsar ใช้ได้ 1 ครั้งต่อเทิร์นเท่านั้น";
            }
            if (otherHandCount == 0) {
                return "Pulsar: ต้องมีการ์ดอีก 1 ใบในมือเพื่อทิ้งเป็นค่าการใช้";
            }
        } else if (name.equals("Black Hole")) {
            if (gameState.isBlackHoleUsed()) {
                return "Black Hole ถูกใช้ไปแล้วในเกมนี้";
            }
        } else if (name.equals("Worm Hole")) {
            if (otherHandCount == 0) {
                return "Worm Hole: ต้องมีการ์ดอีก 1 ใบในมือเพื่อใช้แลก";
            }
        }
        return null; // ผ่านเงื่อนไข ไม่มีข้อผิดพลาด
    }

    // กรองและหาเป้าหมายที่เป็นดาวของศัตรู
    public static Set<StarCard> getTargetableEnemyStars(List<Player> players, Player myPlayer) {
        Set<StarCard> result = new HashSet<>();
        if (players == null) return result;

        for (Player p : players) {
            if (p == null || p == myPlayer || !p.isActive()) continue;
            for (StarCard star : p.getField()) {
                if (star != null && !p.isCloaked(star)) {
                    result.add(star);
                }
            }
        }
        return result;
    }

    // กรองและหาเป้าหมายที่เป็นดาวของตัวเอง
    public static Set<StarCard> getTargetableOwnStars(Player myPlayer) {
        Set<StarCard> result = new HashSet<>();
        if (myPlayer == null) return result;

        for (StarCard star : myPlayer.getField()) {
            if (star != null) {
                result.add(star);
            }
        }
        return result;
    }

    // สร้างข้อความ Protocol สำหรับส่งคำสั่งไปยัง Server
    public static String buildServerCommand(Card card, Player target, StarCard targetStar, Card cost, List<Player> players) {
        if (card == null || card.getId() == null) {
            return null;
        }

        int targetIndex = -1;
        if (target != null && players != null) {
            targetIndex = players.indexOf(target);
        }

        int targetSlot = targetStar == null ? -1 : targetStar.getOrbitPosition();
        String costId = (cost == null || cost.getId() == null) ? "" : cost.getId();
        String command = "PLAY";

        if (card instanceof SpecialCard) {
            String category = ((SpecialCard) card).getCategory();
            if (SpecialCard.CATEGORY_ATTACK.equals(category)) {
                command = "ATTACK";
            } else if (SpecialCard.CATEGORY_DEFENSE.equals(category)) {
                command = "DEFEND";
            }
        }

        return command + "#" + card.getId() + "#" + targetIndex + "#" + targetSlot + "#" + costId;
    }
}
