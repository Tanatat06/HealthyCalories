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
import java.util.Locale;
import main.java.healthycalories.model.enumeration.Gender;
import main.java.healthycalories.model.user.Member;

/** หน้าตั้งค่า แสดงตัวเลือกแก้ข้อมูล/เป้าหมาย อ่านเกี่ยวกับแอป และปรับแคลอรี่ */
public class SettingsScreen extends BaseScreen {
    private static final Color HEADER_COLOR = new Color(0xF3A5EF);
    private static final Color HEADER_BUTTON = new Color(0xE98DE4);
    private static final Font TEXT_FONT = new Font("Tahoma", Font.PLAIN, 16);
    private JLayeredPane contentLayers;
    private JPanel page;
    private JPanel calorieDialogOverlay;
    private boolean calorieDialogOpen;
    private final Timer resizeTimer = new Timer(120, e -> {
        if (!calorieDialogOpen) {
            buildUI();
        }
    });

    /** เชื่อมหน้าตั้งค่ากับสมาชิกปัจจุบันและเตรียมปรับ UI ตามขนาดหน้าต่าง */
    public SettingsScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    /** เปิดขั้นตอนแก้ไขข้อมูลร่างกายและเป้าหมายของสมาชิก */
    public void onEditProfileAndGoal() {
        frame.editProfileAndGoal();
    }

