package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import java.awt.*;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public abstract class BaseScreen extends JPanel {

    protected final HealthyCalories app;
    protected final MainFrame frame;

    public BaseScreen(HealthyCalories app, MainFrame frame) {
        this.app = app;
        this.frame = frame;
    }

    // แต่ละหน้าสร้าง UI ในเมธอดนี้
    public abstract void buildUI();

    // ถูกเรียกทุกครั้งที่สลับมาหน้านี้
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