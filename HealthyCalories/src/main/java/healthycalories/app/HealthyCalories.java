package main.java.healthycalories.app;

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.persistence.MemberRepository;

public class HealthyCalories {
    private final MemberRepository memberRepository = new MemberRepository();

    public MemberRepository getMemberRepository() {
        return memberRepository;
    }

    public static void main(String[] args) {
        configureThaiFonts();
        SwingUtilities.invokeLater(() -> {
            HealthyCalories app = new HealthyCalories();
            MainFrame frame = new MainFrame(app);
            frame.setVisible(true);
        });
    }

    private static void configureThaiFonts() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
                Font font = (Font) value;
                defaults.put(key, new FontUIResource("Tahoma", font.getStyle(), font.getSize()));
            }
        }
    }
}
