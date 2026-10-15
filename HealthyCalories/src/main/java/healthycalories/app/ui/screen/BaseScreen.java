    package healthycalories.app.ui.screen;

import javax.swing.*;
import java.awt.*;
import healthycalories.app.HealthyCalories;
import healthycalories.app.ui.MainFrame;

/** คลาสแม่ของหน้าจอทั้งหมด จัดเก็บตัวอ้างอิงแอปและหน้าต่างหลัก */
public abstract class BaseScreen extends JPanel {

    protected final HealthyCalories app;
    protected final MainFrame frame;

    public BaseScreen(HealthyCalories app, MainFrame frame) {
        this.app = app;
        this.frame = frame;
    }

    // แต่ละหน้าสร้างส่วนที่ผู้ใช้เห็น เช่น ฟอร์ม ปุ่ม และรายการ ในเมธอดนี้
    public abstract void buildUI();

    // ถูกเรียกทุกครั้งที่สลับมาหน้านี้ เพื่อวาดหน้าจอและโหลดข้อมูลใหม่
    public void refresh() {
        buildUI();
    }

    // UI ชั่วคราวสำหรับหน้าที่ยังไม่ได้ออกแบบ
    protected void showPlaceholder(String title) {
        removeAll();
        setLayout(new BorderLayout());
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 28));
        add(label, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}