import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.IntConsumer;

/**
 * HealthyCalories - หน้าหลัก (ข้อมูล)  เฉพาะ GUI ยังไม่มีฟังก์ชัน
 *
 * ไฟล์รูปที่ต้องใช้ (วางไว้ในโฟลเดอร์ images/ ข้างไฟล์นี้):
 *   images/girl.png            ตัวการ์ตูนกลางวงกลม
 *   images/couple.png          รูปคู่ด้านล่าง
 *   images/icon_pie.png        ไอคอนเมนู "ข้อมูล"
 *   images/icon_book.png       ไอคอนเมนู "บันทึก"
 *   images/icon_calendar.png   ไอคอนเมนู "ปฏิทิน"
 *
 * ขนาดหน้าต่างปรับอัตโนมัติให้พอดีกับหน้าจอ
 * ถ้าอยากกำหนดเอง ให้แก้ SCALE_OVERRIDE (เช่น 0.8 = เล็กลง, 1.0 = ขนาดเต็ม)
 *
 * ภายหลังจะใช้ไฟล์ CSV เป็นฐานข้อมูล (data/*.csv)
 */
public class Homemenu extends JFrame {

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
    static final String IMG_GIRL   = "images/girl.png";
    static final String IMG_COUPLE = "images/couple.png";
    static final String IMG_PIE    = "images/icon_pie.png";
    static final String IMG_BOOK   = "images/icon_book.png";
    static final String IMG_CAL    = "images/icon_calendar.png";

    // ---------- ขนาด ----------
    static final int BASE_W = 420, BASE_H = 880;   // ขนาดที่ออกแบบไว้
    static final double MAX_SCALE = 0.9;           // ขนาดสูงสุด (ไม่ให้ใหญ่เกินไป)
    static final double SCALE_OVERRIDE = 0;        // 0 = อัตโนมัติ, หรือใส่เอง เช่น 0.8
    static double S = 1.0;                         // อัตราส่วนที่ใช้จริง

