package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class WeightHistoryScreen extends BaseScreen {

    // ---------- ขนาดหน้าจอตามดีไซน์ ----------
    static final int W = 1018, H = 707;

    // ---------- สี ----------
    static final Color HEADER   = new Color(0xB091D8);
    static final Color BAND     = new Color(0xF0D4F2);
    static final Color PURPLE   = new Color(0x9B6FD0);
    static final Color DARK     = new Color(0x1A1A1A);
    static final Color HOVER    = new Color(0xF6F0FB);

    static final String[] MON = {"ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.",
                                 "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."};
    static final String[] MON_FULL = {"มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
                                      "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"};

    // ---------- ตำแหน่ง/ขนาดของแต่ละส่วน ----------
    final Rectangle BACK       = new Rectangle(0, 0, 80, 66);
    final Rectangle INFO       = new Rectangle(372, 115, 278, 59);
    final Rectangle DATE_BTN   = new Rectangle(INFO.x, INFO.y, INFO.width / 2, INFO.height);
    final Rectangle WEIGHT_BTN = new Rectangle(INFO.x + INFO.width / 2, INFO.y, INFO.width - INFO.width / 2, INFO.height);
    final Rectangle CARD       = new Rectangle(372, 221, 278, 158);
    final Rectangle PREV       = new Rectangle(CARD.x + 80, CARD.y, 26, 29);
    final Rectangle NEXT       = new Rectangle(CARD.x + 172, CARD.y, 26, 29);

    // ---------- ข้อมูล ----------
    static final Path FILE = Paths.get(System.getProperty("user.home"), ".healthycalories_weights.csv");
    final TreeMap<LocalDate, Double> data = new TreeMap<>();

    // เปลี่ยนเป็น LocalDate.now() ถ้าอยากให้เริ่มที่วันนี้ (ในดีไซน์ใช้ 25 ก.ย. 2569)
    LocalDate selected    = LocalDate.of(2026, 9, 25);
    LocalDate windowStart = selected;

    Rectangle hover = null;
    BufferedImage mascot = loadMascot();

    public WeightHistoryScreen() {
        this(null, null);
    }

    public WeightHistoryScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        load();

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Rectangle h = buttonAt(e.getPoint());
                boolean clickable = h != null || columnAt(e.getPoint()) >= 0;
                setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (h != hover) { hover = h; repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (hover != null) { hover = null; repaint(); }
            }
            @Override public void mouseClicked(MouseEvent e) { onClick(e.getPoint()); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    @Override
    public void buildUI() {
        removeAll();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        revalidate();
        repaint();
    }

    // =====================================================================
    //  การโต้ตอบ
    // =====================================================================
    Rectangle buttonAt(Point p) {
        if (BACK.contains(p)) return BACK;
        if (DATE_BTN.contains(p)) return DATE_BTN;
        if (WEIGHT_BTN.contains(p)) return WEIGHT_BTN;
        if (PREV.contains(p)) return PREV;
        if (NEXT.contains(p)) return NEXT;
        return null;
    }

    /** คืนค่า index 0-5 ของคอลัมน์วันที่ในกราฟที่ถูกกด, -1 ถ้าไม่ใช่ */
    int columnAt(Point p) {
        if (p.y < CARD.y + 30 || p.y > CARD.y + CARD.height) return -1;
        if (p.x < CARD.x + 20 || p.x > CARD.x + CARD.width) return -1;
        int i = (int) Math.round((p.x - colX(0)) / 33.0);
        if (i < 0 || i > 5) return -1;
        return Math.abs(p.x - colX(i)) <= 16.5 ? i : -1;
    }

    void onClick(Point p) {
        Rectangle b = buttonAt(p);
        if (b == BACK) {
            // TODO: เปลี่ยนเป็นโค้ดกลับไปหน้าก่อนหน้าของโปรเจกต์
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) w.dispose();
        } else if (b == DATE_BTN) {
            pickDate();
        } else if (b == WEIGHT_BTN) {
            editWeight();
        } else if (b == PREV) {
            windowStart = windowStart.minusDays(6);
            repaint();
        } else if (b == NEXT) {
            windowStart = windowStart.plusDays(6);
            repaint();
        } else {
            int c = columnAt(p);
            if (c >= 0) selectDate(windowStart.plusDays(c));
        }
    }

    void selectDate(LocalDate d) {
        selected = d;
        if (d.isBefore(windowStart) || d.isAfter(windowStart.plusDays(5))) windowStart = d;
        repaint();
    }

    void pickDate() {
        JComboBox<Integer> day = new JComboBox<>();
        for (int d = 1; d <= 31; d++) day.addItem(d);
        JComboBox<String> mon = new JComboBox<>(MON_FULL);
        JComboBox<Integer> year = new JComboBox<>();
        int nowBE = LocalDate.now().getYear() + 543;
        for (int y = nowBE - 30; y <= nowBE + 1; y++) year.addItem(y);

        day.setSelectedItem(selected.getDayOfMonth());
        mon.setSelectedIndex(selected.getMonthValue() - 1);
        year.setSelectedItem(selected.getYear() + 543);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        p.add(day); p.add(mon); p.add(year);

        int r = JOptionPane.showConfirmDialog(this, p, "เปลี่ยนวันที่",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            int y = (Integer) year.getSelectedItem() - 543;
            int m = mon.getSelectedIndex() + 1;
            int d = Math.min((Integer) day.getSelectedItem(), YearMonth.of(y, m).lengthOfMonth());
            selectDate(LocalDate.of(y, m, d));
        }
    }

    void editWeight() {
        Double cur = data.get(selected);
        JSpinner sp = new JSpinner(new SpinnerNumberModel(cur != null ? cur : 60.0, 1.0, 500.0, 0.1));
        sp.setEditor(new JSpinner.NumberEditor(sp, "0.0"));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        p.add(new JLabel("น้ำหนัก"));
        p.add(sp);
        p.add(new JLabel("กิโลกรัม"));

        String[] opts = cur != null ? new String[]{"บันทึก", "ลบข้อมูล", "ยกเลิก"}
                                    : new String[]{"บันทึก", "ยกเลิก"};
        int r = JOptionPane.showOptionDialog(this, p, "เปลี่ยนน้ำหนัก  " + dateText(selected),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);

        if (r == 0) {
            try { sp.commitEdit(); } catch (ParseException ignored) { }
            double v = ((Number) sp.getValue()).doubleValue();
            data.put(selected, Math.round(v * 10) / 10.0);
            save();
        } else if (cur != null && r == 1) {
            data.remove(selected);
            save();
        }
        repaint();
    }

    // =====================================================================
    //  วาดหน้าจอ
    // =====================================================================
    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        paintHeader(g);
        paintInfo(g);
        paintChart(g);

        if (mascot != null) {
            int mw = 296, mh = mascot.getHeight() * mw / mascot.getWidth();
            g.drawImage(mascot, (W - mw) / 2, 462, mw, mh, null);
        }
        g.dispose();
    }

    void paintHeader(Graphics2D g) {
        g.setColor(HEADER);
        g.fillRect(0, 0, W, 66);

        g.setFont(font(Font.PLAIN, 32));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        centered(g, "ประวัติน้ำหนัก", W / 2.0, 33 + (fm.getAscent() - fm.getDescent()) / 2.0);

        // ลูกศรย้อนกลับ
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(12, 33, 56, 33);
        g.drawLine(12, 33, 25, 20);
        g.drawLine(12, 33, 25, 46);
    }

    void paintInfo(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(INFO.x, INFO.y, INFO.width, INFO.height);
        if (hover == DATE_BTN || hover == WEIGHT_BTN) {
            g.setColor(HOVER);
            g.fillRect(hover.x + 1, hover.y + 1, hover.width - 2, hover.height - 2);
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0x6F6F6F));
        g.drawRect(INFO.x, INFO.y, INFO.width, INFO.height);
        int mid = INFO.x + INFO.width / 2;
        g.drawLine(mid, INFO.y + 7, mid, INFO.y + INFO.height - 7);

        halfLabel(g, "เปลี่ยนวันที่", dateText(selected), INFO.x + INFO.width / 4.0);
        halfLabel(g, "เปลี่ยนน้ำหนัก", weightText(), INFO.x + INFO.width * 3 / 4.0);
    }

    void halfLabel(Graphics2D g, String label, String value, double cx) {
        g.setFont(font(Font.PLAIN, 8));
        g.setColor(new Color(0x555555));
        double tw = g.getFontMetrics().stringWidth(label);
        double x0 = cx - (8 + 3 + tw) / 2;
        pencil(g, x0, INFO.y + 8, 8);
        g.drawString(label, (float) (x0 + 11), (float) (INFO.y + 16));

        g.setFont(font(Font.PLAIN, 16));
        g.setColor(DARK);
        centered(g, value, cx, INFO.y + 42);
    }

    void paintChart(Graphics2D g) {
        // การ์ด + แถบหัวสีม่วงอ่อน
        RoundRectangle2D card = new RoundRectangle2D.Double(CARD.x + .5, CARD.y + .5, CARD.width - 1, CARD.height - 1, 8, 8);
        g.setColor(Color.WHITE);
        g.fill(card);
        Shape oldClip = g.getClip();
        g.clip(card);
        g.setColor(BAND);
        g.fillRect(CARD.x, CARD.y, CARD.width, 29);
        g.setClip(oldClip);
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0xE2E2E2));
        g.draw(card);

        // หัวกราฟ  <  ประวัติน้ำหนัก  >
        g.setFont(font(Font.PLAIN, 8));
        g.setColor(new Color(0x5A4A66));
        centered(g, "ประวัติน้ำหนัก", CARD.x + CARD.width / 2.0, CARD.y + 17);
        g.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        chevron(g, CARD.x + 93, CARD.y + 14.5, -1);
        chevron(g, CARD.x + 185, CARD.y + 14.5, 1);

        final int ax = CARD.x + 20, right = CARD.x + 250;
        final int top = CARD.y + 56, bottom = CARD.y + 141;

        // หาช่วงค่าของแกน Y จากข้อมูลใน 6 วันที่แสดง
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        boolean any = false;
        for (int i = 0; i < 6; i++) {
            Double v = data.get(windowStart.plusDays(i));
            if (v != null) { any = true; min = Math.min(min, v); max = Math.max(max, v); }
        }
        double lo = 0, hi = 100;
        if (any) {
            lo = Math.floor(min) - 2;
            hi = Math.ceil(max) + 2;
            double span = Math.max(4, Math.ceil((hi - lo) / 4) * 4);
            hi = lo + span;
            if (lo < 0) { hi -= lo; lo = 0; }
        }

        // ไฮไลต์คอลัมน์ของวันที่ที่เลือก
        int sel = (int) (selected.toEpochDay() - windowStart.toEpochDay());
        if (sel >= 0 && sel <= 5) {
            g.setColor(new Color(0xF5ECFB));
            g.fill(new Rectangle2D.Double(colX(sel) - 16.5, top, 33, bottom - top));
        }

        // เส้นกริด 5 เส้น + ตัวเลขแกน Y (แสดงเมื่อมีข้อมูล)
        for (int i = 0; i < 5; i++) {
            int y = bottom - 18 * i;
            g.setStroke(new BasicStroke(0.8f));
            g.setColor(new Color(0x8C8C8C));
            g.drawLine(ax, y, right, y);
            if (any) {
                g.setFont(font(Font.PLAIN, 6));
                g.setColor(new Color(0x888888));
                String t = fmt(lo + i * (hi - lo) / 4);
                g.drawString(t, right - 2 - g.getFontMetrics().stringWidth(t), y - 2);
            }
        }
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(0x333333));
        g.drawLine(ax, top, ax, bottom);

        // ป้าย "กิโลกรัม" แนวตั้ง
        AffineTransform old = g.getTransform();
        g.translate(CARD.x + 14, (top + bottom) / 2.0);
        g.rotate(-Math.PI / 2);
        g.setFont(font(Font.PLAIN, 6));
        g.setColor(new Color(0x555555));
        centered(g, "กิโลกรัม", 0, 0);
        g.setTransform(old);

        // วันที่แกน X
        for (int i = 0; i < 6; i++) {
            LocalDate d = windowStart.plusDays(i);
            boolean isSel = i == sel;
            g.setFont(font(isSel ? Font.BOLD : Font.PLAIN, 7));
            g.setColor(isSel ? new Color(0x7B4FB5) : new Color(0x555555));
            centered(g, d.getDayOfMonth() + " " + MON[d.getMonthValue() - 1], colX(i), CARD.y + 152);
        }

        // เส้นกราฟ + จุด + ค่าน้ำหนัก
        Path2D line = new Path2D.Double();
        List<Point2D.Double> pts = new ArrayList<>();
        List<Double> vals = new ArrayList<>();
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Double v = data.get(windowStart.plusDays(i));
            if (v == null) continue;
            double px = colX(i);
            double py = bottom - (v - lo) / (hi - lo) * 72.0;
            if (pts.isEmpty()) line.moveTo(px, py); else line.lineTo(px, py);
            pts.add(new Point2D.Double(px, py));
            vals.add(v);
            idxs.add(i);
        }
        if (pts.size() > 1) {
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(PURPLE);
            g.draw(line);
        }
        for (int k = 0; k < pts.size(); k++) {
            Point2D.Double p = pts.get(k);
            boolean isSel = idxs.get(k) == sel;
            double r = isSel ? 4 : 3;
            Ellipse2D dot = new Ellipse2D.Double(p.x - r, p.y - r, 2 * r, 2 * r);
            g.setColor(isSel ? PURPLE : Color.WHITE);
            g.fill(dot);
            g.setStroke(new BasicStroke(1.4f));
            g.setColor(PURPLE);
            g.draw(dot);
            g.setFont(font(Font.BOLD, 7));
            g.setColor(new Color(0x5B3F8E));
            centered(g, fmt(vals.get(k)), p.x, p.y - 7);
        }
    }

    // =====================================================================
    //  ตัวช่วยวาด / จัดรูปแบบ
    // =====================================================================
    double colX(int i) { return CARD.x + 35 + 33.0 * i; }

    static String dateText(LocalDate d) {
        return d.getDayOfMonth() + " " + MON[d.getMonthValue() - 1] + " " + (d.getYear() + 543);
    }

    String weightText() {
        Double v = data.get(selected);
        return (v == null ? "0" : fmt(v)) + " กิโลกรัม";
    }

    static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.format(Locale.US, "%.1f", v);
    }

    static void centered(Graphics2D g, String s, double cx, double baseline) {
        g.drawString(s, (float) (cx - g.getFontMetrics().stringWidth(s) / 2.0), (float) baseline);
    }

    static void chevron(Graphics2D g, double x, double y, int dir) {
        Path2D p = new Path2D.Double();
        p.moveTo(x - dir * 2.5, y - 4);
        p.lineTo(x + dir * 2.5, y);
        p.lineTo(x - dir * 2.5, y + 4);
        g.draw(p);
    }

    static void pencil(Graphics2D g, double x, double y, double s) {
        AffineTransform old = g.getTransform();
        Stroke os = g.getStroke();
        g.translate(x + s / 2, y + s / 2);
        g.rotate(Math.toRadians(45));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Rectangle2D.Double(-s * 0.14, -s * 0.5, s * 0.28, s * 0.75));
        Path2D tip = new Path2D.Double();
        tip.moveTo(-s * 0.14, s * 0.25);
        tip.lineTo(0, s * 0.5);
        tip.lineTo(s * 0.14, s * 0.25);
        tip.closePath();
        g.draw(tip);
        g.setTransform(old);
        g.setStroke(os);
    }

    // ---------- ฟอนต์ที่รองรับภาษาไทย ----------
    static final String FAMILY = pickFamily();

    static String pickFamily() {
        Set<String> have = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String f : new String[]{"Sarabun", "Leelawadee UI", "Tahoma", "Thonburi",
                                     "Sukhumvit Set", "Noto Sans Thai", "Loma"}) {
            if (have.contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }

    static Font font(int style, float size) {
        return new Font(FAMILY, style, Math.round(size)).deriveFont(size);
    }

    // =====================================================================
    //  โหลด/บันทึกข้อมูล และรูปมาสคอต
    // =====================================================================
    void load() {
        try {
            for (String l : Files.readAllLines(FILE)) {
                String[] s = l.split(",");
                if (s.length == 2) data.put(LocalDate.parse(s[0].trim()), Double.parseDouble(s[1].trim()));
            }
        } catch (Exception ignored) { }
    }

    void save() {
        try {
            List<String> lines = new ArrayList<>();
            for (var e : data.entrySet()) lines.add(e.getKey() + "," + e.getValue());
            Files.write(FILE, lines);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "บันทึกข้อมูลไม่สำเร็จ: " + ex.getMessage());
        }
    }

    BufferedImage loadMascot() {
        try {
            File f = new File("mascot.png");
            if (f.exists()) return ImageIO.read(f);
            java.net.URL u = getClass().getResource("/mascot.png");
            if (u != null) return ImageIO.read(u);
        } catch (IOException ignored) { }
        return null;
    }

}