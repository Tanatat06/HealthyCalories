package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import java.awt.*;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class SplashScreen extends BaseScreen {

    public SplashScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void onStart() {
        frame.showScreen(MainFrame.LOGIN);
    }

    public void onSkip() {
        frame.showScreen(MainFrame.LOGIN);
    }

    @Override
    public void buildUI() {
        removeAll();
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));

        JLabel title = new JLabel("HealthyCalories");
        title.setFont(new Font("Tahoma", Font.BOLD, 40));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>แอปเพื่อสุขภาพและการไดเอทที่ช่วยคนไทย<br>"
                + "ทำตามเป้าหมายการลดน้ำหนักกันมาแล้วมากมาย</html>");
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 19));
        subtitle.setForeground(Color.BLACK);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        actions.setMaximumSize(new Dimension(420, 100));

        JButton btnStart = new JButton("เข้าสู่ระบบ/สร้างบัญชีใหม่");
        btnStart.setFont(new Font("Tahoma", Font.PLAIN, 18));
        btnStart.setForeground(Color.BLACK);
        btnStart.setBackground(new Color(221, 185, 231));
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(360, 54));
        btnStart.setPreferredSize(new Dimension(360, 54));
        btnStart.addActionListener(e -> onStart());

        JButton btnSkip = new JButton("ข้ามขั้นตอนนี้");
        btnSkip.setFont(new Font("Tahoma", Font.PLAIN, 15));
        btnSkip.setForeground(Color.BLACK);
        btnSkip.setContentAreaFilled(false);
        btnSkip.setBorderPainted(false);
        btnSkip.setFocusPainted(false);
        btnSkip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSkip.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSkip.addActionListener(e -> onSkip());

        actions.add(btnStart);
        actions.add(Box.createVerticalStrut(22));
        actions.add(btnSkip);

        content.add(Box.createRigidArea(new Dimension(1, 180)));
        content.add(title);
        content.add(Box.createVerticalStrut(16));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(48));
        content.add(actions);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(content, constraints);

        revalidate();
        repaint();
    }
}