    static void initScale() {
        if (SCALE_OVERRIDE > 0) { S = SCALE_OVERRIDE; return; }
        Rectangle b = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double fit = Math.min((b.height - 50.0) / BASE_H, (b.width - 20.0) / BASE_W);
        S = Math.max(0.5, Math.min(MAX_SCALE, fit));
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
        String[] files = {path, name, "src/" + path, "src/" + name,
                          "images/" + name, "img/" + name, "resources/" + name};
        for (String f : files) {
            File file = new File(f);
            if (file.isFile()) {
                try { return ImageIO.read(file); } catch (Exception ignored) { }
            }
        }
        String[] res = {"/" + path, "/" + name, "/images/" + name};
        for (String r : res) {
            try (java.io.InputStream in = Homemenu.class.getResourceAsStream(r)) {
                if (in != null) return ImageIO.read(in);
            } catch (Exception ignored) { }
        }
        System.err.println("หาไฟล์รูปไม่เจอ: " + name
                + "  (โฟลเดอร์ที่รันอยู่ตอนนี้: " + new File("").getAbsolutePath() + ")");
        return null;
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
    //  ปุ่มเมนู (ขีด 2 ขีดซ้ายบน)
    // =====================================================
    static class MenuButton extends JButton {
        boolean hover;
        MenuButton() {
            flat(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            if (getModel().isPressed())      { g.setColor(new Color(255, 255, 255, 100)); g.fillRoundRect(0, 0, 54, 44, 14, 14); }
            else if (hover)                  { g.setColor(new Color(255, 255, 255, 55));  g.fillRoundRect(0, 0, 54, 44, 14, 14); }
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(12, 15, 42, 15);
            g.drawLine(12, 29, 42, 29);
            g.dispose();
        }
    }

    // =====================================================
    //  ส่วนหัว
    // =====================================================
    static class HeaderPanel extends JPanel {
        final MenuButton menu = new MenuButton();
        HeaderPanel() {
            setLayout(null);
            setBackground(PINK_BAR);
            JLabel title = label("HealthyCalories", Font.PLAIN, 32, Color.WHITE, 70, 18, 330, 44);
            title.setHorizontalAlignment(SwingConstants.CENTER);
            add(title);
            menu.setBounds(s(14), s(18), s(54), s(44));
            add(menu);
        }
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
        static final double CX = BASE_W / 2.0, CY = 150, R = 130, r = 92;
        static final String[] MEALS   = {"อาหารเช้า", "อาหารเที่ยง", "อาหารว่าง", "อาหารเย็น"};
        static final int[]    CENTERS = {90, 0, 270, 180};                // องศาแบบ Java2D
        static final double[] ROT     = {0, Math.PI / 2, 0, -Math.PI / 2};

        final BufferedImage girl = loadFit(IMG_GIRL, s(70), s(70));
        final Area[] segs = new Area[4];
        final ArrowButton prev = new ArrowButton(true);
        final ArrowButton next = new ArrowButton(false);
        int hover = -1, pressed = -1;

        /** ถูกเรียกเมื่อกดมื้ออาหาร: 0=เช้า 1=เที่ยง 2=ว่าง 3=เย็น (ใส่ฟังก์ชันภายหลัง) */
        IntConsumer onMealClick = i -> { };

        RingPanel() {
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
            g.setFont(fontBase(Font.PLAIN, 18));
            drawCentered(g, "วันนี้", CX, CY - 52);
            g.setColor(new Color(0x555555));
            g.setFont(fontBase(Font.PLAIN, 14));
            drawCentered(g, "(เหลือแคลอรี่ทานได้อีก)", CX, CY - 32);

            g.setColor(PURPLE);
            g.setFont(fontBase(Font.PLAIN, 46));
            drawCentered(g, "0", CX, CY + 10);

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
        static final int CW = 370, CH = 150, STRIP_Y = 105, LINE_Y = 66;
        final JButton saveBtn = new JButton("บันทึกน้ำหนัก");
        final HistoryButton historyBtn = new HistoryButton();

        void addLine(int x, int w, int gap, boolean center, String[] t, int[] sz, Color[] c) {
            TextLine line = new TextLine(46, gap, center, t, sz, c);
            line.setBounds(s(x), s(LINE_Y - 46), s(w), s(56));
            add(line);
        }

        WeightCard() {
            setLayout(null);
            setOpaque(false);

            historyBtn.setBounds(s(62), s(6), s(120), s(24));
            add(historyBtn);

            addLine(30, 125, 6, false, new String[]{"0", "/ 0 กก."},
                    new int[]{44, 16}, new Color[]{Color.BLACK, GRAY_TEXT});

            saveBtn.setFont(font(Font.PLAIN, 13));
            saveBtn.setBackground(PINK_BTN);
            saveBtn.setForeground(Color.BLACK);
            saveBtn.setFocusPainted(false);
            saveBtn.setBorder(BorderFactory.createLineBorder(new Color(0xE79BE0)));
            saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            saveBtn.setBounds(s(28), s(70), s(125), s(30));
            add(saveBtn);

            JPanel bar = new JPanel();
            bar.setBackground(new Color(0x777777));
            bar.setBounds(s(172), s(30), Math.max(2, s(5)), s(66));
            add(bar);

            add(label("แคลอรี่ที่ได้รับจากอาหาร", Font.PLAIN, 13, GRAY_TEXT, 188, 22, 175, 22));
            addLine(188, 175, 6, true, new String[]{"0", "Kcal"},
                    new int[]{13, 14}, new Color[]{PURPLE, Color.BLACK});

            String[][] cols = {{"0%", "ความก้าวหน้า"}, {"BMI 0.0", "มาตรฐาน"}, {"0", "BMR"}};
            for (int i = 0; i < 3; i++) {
                StripCell cell = new StripCell(cols[i][0], cols[i][1]);
                cell.setBounds(s(i * 123), s(STRIP_Y), s(124), s(45));
                add(cell);
            }
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            g.scale(S, S);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5, CW - 1, CH - 1, 28, 28);
            g.setColor(Color.WHITE);
            g.fill(shape);
            Shape old = g.getClip();
            g.clip(shape);
            g.setColor(PURPLE);
            g.fillRect(0, STRIP_Y, CW, CH - STRIP_Y);
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
        int value = 0; // 0-100 (ยังไม่ใช้งาน)
        GoalBar() { setOpaque(false); }
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
        boolean hover;

        NavButton(String text, String imgPath, boolean active) {
            this.text = text;
            this.active = active;
            this.img = loadFit(imgPath, s(40), s(40));
            flat(this);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create(); aa(g);
            AffineTransform orig = g.getTransform();
            g.scale(S, S);
            double w = getWidth() / S;
            if (getModel().isPressed())  { g.setColor(new Color(255, 255, 255, 80)); g.fillRoundRect(2, 2, (int) w - 4, 66, 18, 18); }
            else if (hover)              { g.setColor(new Color(255, 255, 255, 45)); g.fillRoundRect(2, 2, (int) w - 4, 66, 18, 18); }
            g.setFont(fontBase(Font.BOLD, 15));
            g.setColor(active ? PINK_ACTIVE : Color.WHITE);
            drawCentered(g, text, w / 2, 62);
            g.setTransform(orig);
            if (img != null) {
                g.drawImage(img, (getWidth() - img.getWidth()) / 2,
                        (int) Math.round(28 * S - img.getHeight() / 2.0), null);
            }
            g.dispose();
        }
    }

    static class NavBar extends JPanel {
        final NavButton info  = new NavButton("ข้อมูล", IMG_PIE, true);
        final NavButton diary = new NavButton("บันทึก", IMG_BOOK, false);
        final NavButton cal   = new NavButton("ปฏิทิน", IMG_CAL, false);

        NavBar() {
            setLayout(null);
            setBackground(PINK_BAR);
            place(info, 52);
            place(diary, 130);
            place(cal, 340);
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
    public Homemenu() {
        setTitle("HealthyCalories");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(s(BASE_W), s(BASE_H)));

        JPanel main = new JPanel(null);
        main.setBackground(Color.WHITE);
        main.setBounds(0, 0, s(BASE_W), s(BASE_H));

        // ส่วนหัว + ปุ่มเมนู
        HeaderPanel header = new HeaderPanel();
        header.setBounds(0, 0, s(BASE_W), s(80));
        header.menu.addActionListener(e -> System.out.println("กดปุ่มเมนู"));   // TODO: เปิดเมนู
        main.add(header);

        // วงแหวนมื้ออาหาร
        RingPanel ring = new RingPanel();
        ring.setBounds(0, s(100), s(BASE_W), s(290));
        ring.onMealClick = i -> System.out.println("กด " + RingPanel.MEALS[i]);   // TODO: เพิ่มอาหาร
        main.add(ring);

        ring.prev.addActionListener(e -> System.out.println("กดปุ่ม <"));   // TODO: วันก่อนหน้า
        ring.next.addActionListener(e -> System.out.println("กดปุ่ม >"));   // TODO: วันถัดไป

        // การ์ดน้ำหนัก
        WeightCard card = new WeightCard();
        card.setBounds(s(25), s(395), s(WeightCard.CW), s(WeightCard.CH));
        card.saveBtn.addActionListener(e -> System.out.println("กดบันทึกน้ำหนัก"));   // TODO
        card.historyBtn.addActionListener(e -> System.out.println("กดประวัติน้ำหนัก"));   // TODO: เปิดหน้าประวัติ
        main.add(card);

        // เป้าหมาย
        PinIcon pin = new PinIcon();
        pin.setBounds(s(28), s(552), s(14), s(18));
        main.add(pin);
        main.add(label("เป้าหมายอีก 0 วัน", Font.PLAIN, 12, Color.BLACK, 46, 553, 200, 18));
        GoalBar goal = new GoalBar();
        goal.setBounds(s(28), s(576), s(364), s(10));
        main.add(goal);

        // รูปคู่ด้านล่าง
        BufferedImage couple = loadFit(IMG_COUPLE, s(330), s(200));
        if (couple != null) {
            JLabel cl = new JLabel(new ImageIcon(couple), SwingConstants.CENTER);
            cl.setBounds(0, s(596), s(BASE_W), s(200));
            main.add(cl);
        }

        // แถบเมนูล่าง
        NavBar nav = new NavBar();
        nav.setBounds(0, s(810), s(BASE_W), s(70));
        nav.info.addActionListener(e -> System.out.println("ไปหน้า ข้อมูล"));
        nav.diary.addActionListener(e -> System.out.println("ไปหน้า บันทึก"));
        nav.cal.addActionListener(e -> System.out.println("ไปหน้า ปฏิทิน"));
        main.add(nav);

        layered.add(main, JLayeredPane.DEFAULT_LAYER);

        // ปุ่ม + (ลอยทับแถบเมนู)
        FabButton fab = new FabButton();
        fab.setBounds(s(210 - 40), s(810 - 40), s(84), s(84));
        fab.addActionListener(e -> System.out.println("กดปุ่ม +"));   // TODO
        layered.add(fab, JLayeredPane.PALETTE_LAYER);

        setContentPane(layered);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        initScale();
        SwingUtilities.invokeLater(() -> new Homemenu().setVisible(true));
    }
}