    /** เปิดตัวแก้ไขเป้าหมายแคลอรี่เมื่อมีโปรไฟล์สมาชิก */
    public void onChangeDailyCalories() {
        Member member = app.getCurrentMember();
        if (member == null) {
            JOptionPane.showMessageDialog(this, "ยังไม่มีข้อมูลสมาชิก กรุณากรอกข้อมูลเริ่มต้นก่อน",
                    "ไม่พบข้อมูล", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showCalorieDialog(member);
    }

    /** แสดงแผงแก้เป้าหมายแคลอรี่พร้อมตรวจและบันทึกค่าที่ผู้ใช้ยืนยัน */
    private void showCalorieDialog(Member member) {
        if (calorieDialogOpen) {
            return;
        }
        calorieDialogOpen = true;
        resizeTimer.stop();
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.setColor(new Color(0, 0, 0, 110));
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);

        JPanel card = new CalorieDialogPanel();
        card.setLayout(new GridBagLayout());
        int dialogWidth = Math.min(560, Math.max(320, getWidth() * 4 / 5));
        int dialogHeight = Math.min(350, Math.max(250, getHeight() * 4 / 5));
        card.setPreferredSize(new Dimension(dialogWidth, dialogHeight));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(0, 12, 12, 12);
        constraints.anchor = GridBagConstraints.CENTER;
        JLabel title = new JLabel("กรอกเป้าหมายแคลอรี่ต่อวันที่ต้องการ (kcal)", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.PLAIN, 21));
        card.add(title, constraints);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        inputRow.setOpaque(false);
        JTextField caloriesField = new JTextField(
                String.format(Locale.ROOT, "%.0f", member.getDailyCalorie()), 6);
        caloriesField.setFont(new Font("Tahoma", Font.PLAIN, 24));
        caloriesField.setHorizontalAlignment(SwingConstants.CENTER);
        caloriesField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
                new Color(0x9A, 0x9A, 0x9A)));
        caloriesField.setPreferredSize(new Dimension(120, 42));
        JLabel unit = new JLabel("กิโลแคลอรี่");
        unit.setFont(new Font("Tahoma", Font.PLAIN, 21));
        unit.setForeground(new Color(0x59, 0x99, 0xF8));
        inputRow.add(caloriesField);
        inputRow.add(unit);

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 12, 4, 12);
        card.add(inputRow, constraints);

        JLabel validation = new JLabel(" ", SwingConstants.CENTER);
        validation.setFont(new Font("Tahoma", Font.PLAIN, 12));
        validation.setForeground(new Color(0xD9, 0x35, 0x45));
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 12, 6, 12);
        card.add(validation, constraints);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 15, 0));
        buttons.setOpaque(false);
        CalorieDialogButton confirm = new CalorieDialogButton("ยืนยัน",
                new Color(0x6D, 0xD5, 0x8D));
        CalorieDialogButton close = new CalorieDialogButton("ปิด",
                new Color(0xF2, 0x60, 0x63));
        buttons.add(confirm);
        buttons.add(close);
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        card.add(buttons, constraints);

        confirm.addActionListener(event -> {
            try {
                double calories = Double.parseDouble(caloriesField.getText().trim());
                double previousCalories = member.getDailyCalorie();
                member.setDailyCalorie(calories);
                try {
                    app.saveCalorieProfile(member);
                } catch (IOException e) {
                    member.setDailyCalorie(previousCalories);
                    throw e;
                }
                closeCalorieDialog(overlay);
                frame.showScreen(MainFrame.HOME);
            } catch (IllegalArgumentException e) {
                validation.setText("กรอกค่า " + (member.getGender() == Gender.MALE
                        ? "1,500–10,000" : "1,200–10,000") + " kcal");
                caloriesField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
                        new Color(0xD9, 0x35, 0x45)));
                caloriesField.requestFocusInWindow();
                caloriesField.selectAll();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "บันทึกค่าแคลอรี่ไม่สำเร็จ: " + e.getMessage(),
                        "บันทึกไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            }
        });
        close.addActionListener(event -> closeCalorieDialog(overlay));

        contentLayers.add(overlay, JLayeredPane.MODAL_LAYER);
        overlay.add(card);
        calorieDialogOverlay = overlay;
        layoutContentLayers();
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            caloriesField.requestFocusInWindow();
            caloriesField.selectAll();
        });
    }

    /** ปิดแผงเป้าหมายและคืนหน้าจอให้รับการปรับขนาดได้ */
    private void closeCalorieDialog(JPanel overlay) {
        contentLayers.remove(overlay);
        calorieDialogOverlay = null;
        calorieDialogOpen = false;
        revalidate();
        repaint();
        resizeTimer.restart();
    }

    /** แสดงรายละเอียดเกี่ยวกับแอป */
    public void showAbout() {
        String about = "เกี่ยวกับ HealthyCalories\n\n"
                + "HealthyCalories ช่วยวางแผนและติดตามเป้าหมายน้ำหนักของคุณ\n"
                + "กรอกข้อมูลพื้นฐานและน้ำหนักเป้าหมาย เพื่อดูค่า BMI, BMR และปริมาณแคลอรี่ "
                + "ที่แนะนำต่อวัน จากนั้นบันทึกน้ำหนักเพื่อดูความก้าวหน้าได้\n\n"
                + "ค่าที่คำนวณเป็นเพียงค่าประมาณ ไม่ใช่คำแนะนำทางการแพทย์ "
                + "หากมีโรคประจำตัวหรือกำลังวางแผนควบคุมน้ำหนักอย่างจริงจัง "
                + "ควรปรึกษาแพทย์หรือนักกำหนดอาหาร";
        JOptionPane.showMessageDialog(this, about, "เกี่ยวกับแอป",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void buildUI() {
        // สร้างหัวหน้าตั้งค่าและรายการตัวเลือกที่ผู้ใช้กดได้
        removeAll();
        setLayout(null);
        setBackground(Color.WHITE);
        setOpaque(true);

        int availableWidth = getWidth() > 0 ? getWidth() : 576;
        int availableHeight = getHeight() > 0 ? getHeight() : 700;

        contentLayers = new JLayeredPane();
        contentLayers.setLayout(null);
        add(contentLayers);

        page = new JPanel(new BorderLayout());
        page.setBackground(Color.WHITE);
        page.setPreferredSize(new Dimension(availableWidth, availableHeight));
        page.add(buildHeader(), BorderLayout.NORTH);
        page.add(buildOptions(), BorderLayout.CENTER);
        contentLayers.add(page, JLayeredPane.DEFAULT_LAYER);
        layoutContentLayers();
        revalidate();
        repaint();
    }

    @Override
    public void doLayout() {
        layoutContentLayers();
    }

    /** จัดตำแหน่งชั้นเนื้อหาและแผงที่วางซ้อนให้เต็มพื้นที่ */
    private void layoutContentLayers() {
        if (contentLayers == null || page == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();
        contentLayers.setBounds(0, 0, width, height);
        page.setBounds(0, 0, width, height);
        if (calorieDialogOverlay != null) {
            calorieDialogOverlay.setBounds(0, 0, width, height);
            Component dialog = calorieDialogOverlay.getComponentCount() == 0
                    ? null : calorieDialogOverlay.getComponent(0);
            if (dialog != null) {
                int dialogWidth = Math.min(560, Math.max(320, width * 4 / 5));
                int dialogHeight = Math.min(350, Math.max(250, height * 4 / 5));
                dialog.setBounds((width - dialogWidth) / 2, (height - dialogHeight) / 2,
                        dialogWidth, dialogHeight);
            }
        }
    }

    /** สร้างหัวหน้าตั้งค่าพร้อมชื่อและองค์ประกอบตกแต่ง */
    private JPanel buildHeader() {
        // แถบหัวข้อสีชมพูและปุ่มปิดกลับหน้าแรก
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

    /** สร้างตัวเลือกการตั้งค่าที่นำไปยังคำสั่งแต่ละรายการ */
    private JPanel buildOptions() {
        // รายการเมนูสำหรับตั้งเป้าหมาย ดูข้อมูลแอป และเปลี่ยนแคลอรี่ต่อวัน
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

    /** สร้างปุ่มตัวเลือกพร้อมรูปและการทำงานเมื่อผู้ใช้กด */
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

    /** โหลดไอคอนสำหรับตัวเลือก หรือคืนไอคอนสำรองเมื่ออ่านรูปไม่ได้ */
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

    /** แผงโมดัลที่วาดพื้นหลังทรงมนสำหรับปรับเป้าหมายแคลอรี่ */
    private static final class CalorieDialogPanel extends JPanel {
        /** ตั้งค่าพื้นหลังโปร่งใสเพื่อให้วาดแผงโค้งมนเอง */
        private CalorieDialogPanel() {
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(30, 28, 30, 28));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g.dispose();
        }
    }

    /** ปุ่มยืนยัน/ปิดแผงที่วาดสีพื้นและสถานะ hover เอง */
    private static final class CalorieDialogButton extends JButton {
        private final Color background;
        private boolean hovered;

        /** สร้างปุ่มยืนยันหรือปิดแผงด้วยสีที่กำหนด */
        private CalorieDialogButton(String text, Color background) {
            super(text);
            this.background = background;
            setFont(new Font("Tahoma", Font.PLAIN, 20));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(140, 48));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        /** วาดปุ่มทรงมนตามสถานะ hover และ pressed */
        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getModel().isPressed() ? background.darker()
                    : hovered ? background.brighter() : background;
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** ไอคอนวาดเองประกอบตัวเลือกเกี่ยวกับแอป */
    private static class AboutIcon implements Icon {
        /** คืนขนาดกว้างคงที่ของไอคอนเกี่ยวกับแอป */
        @Override
        public int getIconWidth() { return 38; }
        @Override public int getIconHeight() { return 38; }

        /** วาดสัญลักษณ์ไอคอนเกี่ยวกับแอป */
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
