package main.java.healthycalories.app.ui.screen;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * หน้า "ปฏิทิน" (แนวนอน 1020 x 708 ตามดีไซน์ใน Figma)
 *  - ลูกศรสีชมพูซ้าย/ขวา (หรือปุ่มคีย์บอร์ด ← →) เลื่อนเดือน
 *  - กดที่ชื่อเดือน เพื่อเลือกเดือน / กดที่ปี พ.ศ. เพื่อพิมพ์แก้ปี
 *  - ตารางวันที่คำนวณจากปฏิทินจริง (เริ่มวันอาทิตย์, 6 แถว) วันนี้จะเป็นวงกลมสีน้ำเงิน + ช่องสีเหลือง
 *    และเปลี่ยนตามวันที่จริงของเครื่องโดยอัตโนมัติ (ข้ามเที่ยงคืนก็เปลี่ยนเอง)
 *  - วันที่มีข้อมูลน้ำหนัก = ช่องสีเหลือง, มีข้อมูลแคลลอรี่ = ช่องสีชมพู (อ่านจากไฟล์ใน home ของผู้ใช้)
 *  - กดวงกลมแคลลอรี่ / น้ำหนัก ด้านล่าง เพื่อเปิด-ปิดการแสดงสี
 */
public class CalendarScreen extends JPanel {

    static final int W = 1020, H = 708;

    // ---------- สี ----------
    static final Color BLUE       = new Color(0x4F8FE8);
    static final Color TODAY_DOT  = new Color(0x6C9BEF);
    static final Color PINK       = new Color(0xF0A5EE);
    static final Color PINK_CELL  = new Color(0xF6C3F2);
    static final Color YELLOW     = new Color(0xF2CB7A);
    static final Color YELLOW_CELL= new Color(0xF8D68C);
    static final Color GRID       = new Color(0xE6E6E6);
    static final Color GRAY_BAND  = new Color(0x797979);
    static final Color NAV_BAR    = new Color(0xC8A4C8);
    static final Color FAB        = new Color(0xE9A5EA);
    static final Color NAV_ACTIVE = new Color(0xF2A2F2);

    static final String[] MON_FULL = {"มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
                                      "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"};
    static final String[] DOW = {"อา", "จ", "อ", "พ", "พฤ", "ศ", "ส"};

    // ---------- เรขาคณิต (หน่วยเดียวกับดีไซน์) ----------
    static final int CX = 522;                       // กึ่งกลางเนื้อหา
    static final double GX = 355, GY = 108;          // มุมซ้ายบนของตาราง
    static final double CW = 334 / 7.0, RH = 316 / 6.0;

    final Rectangle LEFT     = new Rectangle(272, 25, 52, 40);
    final Rectangle RIGHT    = new Rectangle(712, 25, 52, 40);
    final Rectangle MONTH    = new Rectangle(CX - 55, 12, 110, 32);
    final Rectangle YEAR     = new Rectangle(CX - 32, 44, 64, 18);
    final Rectangle PINK_BTN = new Rectangle(444, 434, 40, 40);
    final Rectangle YEL_BTN  = new Rectangle(558, 434, 40, 40);
    final Rectangle NAV_DATA = new Rectangle(356, 648, 60, 58);
    final Rectangle NAV_LOG  = new Rectangle(422, 648, 60, 58);
    final Rectangle NAV_CAL  = new Rectangle(571, 648, 60, 58);
    final Rectangle FAB_BTN  = new Rectangle(490, 616, 64, 64);
    final Rectangle[] ALL = {LEFT, RIGHT, MONTH, YEAR, PINK_BTN, YEL_BTN, NAV_DATA, NAV_LOG, NAV_CAL, FAB_BTN};

    // ---------- สถานะ ----------
    YearMonth ym = YearMonth.now();
    boolean showCal = true, showWeight = true;
    Rectangle hover = null;

