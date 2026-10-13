package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.app.ui.BufferedImageIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SettingsScreen extends BaseScreen {
    private static final Color HEADER_COLOR = new Color(0xF3A5EF);
    private static final Color HEADER_BUTTON = new Color(0xE98DE4);
    private static final Font TEXT_FONT = new Font("Tahoma", Font.PLAIN, 16);
    private final Timer resizeTimer = new Timer(120, e -> buildUI());

    public SettingsScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    public void onEditProfileAndGoal() {
        frame.showScreen(MainFrame.PROFILE);
    }

    public void onChangeDailyCalories() {
        String value = JOptionPane.showInputDialog(this,
                "กรอกจำนวนแคลอรี่ที่ควรบริโภคต่อวัน", "ตั้งค่าแคลอรี่",
                JOptionPane.PLAIN_MESSAGE);
        if (value == null) return;
        try {
            int calories = Integer.parseInt(value.trim());
            if (calories <= 0) throw new NumberFormatException();
            java.util.prefs.Preferences.userNodeForPackage(SettingsScreen.class)
                    .putInt("dailyCalories", calories);
            JOptionPane.showMessageDialog(this, "ตั้งค่าเป้าหมายเป็น " + calories + " kcal ต่อวันแล้ว");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "กรุณากรอกจำนวนแคลอรี่เป็นตัวเลขมากกว่า 0",
                    "ข้อมูลไม่ถูกต้อง", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void showAbout() {
        JOptionPane.showMessageDialog(this, "HealthyCalories\nแอปบันทึกข้อมูลสุขภาพและแคลอรี่",
                "เกี่ยวกับแอป", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void buildUI() {
        removeAll();
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setOpaque(true);

        int availableWidth = getWidth() > 0 ? getWidth() : 576;
        int availableHeight = getHeight() > 0 ? getHeight() : 700;

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(Color.WHITE);
        page.setPreferredSize(new Dimension(availableWidth, availableHeight));
        page.add(buildHeader(), BorderLayout.NORTH);
        page.add(buildOptions(), BorderLayout.CENTER);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.CENTER;
        add(page, constraints);
        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setPreferredSize(new Dimension(0, 66));

        JLabel title = new JLabel("ตั้งค่า", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.PLAIN, 24));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);

        JButton close = new JButton("×");
        close.setFont(new Font("Tahoma", Font.PLAIN, 38));
        close.setForeground(Color.WHITE);
        close.setBackground(HEADER_BUTTON);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setContentAreaFilled(true);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.setPreferredSize(new Dimension(66, 66));
        close.getAccessibleContext().setAccessibleName("ปิดหน้าตั้งค่า");
        close.addActionListener(e -> frame.showScreen(MainFrame.HOME));
        header.add(close, BorderLayout.WEST);
        return header;
    }

    private JPanel buildOptions() {
        JPanel options = new JPanel();
        options.setOpaque(false);
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(BorderFactory.createEmptyBorder(12, 24, 0, 12));

        options.add(createOption("Pin.png", "ตั้งเป้าหมาย/ข้อมูลตัวเองใหม่",
                this::onEditProfileAndGoal));
        options.add(Box.createVerticalStrut(8));
        options.add(createOption(null, "เกี่ยวกับแอป", this::showAbout));
        options.add(Box.createVerticalStrut(8));
        options.add(createOption("Kcal.png", "เปลี่ยนแคลอรี่ที่ควรบริโภคต่อวัน",
                this::onChangeDailyCalories));
        options.add(Box.createVerticalGlue());
        return options;
    }

    private JButton createOption(String imageName, String text, Runnable action) {
        JButton option = new JButton(text);
        option.setFont(TEXT_FONT);
        option.setForeground(Color.BLACK);
        option.setHorizontalAlignment(SwingConstants.LEFT);
        option.setIconTextGap(14);
        option.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        option.setContentAreaFilled(false);
        option.setBorderPainted(false);
        option.setFocusPainted(false);
        option.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        option.setAlignmentX(Component.LEFT_ALIGNMENT);
        option.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        option.setPreferredSize(new Dimension(400, 54));
        option.setIcon(imageName == null ? new AboutIcon() : loadIcon(imageName));
        option.addActionListener(e -> action.run());
        return option;
    }

    private Icon loadIcon(String name) {
        try {
            File file = new File("ProjectFolders" + File.separator + "Photo", name);
            BufferedImage image = file.isFile() ? ImageIO.read(file) : null;
            if (image == null) {
                throw new IllegalStateException("ไม่พบรูปไอคอนตั้งค่า: " + name);
            }
            return new BufferedImageIcon(image, 38, 38);
        } catch (IOException e) {
            throw new IllegalStateException("อ่านรูปไอคอนตั้งค่าไม่สำเร็จ: " + name, e);
        }
    }

    private static class AboutIcon implements Icon {
        @Override public int getIconWidth() { return 38; }
        @Override public int getIconHeight() { return 38; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0xF6, 0xD7, 0x74));
            g.fillRoundRect(x + 5, y + 3, 25, 31, 2, 2);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.4f));
            g.drawRoundRect(x + 5, y + 3, 25, 31, 2, 2);
            g.drawLine(x + 10, y + 10, x + 25, y + 10);
            g.drawLine(x + 10, y + 15, x + 21, y + 15);
            g.drawLine(x + 10, y + 20, x + 19, y + 20);
            g.setColor(new Color(0x8C, 0xB8, 0x3E));
            g.fillOval(x + 20, y + 19, 17, 17);
            g.setColor(Color.BLACK);
            g.drawOval(x + 20, y + 19, 17, 17);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Tahoma", Font.BOLD, 12));
            g.drawString("?", x + 25, y + 32);
            g.dispose();
        }
    }
}
