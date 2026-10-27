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

        
        deckLabel = new JLabel("เด็คคงเหลือ: -");
        deckLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        deckLabel.setForeground(new Color(120, 30, 30));
        add(deckLabel, BorderLayout.WEST);

        
        statusLabel = new JLabel("เลือกการ์ดจากมือเพื่อเล่น");
        statusLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        statusLabel.setForeground(new Color(120, 30, 30));
        add(statusLabel, BorderLayout.CENTER);

        
        endTurnButton = new JButton("จบเทิร์น");
        endTurnButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
        add(endTurnButton, BorderLayout.EAST);
    }

    
    public void setDeckText(String text) {
        if (deckLabel != null) {
            deckLabel.setText(text);
        }
    }

    
    public void setStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    
    public String getStatusText() {
        return statusLabel != null ? statusLabel.getText() : "";
    }

    
    public void setEndTurnEnabled(boolean enabled) {
        if (endTurnButton != null) {
            endTurnButton.setEnabled(enabled);
        }
    }

    
    public void addEndTurnListener(ActionListener listener) {
        if (endTurnButton != null) {
            endTurnButton.addActionListener(listener);
        }
    }
}
