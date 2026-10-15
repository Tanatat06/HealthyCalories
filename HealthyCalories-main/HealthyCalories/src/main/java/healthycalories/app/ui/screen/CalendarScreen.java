package main.java.healthycalories.app.ui.screen;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Consumer;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.model.log.DailyLog;
import main.java.healthycalories.model.log.FoodLog;
import main.java.healthycalories.model.user.Member;
import main.java.healthycalories.persistence.DailyCalorieRepository;
import main.java.healthycalories.persistence.FoodLogRepository;

/** หน้าปฏิทิน แสดงวันมีบันทึกอาหาร/น้ำหนัก และรายละเอียดเมื่อเลือกวันที่ */
public class CalendarScreen extends BaseScreen {

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

    static final String[] MON_FULL = {"มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน",
                                      "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"};
    static final String[] DOW = {"อา", "จ", "อ", "พ", "พฤ", "ศ", "ส"};

    // ---------- ตำแหน่งส่วนต่าง ๆ บนปฏิทิน (หน่วยตามขนาดออกแบบ) ----------
    static final int CX = 522;                       // กึ่งกลางหน้า ใช้จัดชื่อเดือน/ปีให้อยู่ตรงกลาง
    static final double GX = 355, GY = 108;          // จุดเริ่มต้นมุมซ้ายบนของตารางวันที่
    static final double CW = 334 / 7.0, RH = 316 / 6.0; // ความกว้างช่องวันและความสูงแถว

    // พื้นที่คลิกของปุ่มลูกศรซ้ายบน: ย้อนกลับไปเดือนก่อน
    final Rectangle LEFT     = new Rectangle(272, 25, 52, 40);
    // พื้นที่คลิกของปุ่มลูกศรขวาบน: ไปเดือนถัดไป
    final Rectangle RIGHT    = new Rectangle(712, 25, 52, 40);
    // พื้นที่คลิกปุ่ม "วันนี้" ด้านขวาบน: กลับไปเดือนและวันที่ปัจจุบัน
    final Rectangle TODAY    = new Rectangle(800, 18, 72, 34);
    // พื้นที่คลิกชื่อเดือนตรงกลางด้านบน: เปิดตัวเลือกเดือน
    final Rectangle MONTH    = new Rectangle(CX - 55, 12, 110, 32);
    // พื้นที่คลิกปี พ.ศ. ใต้ชื่อเดือน: เปิดตัวเลือกปี
    final Rectangle YEAR     = new Rectangle(CX - 32, 44, 64, 18);
    // พื้นที่คลิกวงกลมสีชมพู "แคลอรี่" ใต้ตาราง: เปิด/ซ่อนวันที่มีบันทึกอาหาร
    final Rectangle PINK_BTN = new Rectangle(444, 434, 40, 40);
    // พื้นที่คลิกวงกลมสีเหลือง "น้ำหนัก" ใต้ตาราง: เปิด/ซ่อนวันที่มีบันทึกน้ำหนัก
    final Rectangle YEL_BTN  = new Rectangle(558, 434, 40, 40);
    final Rectangle[] ALL = {LEFT, RIGHT, TODAY, MONTH, YEAR, PINK_BTN, YEL_BTN};
    private HomeScreen.NavBar navBar;
    private HomeScreen.FabButton fabButton;
    private double navigationScale = -1;
    private int navigationLogicalWidth = -1;

    // ---------- สถานะ ----------
    YearMonth ym = YearMonth.now();
    LocalDate selectedDate = LocalDate.now();
    // เริ่มซ่อนสีของอาหารและน้ำหนัก จนกว่าผู้ใช้จะกดคำอธิบายสีด้านล่าง
    boolean showCal, showWeight;
    Rectangle hover = null;

    Set<LocalDate> weightDays = new HashSet<>();
    Set<LocalDate> calDays = new HashSet<>();

    /** ผูกกับเมนูล่างได้: "data", "log", "add", "calendar" */
    public Consumer<String> onNavigate = name -> { };

    public CalendarScreen() {
        this(null, null);
    }