    static final Path W_FILE = Paths.get(System.getProperty("user.home"), ".healthycalories_weights.csv");
    static final Path C_FILE = Paths.get(System.getProperty("user.home"), ".healthycalories_calories.csv");
    Set<LocalDate> weightDays = new HashSet<>();
    Set<LocalDate> calDays = new HashSet<>();

    /** ผูกกับเมนูล่างได้: "data", "log", "add", "calendar" */
    public Consumer<String> onNavigate = name -> { };

    public CalendarScreen() {
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        reload();

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Rectangle h = buttonAt(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(h != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (h != hover) { hover = h; repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (hover != null) { hover = null; repaint(); }
            }
            @Override public void mouseClicked(MouseEvent e) { onClick(buttonAt(e.getPoint())); }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // ปุ่มลูกศรบนคีย์บอร์ด
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "prev");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "next");
        getActionMap().put("prev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { shift(-1); }
        });
        getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { shift(1); }
        });

        // อัปเดตวันที่จริง/ข้อมูลทุก 30 วินาที (ข้ามเที่ยงคืนแล้ววงกลม "วันนี้" จะย้ายเอง)
        new javax.swing.Timer(30_000, e -> { reload(); repaint(); }).start();
    }

    // =====================================================================
    //  การโต้ตอบ
    // =====================================================================
    Rectangle buttonAt(Point p) {
        for (Rectangle r : ALL) if (r.contains(p)) return r;
        return null;
    }

    void onClick(Rectangle b) {
        if (b == null) return;
        if (b == LEFT) shift(-1);
        else if (b == RIGHT) shift(1);
        else if (b == MONTH) pickMonth();
        else if (b == YEAR) pickYear();
        else if (b == PINK_BTN) { showCal = !showCal; repaint(); }
        else if (b == YEL_BTN) { showWeight = !showWeight; repaint(); }
        else if (b == NAV_CAL) { ym = YearMonth.now(); repaint(); onNavigate.accept("calendar"); }
        else if (b == NAV_DATA) onNavigate.accept("data");
        else if (b == NAV_LOG) onNavigate.accept("log");
        else if (b == FAB_BTN) onNavigate.accept("add");
    }

    void shift(int months) {
        ym = ym.plusMonths(months);
        reload();
        repaint();
    }

    void pickMonth() {
        JComboBox<String> box = new JComboBox<>(MON_FULL);
        box.setSelectedIndex(ym.getMonthValue() - 1);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        p.add(box);
        int r = JOptionPane.showConfirmDialog(this, p, "เลือกเดือน",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            ym = YearMonth.of(ym.getYear(), box.getSelectedIndex() + 1);
            reload();
            repaint();
        }
    }

    void pickYear() {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(ym.getYear() + 543, 2400, 2800, 1));
        sp.setEditor(new JSpinner.NumberEditor(sp, "0"));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        p.add(new JLabel("ปี พ.ศ."));
        p.add(sp);
        int r = JOptionPane.showConfirmDialog(this, p, "แก้ไขปี พ.ศ.",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            try { sp.commitEdit(); } catch (java.text.ParseException ignored) { }
            int be = ((Number) sp.getValue()).intValue();
            ym = YearMonth.of(be - 543, ym.getMonthValue());
            reload();
            repaint();
        }
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

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        // แถบเทาด้านหลัง + แผ่นขาวมุมล่างโค้ง
        g.setColor(GRAY_BAND);
        g.fillRect(0, 476, W, 170);
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(0, -60, W, 576, 80, 80));

        paintHeader(g);
        paintGrid(g);
        paintLegend(g);
        paintNav(g);
        g.dispose();
    }

    void paintHeader(Graphics2D g) {
        String mn = MON_FULL[ym.getMonthValue() - 1];
        g.setFont(font(Font.PLAIN, 19));
        g.setColor(BLUE);
        centeredV(g, mn, CX, 29);
        if (hover == MONTH) underline(g, mn, CX, 42);

        String yr = String.valueOf(ym.getYear() + 543);
        g.setFont(font(Font.PLAIN, 12));
        g.setColor(new Color(0x666666));
        centeredV(g, yr, CX, 52);
        if (hover == YEAR) underline(g, yr, CX, 60);

        arrow(g, 298, 45, -1, hover == LEFT);
        arrow(g, 738, 45, 1, hover == RIGHT);
    }

    void paintGrid(Graphics2D g) {
        LocalDate today = LocalDate.now();
        LocalDate first = ym.atDay(1);
        int offset = first.getDayOfWeek().getValue() % 7;      // อาทิตย์ = 0
        LocalDate start = first.minusDays(offset);

        // หัวคอลัมน์วัน
        g.setFont(font(Font.PLAIN, 9));
        g.setColor(BLUE);
        for (int c = 0; c < 7; c++) centeredV(g, DOW[c], GX + CW * (c + .5), 92);

        // สีพื้นของแต่ละช่อง
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                LocalDate d = start.plusDays(r * 7L + c);
                boolean cal = showCal && calDays.contains(d);
                boolean yel = (showWeight && weightDays.contains(d)) || d.equals(today);
                double x = GX + c * CW, y = GY + r * RH;
                if (cal && yel) {
                    g.setColor(PINK_CELL);
                    g.fill(new Rectangle2D.Double(x, y, CW, RH / 2));
                    g.setColor(YELLOW_CELL);
                    g.fill(new Rectangle2D.Double(x, y + RH / 2, CW, RH / 2));
                } else if (cal || yel) {
                    g.setColor(cal ? PINK_CELL : YELLOW_CELL);
                    g.fill(new Rectangle2D.Double(x, y, CW, RH));
                }
            }
        }

        // เส้นตาราง
        g.setStroke(new BasicStroke(1f));
        g.setColor(GRID);
        for (int c = 0; c <= 7; c++) g.draw(new Line2D.Double(GX + c * CW, GY, GX + c * CW, GY + 6 * RH));
        for (int r = 0; r <= 6; r++) g.draw(new Line2D.Double(GX, GY + r * RH, GX + 7 * CW, GY + r * RH));

        // ตัวเลขวันที่
        g.setFont(font(Font.PLAIN, 9));
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                LocalDate d = start.plusDays(r * 7L + c);
                double nx = GX + c * CW + 9, ny = GY + r * RH + 9;
                String s = String.valueOf(d.getDayOfMonth());
                if (d.equals(today)) {
                    g.setColor(TODAY_DOT);
                    g.fill(new Ellipse2D.Double(nx - 12.5, ny - 12.5, 25, 25));
                    g.setColor(Color.WHITE);
                } else {
                    boolean inMonth = d.getMonthValue() == ym.getMonthValue();
                    g.setColor(inMonth ? new Color(0x4A4A4A) : new Color(0xC4C4C4));
                }
                centeredV(g, s, nx, ny);
            }
        }
    }

    void paintLegend(Graphics2D g) {
        legendItem(g, 464, PINK, "แคลลอรี่", showCal);
        legendItem(g, 578, YELLOW, "น้ำหนัก", showWeight);
    }

    void legendItem(Graphics2D g, double cx, Color c, String label, boolean on) {
        Composite old = g.getComposite();
        if (!on) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(c);
        g.fill(new Ellipse2D.Double(cx - 20, 454 - 20, 40, 40));
        g.setFont(font(Font.PLAIN, 12));
        centeredV(g, label, cx, 488);
        g.setComposite(old);
    }

    void paintNav(Graphics2D g) {
        g.setColor(NAV_BAR);
        g.fillRect(0, 638, W, 70);

        navItem(g, 386, "ข้อมูล", Color.WHITE, 0);
        navItem(g, 452, "บันทึก", Color.WHITE, 1);
        navItem(g, 601, "ปฏิทิน", NAV_ACTIVE, 2);

        // ปุ่ม +
        g.setColor(new Color(0, 0, 0, 40));
        g.fill(new Ellipse2D.Double(CX - 31, 648 - 28, 62, 62));
        g.setColor(FAB);
        g.fill(new Ellipse2D.Double(CX - 31, 648 - 31, 62, 62));
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(CX - 14, 648, CX + 14, 648));
        g.draw(new Line2D.Double(CX, 634, CX, 662));
    }

    void navItem(Graphics2D g, int cx, String label, Color c, int kind) {
        int cy = 673;
        g.setColor(c);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (kind == 0) {                               // กราฟวงกลม
            g.fill(new Arc2D.Double(cx - 10, cy - 9, 20, 20, 90, 270, Arc2D.PIE));
            g.fill(new Arc2D.Double(cx - 8, cy - 11, 20, 20, 0, 90, Arc2D.PIE));
        } else if (kind == 1) {                        // หนังสือเปิด
            Path2D l = new Path2D.Double();
            l.moveTo(cx - 13, cy - 8); l.lineTo(cx - 1, cy - 5); l.lineTo(cx - 1, cy + 10); l.lineTo(cx - 13, cy + 7); l.closePath();
            Path2D r = new Path2D.Double();
            r.moveTo(cx + 13, cy - 8); r.lineTo(cx + 1, cy - 5); r.lineTo(cx + 1, cy + 10); r.lineTo(cx + 13, cy + 7); r.closePath();
            g.fill(l);
            g.fill(r);
        } else {                                       // ปฏิทิน
            g.draw(new RoundRectangle2D.Double(cx - 12, cy - 9, 24, 21, 3, 3));
            g.draw(new Line2D.Double(cx - 12, cy - 3, cx + 12, cy - 3));
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{2f, 2f}, 0f));
            g.draw(new Line2D.Double(cx - 4, cy - 3, cx - 4, cy + 12));
            g.draw(new Line2D.Double(cx + 4, cy - 3, cx + 4, cy + 12));
            g.draw(new Line2D.Double(cx - 12, cy + 4, cx + 12, cy + 4));
        }
        g.setFont(font(Font.PLAIN, 10));
        g.setColor(c);
        centeredV(g, label, cx, 696);
    }

    // =====================================================================
    //  ตัวช่วย
    // =====================================================================
    static void arrow(Graphics2D g, double cx, double cy, int dir, boolean hot) {
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(hot ? new Color(0xEB7DE6) : new Color(0xF2A1EE));
        double tip = cx + dir * 15;
        g.draw(new Line2D.Double(cx - dir * 15, cy, tip, cy));
        g.draw(new Line2D.Double(tip, cy, tip - dir * 8, cy - 6));
        g.draw(new Line2D.Double(tip, cy, tip - dir * 8, cy + 6));
    }

    static void underline(Graphics2D g, String s, double cx, double y) {
        double w = g.getFontMetrics().stringWidth(s);
        g.setStroke(new BasicStroke(1f));
        g.draw(new Line2D.Double(cx - w / 2, y, cx + w / 2, y));
    }

    /** วางข้อความให้กึ่งกลางทั้งแนวนอนและแนวตั้งที่ (cx, cy) */
    static void centeredV(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
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

    // ---------- อ่านข้อมูลวันที่ที่มีบันทึก ----------
    void reload() {
        weightDays = readDates(W_FILE);
        calDays = readDates(C_FILE);
    }

    static Set<LocalDate> readDates(Path f) {
        Set<LocalDate> out = new HashSet<>();
        try {
            for (String l : Files.readAllLines(f)) {
                String[] s = l.split(",");
                if (s.length >= 1) out.add(LocalDate.parse(s[0].trim()));
            }
        } catch (Exception ignored) { }
        return out;
    }

    // =====================================================================
    //  main
    // =====================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            javax.swing.plaf.FontUIResource f = new javax.swing.plaf.FontUIResource(font(Font.PLAIN, 14));
            for (Object k : Collections.list(UIManager.getDefaults().keys())) {
                if (k.toString().endsWith(".font")) UIManager.put(k, f);
            }
            UIManager.put("OptionPane.okButtonText", "ตกลง");
            UIManager.put("OptionPane.cancelButtonText", "ยกเลิก");

            JFrame frame = new JFrame("HealthyCalories - ปฏิทิน");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new CalendarScreen());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}