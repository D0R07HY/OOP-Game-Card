import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBarPanel extends JPanel {

    private JLabel deckLabel;
    private JLabel statusLabel;
    private JButton endTurnButton;

    public StatusBarPanel() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 16, 0, 16));

        // ป้ายแสดงเด็คคงเหลือ (ฝั่งซ้าย)
        deckLabel = new JLabel("เด็คคงเหลือ: -");
        deckLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        deckLabel.setForeground(new Color(120, 30, 30));
        add(deckLabel, BorderLayout.WEST);

        // ป้ายแสดงข้อความสถานะเกม (ตรงกลาง)
        statusLabel = new JLabel("เลือกการ์ดจากมือเพื่อเล่น");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        statusLabel.setForeground(new Color(120, 30, 30));
        add(statusLabel, BorderLayout.CENTER);

        // ปุ่มจบเทิร์น (ฝั่งขวา)
        endTurnButton = new JButton("จบเทิร์น");
        endTurnButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
        add(endTurnButton, BorderLayout.EAST);
    }

    // เมธอดสำหรับอัปเดตข้อความเด็ค
    public void setDeckText(String text) {
        if (deckLabel != null) {
            deckLabel.setText(text);
        }
    }

    // เมธอดสำหรับอัปเดตข้อความสถานะ
    public void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    // ดึงข้อความสถานะปัจจุบัน
    public String getStatusText() {
        return statusLabel != null ? statusLabel.getText() : "";
    }

    // เปิด/ปิดการใช้งานปุ่มจบเทิร์น
    public void setEndTurnEnabled(boolean enabled) {
        if (endTurnButton != null) {
            endTurnButton.setEnabled(enabled);
        }
    }

    // ผูก Event ให้ปุ่มจบเทิร์น
    public void addEndTurnListener(ActionListener listener) {
        if (endTurnButton != null) {
            endTurnButton.addActionListener(listener);
        }
    }
}
