package main.java.healthycalories.app.ui.screen;

import javax.swing.*;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

public class ProfileScreen extends BaseScreen {

    // ---------- สี / ฟอนต์ ----------
    private static final Color BG = new Color(0xFF, 0xFB, 0xEF);
    private static final Color HEADER = new Color(0xEB, 0xC9, 0xEE);
    private static final Color BADGE = new Color(0xB9, 0x9F, 0xDE);
    private static final Color RING_BG = new Color(0xD9, 0xD9, 0xD9);
    private static final Color RING_FG = new Color(0x9B, 0x6F, 0xC4);
    private static final Font FONT_TITLE = new Font("Tahoma", Font.PLAIN, 20);
    private static final Font FONT_NUM = new Font("Tahoma", Font.PLAIN, 24);
    private static final Font FONT_SMALL = new Font("Tahoma", Font.PLAIN, 11);

    // ---------- ส่วนที่ต้องอัปเดตค่า ----------
    private JLabel weightValue, heightValue, bmiValue, bmrValue, diaryValue;
    private ProgressRing ring;

    public ProfileScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        buildUI();
    }

    // =====================================================
    //                      ข้อมูล
    // =====================================================

    /** รีเฟรชหน้าจอ (ค่าเริ่มต้นเป็น 0 ทั้งหมด จนกว่าจะเรียก updateStats) */
    public void showStats() {
        updateStats(0, 0, 0, 0, 0, 0);
    }

    /**
     * อัปเดตข้อมูลที่แสดงในโปรไฟล์
     *
     * @param startWeight   น้ำหนักตอนเริ่มใช้งาน (กก.)
     * @param currentWeight น้ำหนักปัจจุบัน (กก.)
     * @param targetWeight  น้ำหนักเป้าหมาย (กก.)
     * @param heightCm      ส่วนสูง (ซม.)
     * @param bmr           BMR (kcal)
     * @param diaryDays     จำนวนวันที่ใช้ไดอารี่
     */
    public void updateStats(double startWeight, double currentWeight, double targetWeight,
                            double heightCm, double bmr, int diaryDays) {
        double bmi = heightCm > 0 ? currentWeight / Math.pow(heightCm / 100.0, 2) : 0;

        // เปอร์เซ็นต์ความก้าวหน้า = ลด/เพิ่มไปแล้วเทียบกับระยะทั้งหมด
        double total = startWeight - targetWeight;
        double done = startWeight - currentWeight;
        double percent = Math.abs(total) < 0.01 ? 0 : (done / total) * 100.0;
        percent = Math.max(0, Math.min(100, percent));

        weightValue.setText(fmt(currentWeight));
        heightValue.setText(fmt(heightCm));
        bmiValue.setText(fmt(bmi));
        bmrValue.setText(String.format("%.0f", bmr));
        diaryValue.setText(String.valueOf(diaryDays));
        ring.setPercent(percent);
    }

    private String fmt(double v) {
        return (v == Math.floor(v)) ? String.valueOf((int) v) : String.format("%.1f", v);
    }

    // =====================================================
    //                        UI
    // =====================================================
    @Override
    public void buildUI() {
        removeAll();
        setLayout(new BorderLayout());
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildStatsPanel(), BorderLayout.CENTER);

        showStats();
        revalidate();
        repaint();
    }

    /** ส่วนหัวสีม่วง: ปุ่ม X + ชื่อ + รูปตัวการ์ตูน */
    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADER);
                // พื้นหลังโค้งด้านล่างเหมือนในดีไซน์
                g2.fillRect(0, 0, getWidth(), getHeight() - 40);
                g2.fill(new Ellipse2D.Double(-getWidth() * 0.2, getHeight() - 120,
                        getWidth() * 1.4, 120));
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

        // ปุ่มปิด
        JButton close = new JButton("✕");
        close.setFont(new Font("Dialog", Font.PLAIN, 20));
        close.setForeground(Color.WHITE);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> close());
        JPanel closeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        closeRow.setOpaque(false);
        closeRow.add(close);

        JLabel title = new JLabel("โปรไฟล์ของฉัน");
        title.setFont(FONT_TITLE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // รูปตัวการ์ตูน (ใส่ไฟล์ไว้ที่ resources/images/profile.png)
        JLabel avatar = loadAvatar("/images/profile.png", 130);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(closeRow);
        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(avatar);
        header.add(Box.createVerticalStrut(10));
        return header;
    }

    /** ส่วนสถิติ: ซ้าย (น้ำหนัก, ส่วนสูง) | วงกลมความก้าวหน้า | ขวา (BMI, BMR) | ล่าง (วันที่ใช้ไดอารี่) */
    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        GridBagConstraints gc = new GridBagConstraints();

        weightValue = new JLabel("0", SwingConstants.CENTER);
        heightValue = new JLabel("0", SwingConstants.CENTER);
        bmiValue = new JLabel("0", SwingConstants.CENTER);
        bmrValue = new JLabel("0", SwingConstants.CENTER);

        // ซ้าย
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(10, 0, 10, 0);
        p.add(makeBadge(weightValue, "น้ำหนัก", true), gc);
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.SOUTHWEST;
        p.add(makeBadge(heightValue, "ส่วนสูง", true), gc);

        // กลาง: วงกลม (กินสองแถว)
        ring = new ProgressRing();
        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridheight = 2;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(10, 15, 10, 15);
        p.add(ring, gc);

        // ขวา
        gc.gridheight = 1;
        gc.gridx = 2;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTHEAST;
        gc.insets = new Insets(10, 0, 10, 0);
        p.add(makeBadge(bmiValue, "BMI", false), gc);
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.SOUTHEAST;
        p.add(makeBadge(bmrValue, "BMR", false), gc);

        // ล่าง: จำนวนวันที่ใช้ไดอารี่
        diaryValue = new JLabel("0", SwingConstants.CENTER);
        diaryValue.setFont(FONT_NUM);
        diaryValue.setForeground(Color.GRAY);
        JLabel diaryTitle = new JLabel("จำนวนวันที่ใช้ไดอารี่");
        diaryTitle.setFont(FONT_SMALL);

        JPanel diaryBox = new JPanel();
        diaryBox.setBackground(Color.WHITE);
        diaryBox.setLayout(new BoxLayout(diaryBox, BoxLayout.Y_AXIS));
        diaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        diaryTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        diaryValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        diaryBox.add(diaryTitle);
        diaryBox.add(diaryValue);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.CENTER;
        gc.insets = new Insets(20, 0, 0, 0);
        p.add(diaryBox, gc);

        return p;
    }

    /** ป้ายสีม่วงโค้งมน แสดงตัวเลข + ชื่อหัวข้อใต้ตัวเลข */
    private JPanel makeBadge(JLabel valueLabel, String caption, boolean leftSide) {
        valueLabel.setFont(FONT_NUM);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel cap = new JLabel(caption);
        cap.setFont(FONT_SMALL);
        cap.setAlignmentX(leftSide ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        JPanel pill = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BADGE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        pill.setOpaque(false);
        pill.setPreferredSize(new Dimension(80, 42));
        pill.add(valueLabel, BorderLayout.CENTER);

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        pill.setAlignmentX(leftSide ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);
        wrap.add(pill);
        wrap.add(cap);
        return wrap;
    }

    private JLabel loadAvatar(String path, int height) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) {
            JLabel l = new JLabel("👧👦", SwingConstants.CENTER);
            l.setFont(new Font("Dialog", Font.PLAIN, 60));
            return l;
        }
        ImageIcon raw = new ImageIcon(url);
        int w = raw.getIconWidth() * height / Math.max(1, raw.getIconHeight());
        return new JLabel(new ImageIcon(raw.getImage().getScaledInstance(w, height, Image.SCALE_SMOOTH)));
    }

    /** ปุ่ม X */
    private void close() {
        // TODO: กลับไปหน้าก่อนหน้า เช่น frame.showScreen("home");
    }

   // =====================================================
    //            วงกลมแสดงความก้าวหน้า (%)
    // =====================================================
    private static class ProgressRing extends JComponent {
        private double percent = 0;

        ProgressRing() {
            setPreferredSize(new Dimension(130, 130));
        }

        void setPercent(double p) {
            this.percent = p;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 12;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // วงพื้น
            g2.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(RING_BG);
            g2.draw(new Ellipse2D.Double(x, y, size, size));

            // วงความก้าวหน้า (เริ่มจากด้านบน วนตามเข็มนาฬิกา)
            if (percent > 0) {
                g2.setColor(RING_FG);
                g2.draw(new Arc2D.Double(x, y, size, size, 90, -360.0 * percent / 100.0, Arc2D.OPEN));
            }

            // ข้อความตรงกลาง
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 13));
            drawCentered(g2, "ความก้าวหน้า", getHeight() / 2 - 22);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 9));
            g2.setColor(Color.GRAY);
            drawCentered(g2, "(จากน้ำหนักเป้าหมาย)", getHeight() / 2 - 10);
            g2.setFont(new Font("Tahoma", Font.PLAIN, 24));
            g2.setColor(new Color(0x7A, 0x4F, 0x9E));
            drawCentered(g2, String.format("%.0f%%", percent), getHeight() / 2 + 20);
            g2.dispose();
        }

        private void drawCentered(Graphics2D g2, String text, int baselineY) {
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, baselineY);
        }
    }
}