package main.java.healthycalories.app.ui;

import javax.swing.*;
import java.awt.*;
import main.java.healthycalories.app.ui.dialog.ConfirmDialog;

public class SideMenu extends JPanel {
    private final MainFrame frame;

    public SideMenu(MainFrame frame) {
        this.frame = frame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(180, 0));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        add(createButton("หน้าหลัก", MainFrame.HOME));
        add(Box.createVerticalStrut(8));
        add(createButton("ค้นหาอาหาร", MainFrame.FOOD));
        add(Box.createVerticalStrut(8));
        add(createButton("ปฏิทิน", MainFrame.CALENDAR));
        add(Box.createVerticalStrut(8));
        add(createButton("ประวัติน้ำหนัก", MainFrame.WEIGHT));
        add(Box.createVerticalStrut(8));
        add(createButton("โปรไฟล์", MainFrame.PROFILE));
        add(Box.createVerticalStrut(8));
        add(createButton("ตั้งค่า", MainFrame.SETTINGS));

        add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("ออกจากระบบ");
        styleButton(btnLogout);
        btnLogout.addActionListener(e -> onLogout());
        add(btnLogout);
    }

    private JButton createButton(String text, String screenName) {
        JButton button = new JButton(text);
        styleButton(button);
        button.addActionListener(e -> onNavigate(screenName));
        return button;
    }

    private void styleButton(JButton button) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }

    public void onNavigate(String name) {
        frame.showScreen(name);
    }

    public void onLogout() {
        boolean ok = new ConfirmDialog().show(frame, "ต้องการออกจากระบบหรือไม่?");
        if (ok) {
            frame.showScreen(MainFrame.LOGIN);
        }
    }
}