package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.app.ui.BufferedImageIcon;

public class SplashScreen extends BaseScreen {

    public SplashScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void onStart() {
        frame.showScreen(MainFrame.LOGIN);
    }

    public void onSkip() {
        frame.showScreen(MainFrame.ONBOARDING);
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

        JLabel illustration = createIllustration();
        illustration.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        JButton btnStart = new RoundedButton("เข้าสู่ระบบ/สร้างบัญชีใหม่");
        btnStart.setFont(new Font("Tahoma", Font.PLAIN, 18));
        btnStart.setForeground(Color.BLACK);
        btnStart.setBackground(new Color(221, 185, 231));
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(324, 46));
        btnStart.setPreferredSize(new Dimension(324, 46));
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

        content.add(illustration);
        content.add(Box.createVerticalStrut(8));
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

    private JLabel createIllustration() {
        File file = new File("ProjectFolders" + File.separator + "Photo", "splash-character.png");
        if (!file.isFile()) {
            throw new IllegalStateException("ไม่พบรูป splash-character.png ใน ProjectFolders/Photo");
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IllegalStateException("ไฟล์รูปภาพหน้าเริ่มต้นไม่รองรับ: "
                        + file.getAbsolutePath());
            }
            return new JLabel(new BufferedImageIcon(image, 590, 360));
        } catch (IOException e) {
            throw new IllegalStateException("อ่านรูปภาพหน้าเริ่มต้นไม่สำเร็จ: "
                    + file.getAbsolutePath(), e);
        }
    }

    private static class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.setColor(new Color(205, 160, 217));
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}