    public CalendarScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        setLayout(null);
        if (frame != null) {
            onNavigate = name -> {
                if ("data".equals(name)) frame.showScreen(MainFrame.HOME);
                else if ("log".equals(name)) frame.showScreen(MainFrame.DIARY);
                else if ("add".equals(name)) frame.showScreen(MainFrame.FOOD);
                else if ("calendar".equals(name)) frame.showScreen(MainFrame.CALENDAR);
            };
        }
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        setToolTipText("");
        reload();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                layoutNavigation();
                repaint();
            }
        });
        layoutNavigation();

        // จัดการเมาส์บนพื้นที่วาดปฏิทิน: เปลี่ยนเคอร์เซอร์, ไฮไลต์ปุ่ม,
        // และตรวจว่าผู้ใช้คลิกปุ่มด้านบน/คำอธิบายสี/ช่องวันที่ใด
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                Rectangle h = buttonAt(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(h != null || dateAt(e.getPoint()) != null
                        ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                if (h != hover) { hover = h; repaint(); }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (hover != null) { hover = null; repaint(); }
            }
            @Override public void mouseClicked(MouseEvent e) {
                Rectangle button = buttonAt(e.getPoint());
                if (button != null) {
                    onClick(button);
                    return;
                }
                LocalDate date = dateAt(e.getPoint());
                if (date != null) {
                    // จดจำวันที่คลิกไว้ ทำกรอบเลือกสีฟ้า แล้วเปิดรายละเอียดของวันนั้น
                    selectedDate = date;
                    ym = YearMonth.from(date);
                    repaint();
                    showDateDetails(date);
                }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // คีย์ลัดแทนปุ่มบนหน้า: ซ้าย/ขวาเปลี่ยนเดือน, Home กลับวันนี้, Enter ดูวันที่เลือก
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "prev");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "next");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("HOME"), "today");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "showSelectedDate");
        getActionMap().put("prev", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { shift(-1); }
        });
        getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { shift(1); }
        });
        getActionMap().put("today", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { goToToday(); }
        });
        getActionMap().put("showSelectedDate", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { showDateDetails(selectedDate); }
        });

        // อัปเดตวันที่จริง/ข้อมูลทุก 30 วินาที (ข้ามเที่ยงคืนแล้ววงกลม "วันนี้" จะย้ายเอง)
        new javax.swing.Timer(30_000, e -> { reload(); repaint(); }).start();
    }

    @Override
    public void buildUI() {
        // รีโหลดวันมีบันทึก จัดตำแหน่งเมนูล่าง และวาดปฏิทินใหม่ทุกครั้งที่เปิดหน้า
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.WHITE);
        reload();
        layoutNavigation();
        revalidate();
        repaint();
    }

    // =====================================================================
    //  การโต้ตอบ
    // =====================================================================
    Rectangle buttonAt(Point p) {
        // แปลงพิกัดเมาส์ตามขนาดจริงของหน้าต่าง แล้วตรวจพื้นที่ปุ่มที่กำหนดไว้ด้านบน
        Point designPoint = toDesignPoint(p);
        for (Rectangle r : ALL) if (r.contains(designPoint)) return r;
        return null;
    }

    Point toDesignPoint(Point p) {
        // คืนพิกัดเมาส์จากขนาดหน้าต่างจริงเป็นพิกัดออกแบบ ใช้ตรวจปุ่มและช่องวันให้แม่น
        double scale = calendarScale();
        int offsetX = Math.max(0, (getWidth() - (int) Math.round(W * scale)) / 2);
        return new Point(
                (int) Math.floor((p.x - offsetX) / scale),
                (int) Math.floor(p.y / scale));
    }

    LocalDate dateAt(Point p) {
        // แปลงตำแหน่งเมาส์ในตารางให้เป็นวันจริง; คืน null ถ้าคลิกนอกตาราง
        Point point = toDesignPoint(p);
        int column = (int) ((point.x - GX) / CW);
        int row = (int) ((point.y - GY) / RH);
        if (point.x < GX || point.y < GY || column < 0 || column >= 7
                || row < 0 || row >= 6) {
            return null;
        }
        int offset = ym.atDay(1).getDayOfWeek().getValue() % 7;
        return ym.atDay(1).minusDays(offset).plusDays(row * 7L + column);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        // ข้อความแนะนำที่แสดงเมื่อเอาเมาส์ชี้ปุ่มหรือช่องวันที่
        Rectangle button = buttonAt(event.getPoint());
        if (button == LEFT) return "เดือนก่อนหน้า (←)";
        if (button == RIGHT) return "เดือนถัดไป (→)";
        if (button == TODAY) return "กลับไปเดือนปัจจุบัน (Home)";
        if (button == MONTH) return "เลือกเดือน";
        if (button == YEAR) return "เลือกปี พ.ศ.";
        if (button == PINK_BTN) return "แสดงหรือซ่อนวันที่มีบันทึกแคลอรี่";
        if (button == YEL_BTN) return "แสดงหรือซ่อนวันที่บันทึกน้ำหนัก";
        LocalDate date = dateAt(event.getPoint());
        return date == null ? null : "คลิกเพื่อดูบันทึกวันที่ " + formatThaiDate(date);
    }

    double calendarScale() {
        if (getWidth() <= 0 || getHeight() <= 0) return 1.0;
        return Math.max(0.35, Math.min(HomeScreen.MAX_SCALE,
                Math.min((double) getWidth() / W, (double) getHeight() / H)));
    }

    void layoutNavigation() {
        // สร้างและจัดตำแหน่งแถบเมนูล่าง (ข้อมูล/บันทึก/ปฏิทิน) กับปุ่ม + เพิ่มอาหาร
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;
        HomeScreen.updateSharedScale(width, height);
        double scale = HomeScreen.S;
        int logicalWidth = HomeScreen.BASE_W;
        if (navBar == null || Math.abs(navigationScale - scale) > 0.01
                || navigationLogicalWidth != logicalWidth) {
            if (navBar != null) remove(navBar);
            if (fabButton != null) remove(fabButton);
            navBar = new HomeScreen.NavBar("calendar");
            fabButton = new HomeScreen.FabButton();
            navBar.info.addActionListener(e -> onNavigate.accept("data"));
            navBar.diary.addActionListener(e -> onNavigate.accept("log"));
            navBar.cal.addActionListener(e -> onNavigate.accept("calendar"));
            fabButton.addActionListener(e -> onNavigate.accept("add"));
            add(navBar);
            add(fabButton);
            navigationScale = scale;
            navigationLogicalWidth = logicalWidth;
        }
        int navHeight = HomeScreen.s(70);
        int y = height - navHeight;
        navBar.setBounds(0, y, HomeScreen.s(logicalWidth), navHeight);
        fabButton.setBounds(HomeScreen.s(logicalWidth / 2 - 40),
                y - HomeScreen.s(40), HomeScreen.s(84), HomeScreen.s(84));
        setComponentZOrder(fabButton, 0);
        setComponentZOrder(navBar, 1);
    }

    void onClick(Rectangle b) {
        if (b == null) return;
        // จับคู่พื้นที่คลิกกับการทำงาน: ลูกศรเปลี่ยนเดือน, ปุ่มวันนี้/เดือน/ปีเลือกวันเวลา,
        // วงกลมชมพูและเหลืองเปิดหรือซ่อนสีวันที่มีบันทึกแต่ละชนิด
        if (b == LEFT) shift(-1);
        else if (b == RIGHT) shift(1);
        else if (b == TODAY) goToToday();
        else if (b == MONTH) pickMonth();
        else if (b == YEAR) pickYear();
        else if (b == PINK_BTN) { showCal = !showCal; repaint(); }
        else if (b == YEL_BTN) { showWeight = !showWeight; repaint(); }
    }

    void shift(int months) {
        // เปลี่ยนเดือนตามลูกศร โดยพยายามคงเลขวันที่เลือกไว้ และปรับถ้าเดือนไม่มีวันนั้น
        ym = ym.plusMonths(months);
        selectedDate = ym.atDay(Math.min(selectedDate.getDayOfMonth(), ym.lengthOfMonth()));
        reload();
        repaint();
    }

    void goToToday() {
        // ปุ่ม "วันนี้": ตั้งเดือนและวันที่เลือกให้เป็นวันปัจจุบัน
        selectedDate = LocalDate.now();
        ym = YearMonth.from(selectedDate);
        reload();
        repaint();
    }

    void pickMonth() {
        // เมื่อกดชื่อเดือนด้านบน เปิดกล่องเลือกเดือน แล้วอัปเดตปฏิทินเมื่อกดยืนยัน
        JComboBox<String> box = new JComboBox<>(MON_FULL);
        box.setSelectedIndex(ym.getMonthValue() - 1);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        p.add(box);
        int r = JOptionPane.showConfirmDialog(this, p, "เลือกเดือน",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            ym = YearMonth.of(ym.getYear(), box.getSelectedIndex() + 1);
            selectedDate = ym.atDay(Math.min(selectedDate.getDayOfMonth(), ym.lengthOfMonth()));
            reload();
            repaint();
        }
    }

    void pickYear() {
        // เมื่อกดปี พ.ศ. ด้านบน เปิดตัวเลือกปี พ.ศ. แล้วแปลงกลับเป็นปี ค.ศ. สำหรับ LocalDate
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
            selectedDate = ym.atDay(Math.min(selectedDate.getDayOfMonth(), ym.lengthOfMonth()));
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
        g.fillRect(0, 0, getWidth(), getHeight());
        double scale = calendarScale();
        int offsetX = Math.max(0, (getWidth() - (int) Math.round(W * scale)) / 2);
        g.translate(offsetX, 0);
        g.scale(scale, scale);

        // วาดแถบสีเทาด้านล่างและแผ่นพื้นสีขาวทรงโค้งที่รองรับเนื้อหาปฏิทิน
        g.setColor(GRAY_BAND);
        g.fill(new Rectangle2D.Double(-offsetX / scale, 476,
                getWidth() / scale, H - 476));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(-offsetX / scale, -60,
                getWidth() / scale, H - 132, 80, 80));

        paintHeader(g); // ชื่อเดือน/ปี ลูกศรเปลี่ยนเดือน และปุ่ม "วันนี้" ด้านบน
        paintGrid(g);   // หัววัน ตารางวันที่ สีบันทึก และวงกลมระบุวันปัจจุบัน
        paintLegend(g); // วงกลมเปิด/ซ่อนสีแคลอรี่และน้ำหนักใต้ตาราง
        g.dispose();
    }

    void paintHeader(Graphics2D g) {
        // วาดแถบส่วนหัว: ชื่อเดือนและปี พ.ศ. อยู่กลางหน้า
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

        // ลูกศรซ้าย/ขวาใช้ย้อนเดือนและเลื่อนไปเดือนถัดไป
        arrow(g, 298, 45, -1, hover == LEFT);
        arrow(g, 738, 45, 1, hover == RIGHT);
        // ปุ่ม "วันนี้" ทางขวา ช่วยกลับไปยังเดือนปัจจุบัน
        g.setColor(hover == TODAY ? new Color(0xEAF2FF) : new Color(0xF4F7FC));
        g.fill(new RoundRectangle2D.Double(TODAY.x, TODAY.y, TODAY.width, TODAY.height, 12, 12));
        g.setColor(BLUE);
        g.setFont(font(Font.PLAIN, 11));
        centeredV(g, "วันนี้", TODAY.getCenterX(), TODAY.getCenterY());
    }

    void paintGrid(Graphics2D g) {
        // วาดตาราง 7 คอลัมน์ x 6 แถว พร้อมระบายสีตามข้อมูลที่เปิดแสดง
        LocalDate today = LocalDate.now();
        LocalDate first = ym.atDay(1);
        int offset = first.getDayOfWeek().getValue() % 7;      // อาทิตย์ = 0
        LocalDate start = first.minusDays(offset);

        // หัวคอลัมน์วัน
        g.setFont(font(Font.PLAIN, 12));
        g.setColor(BLUE);
        for (int c = 0; c < 7; c++) centeredV(g, DOW[c], GX + CW * (c + .5), 92);

        // สีพื้นของแต่ละช่อง: ชมพู = มีบันทึกอาหาร, เหลือง = มีบันทึกน้ำหนัก,
        // ถ้ามีทั้งสองอย่างแบ่งครึ่งช่อง; กรอบสีน้ำเงินคือวันที่ผู้ใช้เลือก
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                LocalDate d = start.plusDays(r * 7L + c);
                boolean cal = showCal && calDays.contains(d);
                boolean yel = showWeight && weightDays.contains(d);
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
                if (d.equals(selectedDate)) {
                    g.setColor(BLUE);
                    g.setStroke(new BasicStroke(2f));
                    g.draw(new RoundRectangle2D.Double(x + 2, y + 2, CW - 4, RH - 4, 8, 8));
                }
            }
        }

        // เส้นแบ่งช่องวันในปฏิทิน
        g.setStroke(new BasicStroke(1f));
        g.setColor(GRID);
        for (int c = 0; c <= 7; c++) g.draw(new Line2D.Double(GX + c * CW, GY, GX + c * CW, GY + 6 * RH));
        for (int r = 0; r <= 6; r++) g.draw(new Line2D.Double(GX, GY + r * RH, GX + 7 * CW, GY + r * RH));

        // ตัวเลขวันที่: วันปัจจุบันเป็นวงกลมสีน้ำเงิน ส่วนวันนอกเดือนเป็นสีเทาจาง
        g.setFont(font(Font.PLAIN, 13));
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
        // คำอธิบายใต้ตาราง: วงกลมชมพูเปิด/ซ่อนบันทึกแคลอรี่, วงกลมเหลืองเปิด/ซ่อนน้ำหนัก
        legendItem(g, 464, PINK, "แคลอรี่", showCal);
        legendItem(g, 578, YELLOW, "น้ำหนัก", showWeight);
    }

    void legendItem(Graphics2D g, double cx, Color c, String label, boolean on) {
        // วาดปุ่มวงกลมและข้อความกำกับ; ถ้าปิดอยู่ทำให้สีจางลงเพื่อบอกสถานะ
        Composite old = g.getComposite();
        if (!on) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(c);
        g.fill(new Ellipse2D.Double(cx - 20, 454 - 20, 40, 40));
        g.setFont(font(Font.PLAIN, 12));
        centeredV(g, label, cx, 488);
        g.setComposite(old);
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
        // โหลดวันที่มีบันทึกจากไฟล์ของสมาชิก: น้ำหนักจากโปรไฟล์ และอาหารจาก food_logs
        Set<LocalDate> loadedWeightDays = new HashSet<>();
        Set<LocalDate> loadedCalDays = new HashSet<>();
        if (app == null || app.getCurrentUsername().trim().isEmpty()) {
            weightDays = loadedWeightDays;
            calDays = loadedCalDays;
            return;
        }
        try {
            String email = app.getCurrentUsername();
            Member member = new DailyCalorieRepository(email).loadLatest();
            if (member != null) {
                for (DailyLog log : member.getWeightHistory()) {
                    loadedWeightDays.add(LocalDate.parse(log.getData()));
                }
            }
            for (FoodLogRepository.LoggedFood log : new FoodLogRepository(email).loadAll()) {
                loadedCalDays.add(log.getDate());
            }
        } catch (java.io.IOException e) {
            System.err.println("ไม่สามารถโหลดข้อมูลปฏิทินของบัญชีได้: " + e.getMessage());
        }
        weightDays = loadedWeightDays;
        calDays = loadedCalDays;
    }

    void showDateDetails(LocalDate date) {
        // เมื่อคลิกวันในตาราง แสดงชื่ออาหาร แคลอรี่รวม และน้ำหนักของวันนั้น
        if (app == null || app.getCurrentUsername().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรุณาเข้าสู่ระบบเพื่อดูบันทึกประจำวัน",
                    "บันทึกประจำวัน", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String email = app.getCurrentUsername();
            java.util.List<FoodLogRepository.LoggedFood> loggedFoods =
                    new FoodLogRepository(email).loadByDate(date);
            Member member = new DailyCalorieRepository(email).loadLatest();
            java.util.List<FoodLog> foodLogs = new java.util.ArrayList<>();
            for (FoodLogRepository.LoggedFood loggedFood : loggedFoods) {
                foodLogs.add(loggedFood.getFoodLog());
            }
            DailyLog summary = member == null
                    ? DailyLog.summarize("", date, 0, foodLogs)
                    : member.summarizeDay(date, foodLogs);

            StringBuilder details = new StringBuilder(formatThaiDate(date)).append('\n');
            if (loggedFoods.isEmpty()) {
                details.append("ยังไม่มีบันทึกอาหาร");
            } else {
                for (FoodLogRepository.LoggedFood food : loggedFoods) {
                    details.append("\n• ")
                            .append(food.getFoodLog().getFood().getName())
                            .append(" · ")
                            .append(String.format(java.util.Locale.ROOT, "%.0f kcal",
                                    food.getFoodLog().getTotalKcal()));
                }
                details.append("\nรวม ")
                        .append(String.format(java.util.Locale.ROOT, "%.0f kcal",
                                summary.getCaloriesIn()));
            }
            details.append("\n").append(!summary.hasWeight()
                    ? "ยังไม่มีบันทึกน้ำหนัก"
                    : String.format(java.util.Locale.ROOT, "น้ำหนัก %.1f กก.",
                            summary.getWeight()));
            JOptionPane.showMessageDialog(this, details.toString(), "สรุปประจำวัน",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this, "โหลดบันทึกประจำวันไม่สำเร็จ: " + e.getMessage(),
                    "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }

    String formatThaiDate(LocalDate date) {
        return date.getDayOfMonth() + " " + MON_FULL[date.getMonthValue() - 1]
                + " " + (date.getYear() + 543);
    }
}