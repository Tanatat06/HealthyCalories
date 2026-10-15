package main.java.healthycalories.app.ui.screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.IntConsumer;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.app.ui.BufferedImageIcon;
import main.java.healthycalories.model.user.Member;
import main.java.healthycalories.persistence.FoodLogRepository;
/** หน้าแรก แสดงแคลอรี่คงเหลือ น้ำหนัก เป้าหมาย และเมนูหลักด้านล่าง */
public class HomeScreen extends BaseScreen {

    // ---------- สีธีม ----------
    static final Color PINK_BAR        = new Color(0xF3BFED);
    static final Color PINK_RING       = new Color(0xE6C3EE);
    static final Color PINK_RING_HOVER = new Color(0xDBA8EA);
    static final Color PINK_RING_PRESS = new Color(0xCC8EDF);
    static final Color PINK_BTN        = new Color(0xF9B8F3);
    static final Color PINK_ACTIVE     = new Color(0xFF8FEA);
    static final Color PURPLE          = new Color(0x7B3A87);
    static final Color GRAY_TEXT       = new Color(0x666666);

    // ---------- ไฟล์รูป ----------
    static final String IMG_GIRL   = "girl.png";
    static final String IMG_COUPLE = "couple.png";
    static final String IMG_PIE    = "icon_pie.png";
    static final String IMG_INFO   = "icon_info.png";
    static final String IMG_BOOK   = "icon_book.png";
    static final String IMG_CAL    = "icon_calendar.png";
    static final String IMG_KCAL   = "Kcal.png";
    static final String IMG_PIN    = "Pin.png";
    private static final Color QUICK_GRAY = new Color(0x77, 0x77, 0x77);
    private static final Color QUICK_PANEL = new Color(0xEB, 0xBF, 0xEB);
    private static final Color QUICK_BUTTON = new Color(0xE9, 0x8B, 0xE0);
    private static final Color QUICK_BUTTON_HOVER = new Color(0xF0, 0x9E, 0xE8);

    // ---------- ขนาด ----------
    static int BASE_W = 420;
    static final int BASE_H = 800;
    static final double MAX_SCALE = 0.9;           // ขนาดสูงสุด (ไม่ให้ใหญ่เกินไป)
    static double SCALE_OVERRIDE = 0;              // 0 = อัตโนมัติ, หรือใส่เอง เช่น 0.8
    static double S = 1.0;                         // อัตราส่วนที่ใช้จริง
    private boolean showingQuickActions;
    private String weightStatus = "ยังไม่มีรายการบันทึกในวันนี้";
    private ComponentAdapter weightDialogResizeListener;
    private JLayeredPane screenLayers;

    private final Timer resizeTimer = new Timer(120, e -> rebuildIfScaleChanged());

    private void updateScaleToFit(int width, int height) {
        if (SCALE_OVERRIDE > 0) {
            S = SCALE_OVERRIDE;
            return;
        }
        if (width <= 0 || height <= 0) {
            Insets insets = frame.getInsets();
            width = frame.getWidth() - 240 - insets.left - insets.right;
            height = frame.getHeight() - 80 - insets.top - insets.bottom;
        }
        updateSharedScale(width, height);
    }

    static void updateSharedScale(int width, int height) {
        if (SCALE_OVERRIDE > 0) {
            S = SCALE_OVERRIDE;
            return;
        }
        if (width <= 0 || height <= 0) return;
        S = Math.max(0.5, Math.min(MAX_SCALE, (double) height / BASE_H));
        BASE_W = Math.max(420, (int) Math.floor(width / S));
        RingPanel.CX = BASE_W / 2.0;
    }

    private void rebuildIfScaleChanged() {
        double oldScale = S;
        int oldWidth = BASE_W;
        updateScaleToFit(getWidth(), getHeight());
        if (Math.abs(oldScale - S) > 0.01 || oldWidth != BASE_W) {
            buildUI();
        }
    }

    static int s(double v) { return (int) Math.round(v * S); }

    // ---------- helper ----------
    static Font fontBase(int style, int size) { return new Font("Tahoma", style, size); }
    static Font font(int style, int size)     { return new Font("Tahoma", style, Math.max(8, s(size))); }

