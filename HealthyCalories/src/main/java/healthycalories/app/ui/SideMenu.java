package main.java.healthycalories.app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.java.healthycalories.app.ui.dialog.ConfirmDialog;

public class SideMenu extends JPanel {
    private static final Color MENU_BACKGROUND = new Color(224, 187, 232);
    private final MainFrame frame;

    public SideMenu(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(MENU_BACKGROUND);

        JButton close = new JButton("×");
        close.setFont(new Font("Tahoma", Font.PLAIN, 40));
        close.setForeground(Color.WHITE);
        close.setHorizontalAlignment(SwingConstants.LEFT);
        close.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> frame.closeMenu());
        close.setPreferredSize(new Dimension(70, 60));
        add(close, BorderLayout.NORTH);

        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createEmptyBorder(8, 16, 20, 12));
        items.add(createButton("โปรไฟล์ของฉัน", MainFrame.PROFILE, "profile", "Profile2.png"));
        items.add(Box.createVerticalStrut(8));
        items.add(createButton("ตั้งค่า", MainFrame.SETTINGS, "settings", "Setting.png"));
        items.add(Box.createVerticalStrut(8));
        items.add(createButton("อาหารของฉัน", MainFrame.FOOD, "food", "MyFood.png"));
        items.add(Box.createVerticalStrut(8));
        items.add(createButton("เข้าสู่ระบบ", MainFrame.LOGIN, "login", "Login.png"));
        items.add(Box.createVerticalStrut(8));
        JButton btnLogout = new JButton("ออกจากระบบ");
        btnLogout.setIcon(createIcon("Logout.png", "logout"));
        styleButton(btnLogout);
        btnLogout.addActionListener(e -> onLogout());
        items.add(btnLogout);
        add(items, BorderLayout.CENTER);
    }

    private JButton createButton(String text, String screenName, String glyphName, String imageName) {
        JButton button = new JButton(text, createIcon(imageName, glyphName));
        styleButton(button);
        button.addActionListener(e -> onNavigate(screenName));
        return button;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Tahoma", Font.PLAIN, 16));
        button.setForeground(Color.WHITE);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setIconTextGap(18);
        button.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 4));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        button.setPreferredSize(new Dimension(280, 64));
        button.setMinimumSize(new Dimension(180, 64));
    }

    private Icon createIcon(String imageName, String glyphName) {
        ImageIcon icon = loadPhotoIcon(imageName, 44);
        return icon != null ? icon : new MenuGlyph(glyphName);
    }

    private ImageIcon loadPhotoIcon(String name, int size) {
        String resource = "/healthycalories/app/ui/screen/images/" + name;
        try {
            java.net.URL url = getClass().getResource(resource);
            BufferedImage image = url == null ? null : ImageIO.read(url);
            if (image == null) {
                File file = new File("ProjectFolders" + File.separator + "Photo", name);
                if (!file.isFile()) return null;
                image = ImageIO.read(file);
            }
            if (image == null) return null;

            double scale = Math.min((double) size / image.getWidth(), (double) size / image.getHeight());
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            throw new IllegalStateException("อ่านไอคอน SideMenu ไม่สำเร็จ: " + name, e);
        }
    }

    public void onNavigate(String name) {
        frame.closeMenu();
        frame.showScreen(name);
    }

    public void onLogout() {
        boolean ok = new ConfirmDialog().show(frame, "ต้องการออกจากระบบหรือไม่?");
        if (ok) {
            frame.showScreen(MainFrame.LOGIN);
        }
    }

    private static class MenuGlyph implements Icon {
        private final String type;

        MenuGlyph(String type) {
            this.type = type;
        }

        @Override
        public int getIconWidth() {
            return 42;
        }

        @Override
        public int getIconHeight() {
            return 42;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0x6B, 0x4B, 0x6D));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if ("settings".equals(type)) {
                g.drawOval(x + 12, y + 12, 18, 18);
                g.drawOval(x + 17, y + 17, 8, 8);
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI * i / 4;
                    int x1 = x + 21 + (int) (Math.cos(angle) * 10);
                    int y1 = y + 21 + (int) (Math.sin(angle) * 10);
                    int x2 = x + 21 + (int) (Math.cos(angle) * 14);
                    int y2 = y + 21 + (int) (Math.sin(angle) * 14);
                    g.drawLine(x1, y1, x2, y2);
                }
            } else if ("login".equals(type) || "logout".equals(type)) {
                g.setColor(new Color(0xD9, 0xA5, 0x55));
                g.fill(new RoundRectangle2D.Double(x + 12, y + 5, 19, 32, 2, 2));
                g.setColor(new Color(0x8C, 0x58, 0x32));
                g.drawLine(x + 16, y + 6, x + 16, y + 36);
                g.setColor(Color.WHITE);
                g.fillOval(x + ("login".equals(type) ? 25 : 15), y + 19, 3, 3);
            } else if ("weight".equals(type)) {
                g.drawRoundRect(x + 9, y + 7, 24, 28, 3, 3);
                g.drawLine(x + 12, y + 14, x + 30, y + 14);
                g.drawLine(x + 12, y + 28, x + 30, y + 28);
                g.drawLine(x + 21, y + 8, x + 21, y + 34);
            }
            g.dispose();
        }
    }
}