    static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static void drawCentered(Graphics2D g, String text, double cx, double baseline) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (float) (cx - fm.stringWidth(text) / 2.0), (float) baseline);
    }

    /** JLabel — ใส่ค่าเป็นขนาดฐาน (420x880) แล้วจะถูกคูณ scale ให้เอง */
    static JLabel label(String text, int style, int size, Color c, int x, int y, int w, int h) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(c);
        l.setBounds(s(x), s(y), s(w), s(h));
        return l;
    }

    static void flat(AbstractButton b) {
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ---------- โหลด/ย่อรูป ----------
    static BufferedImage readImage(String path) {
        String name = new File(path).getName();
        File file = new File("ProjectFolders" + File.separator + "Photo", name);
        if (!file.isFile()) {
            throw new IllegalStateException("ไม่พบไฟล์รูปใน ProjectFolders/Photo: " + name);
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IllegalStateException("ไฟล์รูปไม่รองรับ: " + file.getAbsolutePath());
            }
            return image;
        } catch (IOException e) {
            throw new IllegalStateException("อ่านไฟล์รูปไม่สำเร็จ: " + file.getAbsolutePath(), e);
        }
    }

    static BufferedImage drawScaled(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

    /** โหลดรูปแล้วย่อให้พอดีกรอบ maxW x maxH (หน่วยพิกเซลจริง) คงสัดส่วน */
    static BufferedImage loadFit(String path, int maxW, int maxH) {
        BufferedImage src = readImage(path);
        if (src == null) return null;
        double k = Math.min((double) maxW / src.getWidth(), (double) maxH / src.getHeight());
        int w = Math.max(1, (int) Math.round(src.getWidth() * k));
        int h = Math.max(1, (int) Math.round(src.getHeight() * k));
        BufferedImage cur = src;
        int cw = src.getWidth(), ch = src.getHeight();
        while (cw / 2 >= w && ch / 2 >= h) {      // ย่อทีละครึ่ง ภาพจะเนียนกว่า
            cw /= 2; ch /= 2;
            cur = drawScaled(cur, cw, ch);
        }
        return drawScaled(cur, w, h);
    }

    // =====================================================
    //  ปุ่มลูกศร < > (วงกลมกดได้)
    // =====================================================
    static class ArrowButton extends JButton {
        final boolean left;
        boolean hover;
        ArrowButton(boolean left) {
            this.left = left;
            flat(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }
        @Override public boolean contains(int x, int y) {
            double rad = getWidth() / 2.0;
            double dx = x - rad, dy = y - getHeight() / 2.0;
            return dx * dx + dy * dy <= rad * rad;
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            double w = getWidth() / S, h = getHeight() / S;
            if (getModel().isPressed())  { g.setColor(PINK_RING_PRESS); g.fill(new Ellipse2D.Double(0, 0, w, h)); }
            else if (hover)              { g.setColor(PINK_RING);       g.fill(new Ellipse2D.Double(0, 0, w, h)); }
            double cx = w / 2, cy = h / 2, d = left ? -1 : 1;
            Path2D p = new Path2D.Double();
            p.moveTo(cx - d * 5, cy - 12);
            p.lineTo(cx + d * 5, cy);
            p.lineTo(cx - d * 5, cy + 12);
            g.setColor(PURPLE);
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(p);
            g.dispose();
        }
    }

    // =====================================================
    //  วงแหวนมื้ออาหาร (กดได้ทีละส่วน)
    // =====================================================
    static class RingPanel extends JPanel {
        static double CX = BASE_W / 2.0;
        static final double CY = 150, R = 130, r = 92;
        static final String[] MEALS   = {"อาหารเช้า", "อาหารเที่ยง", "อาหารว่าง", "อาหารเย็น"};
        static final int[]    CENTERS = {90, 0, 270, 180};                // องศาแบบ Java2D
        static final double[] ROT     = {0, Math.PI / 2, 0, -Math.PI / 2};

        final BufferedImage girl = loadFit(IMG_GIRL, s(70), s(70));
        final Area[] segs = new Area[4];
        final ArrowButton prev = new ArrowButton(true);
        final ArrowButton next = new ArrowButton(false);
        int hover = -1, pressed = -1;

        /** ถูกเรียกเมื่อกดมื้ออาหาร: 0=เช้า 1=เที่ยง 2=ว่าง 3=เย็น */
        IntConsumer onMealClick = i -> { };

        final String calorieText;
        final String dateText;

        RingPanel() {
            this(0);
        }

        RingPanel(int remainingCalories) {
            this(remainingCalories, LocalDate.now());
        }

        RingPanel(int remainingCalories, LocalDate date) {
            calorieText = String.format(java.util.Locale.ROOT, "%,d",
                    Math.max(0, remainingCalories));
            dateText = date.equals(LocalDate.now()) ? "วันนี้"
                    : date.getDayOfMonth() + " " + new String[] {
                        "ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
                        "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."
                    }[date.getMonthValue() - 1] + " " + (date.getYear() + 543);
            setOpaque(false);
            setLayout(null);
            prev.setBounds(s(CX - 70 - 20), s(CY - 7 - 20), s(40), s(40));
            next.setBounds(s(CX + 70 - 20), s(CY - 7 - 20), s(40), s(40));
            add(prev);
            add(next);
            Area inner = new Area(new Ellipse2D.Double(CX - r, CY - r, 2 * r, 2 * r));
            for (int i = 0; i < 4; i++) {
                Area seg = new Area(new Arc2D.Double(CX - R, CY - R, 2 * R, 2 * R, CENTERS[i] - 45, 90, Arc2D.PIE));
                seg.subtract(inner);
                segs[i] = seg;
            }
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e)   { setHover(segmentAt(e)); }
                @Override public void mouseExited(MouseEvent e)  { setHover(-1); }
                @Override public void mousePressed(MouseEvent e) { pressed = segmentAt(e); repaint(); }
                @Override public void mouseReleased(MouseEvent e) {
                    int i = segmentAt(e);
                    if (i >= 0 && i == pressed) onMealClick.accept(i);
                    pressed = -1; repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        int segmentAt(MouseEvent e) {
            Point2D p = new Point2D.Double(e.getX() / S, e.getY() / S);
            for (int i = 0; i < 4; i++) if (segs[i].contains(p)) return i;
            return -1;
        }

        void setHover(int i) {
            if (i != hover) {
                hover = i;
                setCursor(Cursor.getPredefinedCursor(i >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                repaint();
            }
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            AffineTransform orig = g.getTransform();
            g.scale(S, S);

            for (int i = 0; i < 4; i++) {
                Color c = PINK_RING;
                if (hover == i) c = (pressed == i) ? PINK_RING_PRESS : PINK_RING_HOVER;
                g.setColor(c);
                g.fill(segs[i]);
            }
            // เส้นแบ่งสีขาว
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f));
            for (int a = 45; a < 360; a += 90) {
                double rad = Math.toRadians(a);
                g.draw(new Line2D.Double(CX + r * Math.cos(rad), CY - r * Math.sin(rad),
                                         CX + R * Math.cos(rad), CY - R * Math.sin(rad)));
            }
            // ข้อความบนวงแหวน
            g.setFont(fontBase(Font.PLAIN, 20));
            g.setColor(Color.BLACK);
            double m = (R + r) / 2;
            for (int i = 0; i < 4; i++) {
                String t = "+" + MEALS[i];
                double rad = Math.toRadians(CENTERS[i]);
                double px = CX + m * Math.cos(rad), py = CY - m * Math.sin(rad);
                AffineTransform old = g.getTransform();
                g.rotate(ROT[i], px, py);
                FontMetrics fm = g.getFontMetrics();
                g.drawString(t, (float) (px - fm.stringWidth(t) / 2.0),
                        (float) (py + (fm.getAscent() - fm.getDescent()) / 2.0));
                g.setTransform(old);
            }
            // ข้อความตรงกลาง
            g.setColor(Color.BLACK);
            g.setFont(fontBase(Font.PLAIN, dateText.length() > 8 ? 15 : 18));
            drawCentered(g, dateText, CX, CY - 52);
            g.setColor(new Color(0x555555));
            g.setFont(fontBase(Font.PLAIN, 14));
            drawCentered(g, "(เหลือแคลอรี่ทานได้อีก)", CX, CY - 32);

            g.setColor(PURPLE);
            g.setFont(fontBase(Font.PLAIN, 46));
            drawCentered(g, calorieText, CX, CY + 10);

            // รูปการ์ตูน (วาดด้วยขนาดจริงเพื่อให้คม)
            g.setTransform(orig);
            if (girl != null) {
                g.drawImage(girl, (int) Math.round(CX * S - girl.getWidth() / 2.0),
                        (int) Math.round((CY + 22) * S), null);
            }
            g.dispose();
        }
    }

    // =====================================================
    //  ไอคอนวาดเอง (ปฏิทินเล็ก / หมุด)
    // =====================================================
    static void drawCalendar(Graphics2D g) {
        RoundRectangle2D body = new RoundRectangle2D.Double(1, 3, 20, 18, 5, 5);
        g.setColor(Color.WHITE);
        g.fill(body);
        Shape old = g.getClip();
        g.clip(body);
        g.setColor(new Color(0xE4504D));
        g.fillRect(0, 3, 22, 6);
        g.setClip(old);
        g.setColor(new Color(0xB5B5B5));
        g.setStroke(new BasicStroke(1f));
        g.draw(body);
        g.setColor(new Color(0x555555));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(6, 1, 6, 5);
        g.drawLine(16, 1, 16, 5);
        g.setColor(new Color(0x9A9A9A));
        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 3; col++)
                g.fillRect(4 + col * 5, 12 + row * 4, 3, 2);
    }

    static class PinIcon extends JComponent {
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            g.setColor(new Color(0xD9304F));
            g.fill(new Ellipse2D.Double(1, 0, 12, 12));
            Path2D tip = new Path2D.Double();
            tip.moveTo(2.2, 8.5); tip.lineTo(11.8, 8.5); tip.lineTo(7, 17); tip.closePath();
            g.fill(tip);
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(4.5, 3.5, 5, 5));
            g.dispose();
        }
    }

    // =====================================================
    //  ปุ่ม "ประวัติน้ำหนัก" (ไอคอนปฏิทิน + ข้อความ)
    // =====================================================
    static class HistoryButton extends JButton {
        boolean hover;
        HistoryButton() {
            flat(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            double w = getWidth() / S, h = getHeight() / S;
            if (getModel().isPressed())  { g.setColor(new Color(123, 58, 135, 55)); g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14)); }
            else if (hover)              { g.setColor(new Color(123, 58, 135, 28)); g.fill(new RoundRectangle2D.Double(0, 0, w, h, 14, 14)); }
            AffineTransform old = g.getTransform();
            g.translate(6, (h - 22) / 2);
            drawCalendar(g);
            g.setTransform(old);
            g.setFont(fontBase(Font.PLAIN, 13));
            g.setColor(hover ? PURPLE : GRAY_TEXT);
            FontMetrics fm = g.getFontMetrics();
            g.drawString("ประวัติน้ำหนัก", 34f, (float) ((h + fm.getAscent() - fm.getDescent()) / 2.0));
            g.dispose();
        }
    }

    // =====================================================
    //  ข้อความหลายส่วนในบรรทัดเดียว (ใช้เส้นฐานเดียวกัน)
    // =====================================================
    static class TextLine extends JComponent {
        final int baseline, gap;
        final boolean center;
        final String[] texts;
        final int[] sizes;
        final Color[] colors;

        TextLine(int baseline, int gap, boolean center, String[] texts, int[] sizes, Color[] colors) {
            this.baseline = baseline;
            this.gap = gap;
            this.center = center;
            this.texts = texts;
            this.sizes = sizes;
            this.colors = colors;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            double total = gap * (texts.length - 1);
            for (int i = 0; i < texts.length; i++) {
                g.setFont(fontBase(Font.PLAIN, sizes[i]));
                total += g.getFontMetrics().stringWidth(texts[i]);
            }
            double x = center ? (getWidth() / S - total) / 2 : 0;
            for (int i = 0; i < texts.length; i++) {
                g.setFont(fontBase(Font.PLAIN, sizes[i]));
                g.setColor(colors[i]);
                g.drawString(texts[i], (float) x, baseline);
                x += g.getFontMetrics().stringWidth(texts[i]) + gap;
            }
            g.dispose();
        }
    }

    // =====================================================
    //  ช่องในแถบม่วง (ตัวเลขบรรทัดบน / ชื่อบรรทัดล่าง ตรงกันทุกช่อง)
    // =====================================================
    static class StripCell extends JComponent {
        final String top, bottom;
        StripCell(String top, String bottom) {
            this.top = top;
            this.bottom = bottom;
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            g.setFont(fontBase(Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            double cx = getWidth() / S / 2.0;
            drawCentered(g, top, cx, 19);
            drawCentered(g, bottom, cx, 38);
            g.dispose();
        }
    }

    // =====================================================
    //  การ์ดน้ำหนัก / แคลอรี่ / BMI / BMR
    // =====================================================
    static class WeightCard extends JPanel {
        static final int CH = 150, STRIP_Y = 105, LINE_Y = 66;
        final int cardWidth = Math.max(370, BASE_W - 50);
        final JButton saveBtn = new JButton("บันทึกน้ำหนัก") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                aa(g);
                g.setColor(PINK_BTN);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0xE79BE0));
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        final HistoryButton historyBtn = new HistoryButton();

        void addLine(int x, int w, int gap, boolean center, String[] t, int[] sz, Color[] c) {
            TextLine line = new TextLine(46, gap, center, t, sz, c);
            line.setBounds(s(x), s(LINE_Y - 46), s(w), s(56));
            add(line);
        }

        WeightCard(Member member, double consumedCalories) {
            setLayout(null);
            setOpaque(false);

            historyBtn.setBounds(s((cardWidth - 120) / 2), s(6), s(120), s(24));
            add(historyBtn);

            double currentWeight = member == null ? 0 : member.getWeight();
            double targetWeight = member == null ? 0 : member.getTargetWeight();
            addLine(20, cardWidth / 2 - 30, 6, true,
                    new String[]{String.format(Locale.ROOT, "%.1f", currentWeight),
                        String.format(Locale.ROOT, "/ %.1f กก.", targetWeight)},
                    new int[]{44, 16}, new Color[]{Color.BLACK, GRAY_TEXT});

            saveBtn.setFont(font(Font.PLAIN, 13));
            saveBtn.setBackground(PINK_BTN);
            saveBtn.setForeground(Color.BLACK);
            saveBtn.setFocusPainted(false);
            saveBtn.setContentAreaFilled(false);
            saveBtn.setBorderPainted(false);
            saveBtn.setRolloverEnabled(false);
            saveBtn.setOpaque(false);
            saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            saveBtn.setBounds(s((cardWidth / 2 - 125) / 2), s(70), s(125), s(30));
            add(saveBtn);

            JPanel bar = new JPanel();
            bar.setBackground(new Color(0x777777));
            bar.setBounds(s(cardWidth / 2), s(30), Math.max(2, s(5)), s(66));
            add(bar);

            int rightStart = cardWidth / 2 + 16;
            BufferedImage kcalIcon = loadFit(IMG_KCAL, s(22), s(22));
            if (kcalIcon != null) {
                JLabel kcalImage = new JLabel(new BufferedImageIcon(kcalIcon));
                kcalImage.setBounds(s(rightStart), s(20), s(22), s(22));
                add(kcalImage);
            }
            add(label("แคลอรี่ที่ได้รับจากอาหาร", Font.PLAIN, 13, GRAY_TEXT,
                    rightStart + 24, 22, cardWidth - rightStart - 30, 22));
            addLine(rightStart, cardWidth - rightStart - 12, 6, true,
                    new String[]{String.format(Locale.ROOT, "%.0f", consumedCalories), "Kcal"},
                    new int[]{13, 14}, new Color[]{PURPLE, Color.BLACK});

            String progress = member == null ? "--%"
                    : String.format(Locale.ROOT, "%.0f%%", member.getProgressPercent());
            String bmi = member == null || member.getHeight() <= 0 ? "--"
                    : String.format(Locale.ROOT, "BMI %.1f", member.calBMI());
            String bmiStatus = member == null || member.getHeight() <= 0
                    ? "ยังไม่มีข้อมูล" : member.getBmiStatus();
            String bmr = member == null ? "--"
                    : String.format(Locale.ROOT, "%.0f", member.calBMR());
            String[][] cols = {{progress, "ความก้าวหน้า"}, {bmi, bmiStatus}, {bmr, "BMR"}};
            for (int i = 0; i < 3; i++) {
                StripCell cell = new StripCell(cols[i][0], cols[i][1]);
                int colStart = i * cardWidth / 3;
                int colEnd = (i + 1) * cardWidth / 3;
                cell.setBounds(s(colStart), s(STRIP_Y), s(colEnd - colStart), s(45));
                add(cell);
            }
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, cardWidth - 1, CH - 1, 28, 28);
            g.setColor(Color.WHITE);
            g.fill(shape);
            Shape old = g.getClip();
            g.clip(shape);
            g.setColor(PURPLE);
            g.fillRect(0, STRIP_Y, cardWidth, CH - STRIP_Y);
            g.setClip(old);
            g.setColor(new Color(0xDDDDDD));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(shape);
            g.dispose();
        }
    }

    // =====================================================
    //  แถบความคืบหน้าเป้าหมาย
    // =====================================================
    static class GoalBar extends JPanel {
        int value;
        GoalBar(int value) {
            this.value = Math.max(0, Math.min(100, value));
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            double w = getWidth() / S, h = getHeight() / S;
            g.setColor(PINK_BAR);
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
            if (value > 0) {
                g.setColor(new Color(0xE08AD8));
                g.fill(new RoundRectangle2D.Double(0, 0, w * value / 100, h, h, h));
            }
            g.dispose();
        }
    }

    // =====================================================
    //  ปุ่มเมนูด้านล่าง (ใช้รูปภาพ)
    // =====================================================
    static class NavButton extends JButton {
        final String text;
        final boolean active;
        final BufferedImage icon = null;
        BufferedImage img;
        final boolean imageIncludesLabel;

        NavButton(String text, String imgPath, boolean active) {
            this.text = text;
            this.active = active;
            this.imageIncludesLabel = IMG_INFO.equals(imgPath);
            BufferedImage source = loadFit(imgPath,
                    s(imageIncludesLabel ? 70 : 40),
                    s(imageIncludesLabel ? 70 : 40));
            this.img = imageIncludesLabel
                    ? recolorIcon(source, active ? PINK_ACTIVE : Color.WHITE)
                    : tintIcon(source, active ? PINK_ACTIVE : Color.WHITE);
            flat(this);
        }

        private static BufferedImage tintIcon(BufferedImage source, Color tint) {
            if (source == null) return null;
            BufferedImage tinted = new BufferedImage(
                    source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int argb = source.getRGB(x, y);
                    int alpha = (argb >>> 24) & 0xFF;
                    int green = (argb >>> 8) & 0xFF;
                    int symbolCoverage = Math.max(0, Math.min(255,
                            (green - PINK_BAR.getGreen()) * 255 / (255 - PINK_BAR.getGreen())));
                    int tintedAlpha = alpha * symbolCoverage / 255;
                    tinted.setRGB(x, y, (tintedAlpha << 24)
                            | (tint.getRed() << 16) | (tint.getGreen() << 8) | tint.getBlue());
                }
            }
            return tinted;
        }

        private static BufferedImage recolorIcon(BufferedImage source, Color tint) {
            if (source == null) return null;
            BufferedImage recolored = new BufferedImage(
                    source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < source.getHeight(); y++) {
                for (int x = 0; x < source.getWidth(); x++) {
                    int alpha = (source.getRGB(x, y) >>> 24) & 0xFF;
                    recolored.setRGB(x, y, (alpha << 24)
                            | (tint.getRed() << 16)
                            | (tint.getGreen() << 8)
                            | tint.getBlue());
                }
            }
            return recolored;
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            AffineTransform orig = g.getTransform();
            g.scale(S, S);
            double w = getWidth() / S;
            if (!imageIncludesLabel) {
                g.setFont(fontBase(Font.BOLD, 15));
                g.setColor(active ? PINK_ACTIVE : Color.WHITE);
                drawCentered(g, text, w / 2, 62);
            }
            g.setTransform(orig);
            if (img != null) {
                g.drawImage(img, (getWidth() - img.getWidth()) / 2,
                        imageIncludesLabel ? 0 : (int) Math.round(28 * S - img.getHeight() / 2.0), null);
            }
            g.dispose();
        }
    }

    static class NavBar extends JPanel {
        final NavButton info;
        final NavButton diary;
        final NavButton cal;

        NavBar() {
            this("data");
        }

        NavBar(boolean diarySelected) {
            this(diarySelected ? "diary" : "data");
        }

        NavBar(String selectedTab) {
            info = new NavButton("ข้อมูล", IMG_INFO, "data".equals(selectedTab));
            diary = new NavButton("บันทึก", IMG_BOOK, "diary".equals(selectedTab));
            cal = new NavButton("ปฏิทิน", IMG_CAL, "calendar".equals(selectedTab));
            setLayout(null);
            setBackground(PINK_BAR);
            place(info, (int) Math.round(BASE_W * 0.16));
            place(diary, (int) Math.round(BASE_W * 0.34));
            place(cal, (int) Math.round(BASE_W * 0.84));
        }

        void place(NavButton b, int centerX) {
            b.setBounds(s(centerX - 40), 0, s(80), s(70));
            add(b);
        }
    }

    // =====================================================
    //  ปุ่ม + ลอยตรงกลางล่าง
    // =====================================================
    static class FabButton extends JButton {
        boolean hover;
        FabButton() {
            flat(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            g.setColor(new Color(0, 0, 0, 30));
            g.fillOval(2, 4, 80, 80);
            g.setColor(getModel().isPressed() ? new Color(0xF48CEC) : hover ? new Color(0xFDB0F8) : new Color(0xFBA0F5));
            g.fillOval(0, 0, 80, 80);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(20, 40, 60, 40);
            g.drawLine(40, 20, 40, 60);
            g.dispose();
        }
    }

    // =====================================================
    //  หน้าต่างหลัก
    // =====================================================
    public HomeScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    @Override
    public void buildUI() {
        // วาดวงแหวนแคลอรี่ การ์ดน้ำหนัก/สถิติ แถบเป้าหมาย และเมนูนำทาง
        if (showingQuickActions) {
            buildQuickActionsUI();
            return;
        }
        updateScaleToFit(getWidth(), getHeight());
        removeAll();
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        screenLayers = new JLayeredPane();
        screenLayers.setLayout(null);
        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(s(BASE_W), s(BASE_H)));

        JPanel main = new JPanel(null);
        main.setBackground(Color.WHITE);
        main.setBounds(0, 0, s(BASE_W), s(BASE_H));

        // วงแหวนมื้ออาหาร
        double consumedCalories = 0;
        try {
            consumedCalories = new FoodLogRepository(app.getCurrentUsername())
                    .getTotalKcalForDate(app.getSelectedDiaryDate());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "ไม่สามารถโหลดข้อมูลแคลอรี่วันนี้ได้: " + e.getMessage(),
                    "โหลดข้อมูลไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
        }
        int remainingCalories = app.getCurrentMember() == null
                ? 0 : (int) Math.round(Math.max(0,
                        app.getCurrentMember().getDailyCalorie() - consumedCalories));
        RingPanel ring = new RingPanel(remainingCalories, app.getSelectedDiaryDate());
        ring.setBounds(0, s(20), s(BASE_W), s(290));
        ring.onMealClick = i -> frame.showFoodCategories(RingPanel.MEALS[i]);
        main.add(ring);

        ring.prev.addActionListener(e -> {
            app.setSelectedDiaryDate(app.getSelectedDiaryDate().minusDays(1));
            buildUI();
        });
        ring.next.addActionListener(e -> {
            if (app.getSelectedDiaryDate().isBefore(LocalDate.now())) {
                app.setSelectedDiaryDate(app.getSelectedDiaryDate().plusDays(1));
                buildUI();
            }
        });

        // การ์ดน้ำหนัก
        WeightCard card = new WeightCard(app.getCurrentMember(), consumedCalories);
        card.setBounds(s((BASE_W - card.cardWidth) / 2), s(315),
                s(card.cardWidth), s(WeightCard.CH));
        card.saveBtn.addActionListener(e -> onSaveWeight());
        card.historyBtn.addActionListener(e -> frame.showScreen(MainFrame.WEIGHT));
        main.add(card);

        // เป้าหมาย
        BufferedImage pinImage = loadFit(IMG_PIN, s(14), s(18));
        if (pinImage != null) {
            JLabel pin = new JLabel(new BufferedImageIcon(pinImage));
            pin.setBounds(s(28), s(472), s(14), s(18));
            main.add(pin);
        }
        Member member = app.getCurrentMember();
        int daysToGoal = member == null ? 0 : member.calDaysToGoal();
        String goalText = member == null ? "เป้าหมายอีก -- วัน"
                : daysToGoal == 0 ? "ถึงเป้าหมายแล้ว!" : "เป้าหมายอีก " + daysToGoal + " วัน";
        main.add(label(goalText, Font.PLAIN, 12, Color.BLACK, 46, 473, 200, 18));
        GoalBar goal = new GoalBar(member == null
                ? 0 : (int) Math.round(member.getProgressPercent()));
        goal.setBounds(s(28), s(496), s(BASE_W - 56), s(10));
        main.add(goal);

        // รูปคู่ด้านล่าง
        BufferedImage couple = loadFit(IMG_COUPLE, s(Math.min(BASE_W - 80, 500)), s(200));
        if (couple != null) {
            JLabel cl = new JLabel(new BufferedImageIcon(couple), SwingConstants.CENTER);
            cl.setBounds(0, s(516), s(BASE_W), s(200));
            main.add(cl);
        }

        // แถบเมนูล่าง
        NavBar nav = new NavBar();
        nav.setBounds(0, s(730), s(BASE_W), s(70));
        nav.info.addActionListener(e -> frame.showScreen(MainFrame.HOME));
        nav.diary.addActionListener(e -> frame.showScreen(MainFrame.DIARY));
        nav.cal.addActionListener(e -> frame.showScreen(MainFrame.CALENDAR));
        main.add(nav);

        layered.add(main, JLayeredPane.DEFAULT_LAYER);

        // ปุ่ม + (ลอยทับแถบเมนู)
        FabButton fab = new FabButton();
        fab.setBounds(s(BASE_W / 2 - 40), s(730 - 40), s(84), s(84));
        fab.addActionListener(e -> {
            showingQuickActions = true;
            frame.setHomeHeaderVisible(false);
            buildUI();
        });
        layered.add(fab, JLayeredPane.PALETTE_LAYER);

        JPanel canvas = new JPanel(new GridBagLayout());
        canvas.setOpaque(true);
        canvas.setBackground(Color.WHITE);
        canvas.add(layered);
        setScreenContent(canvas);
        revalidate();
        repaint();
    }

    public void showHomeView() {
        showingQuickActions = false;
    }

    private void buildQuickActionsUI() {
        // แสดงเมนูลัดพื้นหลังเทา พร้อมปุ่มค้นหาอาหารและบันทึกน้ำหนัก
        updateScaleToFit(getWidth(), getHeight());
        removeAll();
        setOpaque(true);
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
        screenLayers = new JLayeredPane();
        screenLayers.setLayout(null);

        JPanel quickActions = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                aa(g);

                int width = getWidth();
                int height = getHeight();
                int headerHeight = Math.max(54, (int) (height * 0.10));
                int panelY = (int) (height * 0.55);
                int panelHeight = height - panelY;
                int radius = Math.max(24, (int) (width * 0.05));

                g.setColor(QUICK_GRAY);
                g.fillRect(0, headerHeight, width, height - headerHeight);
                g.setColor(PINK_BAR);
                g.fillRect(0, 0, width, headerHeight);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2f));
                g.drawLine(20, headerHeight / 2 - 7, 57, headerHeight / 2 - 7);
                g.drawLine(20, headerHeight / 2 + 7, 57, headerHeight / 2 + 7);

                g.setColor(Color.BLACK);
                g.setFont(font(Font.PLAIN, 20));
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(weightStatus, (width - metrics.stringWidth(weightStatus)) / 2,
                        (int) (height * 0.40) + metrics.getAscent() / 2);

                int corner = Math.min(radius, panelHeight / 2);
                Path2D panel = new Path2D.Double();
                panel.moveTo(0, panelY + panelHeight);
                panel.lineTo(0, panelY + corner);
                panel.quadTo(0, panelY, corner, panelY);
                panel.lineTo(width - corner, panelY);
                panel.quadTo(width, panelY, width, panelY + corner);
                panel.lineTo(width, panelY + panelHeight);
                panel.closePath();
                g.setColor(QUICK_PANEL);
                g.fill(panel);
                g.dispose();
            }
        };
        quickActions.setOpaque(false);
        setScreenContent(quickActions);

        JButton menu = new JButton();
        flat(menu);
        menu.setToolTipText("เปิดเมนู");
        menu.addActionListener(event -> frame.openMenu());
        quickActions.add(menu);

        JButton search = createQuickActionButton("ค้นหาจากฐานข้อมูล", true,
                () -> frame.showFoodCategories());
        JButton save = createQuickActionButton("บันทึกน้ำหนัก", false, this::onSaveWeight);
        quickActions.add(search);
        quickActions.add(save);

        quickActions.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layoutQuickActionsButtons(quickActions, menu, search, save);
            }
        });
        layoutQuickActionsButtons(quickActions, menu, search, save);
        revalidate();
        repaint();
    }

    private void setScreenContent(JComponent content) {
        screenLayers.add(content, JLayeredPane.DEFAULT_LAYER);
        screenLayers.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                content.setBounds(0, 0, screenLayers.getWidth(), screenLayers.getHeight());
            }
        });
        content.setBounds(0, 0, screenLayers.getWidth(), screenLayers.getHeight());
        add(screenLayers, BorderLayout.CENTER);
    }

    private void layoutQuickActionsButtons(JPanel panel, JButton menu,
            JButton search, JButton save) {
        int width = panel.getWidth();
        int height = panel.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        int buttonWidth = Math.max(s(188), width / 5);
        int buttonHeight = Math.max(s(48), height / 14);
        int buttonX = (width - buttonWidth) / 2;
        menu.setBounds(s(8), s(8), s(60), s(52));
        search.setBounds(buttonX, (int) (height * 0.655), buttonWidth, buttonHeight);
        save.setBounds(buttonX, (int) (height * 0.745), buttonWidth, buttonHeight);
    }

    private JButton createQuickActionButton(String text, boolean searchIcon, Runnable action) {
        JButton button = new JButton(text) {
        private boolean hover;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent event) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                aa(g);
                g.setColor(getModel().isPressed()
                        ? QUICK_BUTTON.darker() : hover ? QUICK_BUTTON_HOVER : QUICK_BUTTON);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), s(12), s(12));
                int iconX = s(12);
                int iconY = (getHeight() - s(24)) / 2;
                g.setColor(PURPLE);
                if (searchIcon) {
                    g.setStroke(new BasicStroke(Math.max(1.5f, s(2.5))));
                    g.drawOval(iconX + s(2), iconY + s(2), s(12), s(12));
                    g.drawLine(iconX + s(13), iconY + s(13),
                            iconX + s(21), iconY + s(21));
                } else {
                    Path2D leftPage = new Path2D.Double();
                    leftPage.moveTo(iconX + s(1), iconY + s(5));
                    leftPage.lineTo(iconX + s(10), iconY + s(3));
                    leftPage.lineTo(iconX + s(10), iconY + s(19));
                    leftPage.lineTo(iconX + s(1), iconY + s(21));
                    leftPage.closePath();
                    Path2D rightPage = new Path2D.Double();
                    rightPage.moveTo(iconX + s(13), iconY + s(3));
                    rightPage.lineTo(iconX + s(22), iconY + s(5));
                    rightPage.lineTo(iconX + s(22), iconY + s(21));
                    rightPage.lineTo(iconX + s(13), iconY + s(19));
                    rightPage.closePath();
                    g.fill(leftPage);
                    g.fill(rightPage);
                }
                g.setColor(Color.WHITE);
                g.setFont(font(Font.PLAIN, 16));
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(getText(), s(42),
                        (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                g.dispose();
            }
        };
        flat(button);
        button.setForeground(Color.WHITE);
        button.setFont(font(Font.PLAIN, 16));
        button.addActionListener(event -> action.run());
        return button;
    }

    private void onSaveWeight() {
        if (screenLayers == null) {
            return;
        }
        JPanel overlay = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                graphics.setColor(new Color(0, 0, 0, 110));
                graphics.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setOpaque(false);

        int dialogWidth = Math.min(470, Math.max(360, screenLayers.getWidth() * 2 / 5));
        int dialogHeight = Math.min(330, Math.max(270, screenLayers.getHeight() * 9 / 20));
        RoundedDialogPanel card = new RoundedDialogPanel(Color.WHITE, s(18));
        card.setPreferredSize(new Dimension(dialogWidth, dialogHeight));
        card.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(s(24), s(24), s(22), s(24)));

        JLabel title = new JLabel("กรุณาบันทึกน้ำหนักของวันนี้", SwingConstants.CENTER);
        title.setFont(font(Font.PLAIN, 24));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JPanel weightRow = new JPanel(new FlowLayout(FlowLayout.CENTER, s(12), 0));
        weightRow.setOpaque(false);
        JTextField weightField = new JTextField("0", 5);
        if (app.getCurrentMember() != null) {
            weightField.setText(String.format(Locale.ROOT, "%.1f",
                    app.getCurrentMember().getWeight()));
        }
        weightField.setFont(font(Font.PLAIN, 25));
        weightField.setForeground(new Color(0x59, 0x99, 0xF8));
        weightField.setHorizontalAlignment(SwingConstants.CENTER);
        weightField.setBorder(new MatteBorder(0, 0, s(2), 0, new Color(0x9A, 0x9A, 0x9A)));
        weightField.setPreferredSize(new Dimension(s(105), s(42)));
        weightField.setMaximumSize(new Dimension(s(105), s(42)));

        JLabel unit = new JLabel("กิโลกรัม");
        unit.setFont(font(Font.PLAIN, 22));
        unit.setForeground(new Color(0x59, 0x99, 0xF8));
        weightRow.add(weightField);
        weightRow.add(unit);

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, s(18), 0));
        buttonRow.setOpaque(false);
        RoundedDialogButton confirmButton = new RoundedDialogButton(
                "ยืนยัน", new Color(0x6D, 0xD5, 0x8D));
        RoundedDialogButton closeButton = new RoundedDialogButton(
                "ปิด", new Color(0xF2, 0x60, 0x63));
        confirmButton.addActionListener(event -> {
            try {
                double kilograms = Double.parseDouble(weightField.getText().trim());
                if (kilograms <= 0 || kilograms > 500) {
                    throw new NumberFormatException();
                }
                app.saveWeight(kilograms, LocalDate.now());
                weightStatus = String.format("บันทึกน้ำหนักวันนี้: %.1f กก.", kilograms);
                closeWeightDialog(overlay);
                showingQuickActions = false;
                frame.setHomeHeaderVisible(true);
                frame.showScreen(MainFrame.HOME);
            } catch (NumberFormatException exception) {
                weightField.setBorder(new MatteBorder(0, 0, s(2), 0, Color.RED));
                weightField.requestFocusInWindow();
                weightField.selectAll();
            } catch (IOException | IllegalArgumentException | IllegalStateException exception) {
                JOptionPane.showMessageDialog(this,
                        "ไม่สามารถบันทึกน้ำหนักได้: " + exception.getMessage(),
                        "บันทึกไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            }
        });
        closeButton.addActionListener(event -> closeWeightDialog(overlay));
        buttonRow.add(confirmButton);
        buttonRow.add(closeButton);

        content.add(title);
        content.add(Box.createVerticalGlue());
        content.add(weightRow);
        content.add(Box.createVerticalGlue());
        content.add(buttonRow);
        card.add(content);
        overlay.add(card);
        weightDialogResizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layoutWeightDialog(overlay, card);
            }
        };
        screenLayers.addComponentListener(weightDialogResizeListener);

        overlay.setBounds(0, 0, screenLayers.getWidth(), screenLayers.getHeight());
        layoutWeightDialog(overlay, card);
        screenLayers.add(overlay, JLayeredPane.PALETTE_LAYER);
        screenLayers.moveToFront(overlay);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            weightField.requestFocusInWindow();
            weightField.selectAll();
        });
    }

    private void layoutWeightDialog(JPanel overlay, RoundedDialogPanel card) {
        overlay.setBounds(0, 0, screenLayers.getWidth(), screenLayers.getHeight());
        int width = Math.min(470, Math.max(360, screenLayers.getWidth() * 2 / 5));
        int height = Math.min(330, Math.max(270, screenLayers.getHeight() * 9 / 20));
        card.setPreferredSize(new Dimension(width, height));
        card.setBounds((screenLayers.getWidth() - width) / 2,
                (screenLayers.getHeight() - height) / 2, width, height);
        card.revalidate();
    }

    private void closeWeightDialog(JPanel overlay) {
        if (weightDialogResizeListener != null) {
            screenLayers.removeComponentListener(weightDialogResizeListener);
            weightDialogResizeListener = null;
        }
        screenLayers.remove(overlay);
        revalidate();
        repaint();
    }

    private static final class RoundedDialogPanel extends JPanel {
        private final Color background;
        private final int arc;

        private RoundedDialogPanel(Color background, int arc) {
            this.background = background;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            aa(g);
            g.setColor(background);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g.dispose();
        }
    }

    private static final class RoundedDialogButton extends JButton {
        private final Color background;
        private boolean hover;

        private RoundedDialogButton(String text, Color background) {
            super(text);
            this.background = background;
            setFont(font(Font.PLAIN, 22));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(s(155), s(54)));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            aa(g);
            g.setColor(getModel().isPressed() ? background.darker()
                    : hover ? background.brighter() : background);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), s(18), s(18));
            super.paintComponent(g);
            g.dispose();
        }
    }

}