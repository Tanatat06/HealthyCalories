package healthycalories.app.ui.screen;
import healthycalories.app.HealthyCalories;
import healthycalories.app.ui.MainFrame;
import healthycalories.model.enumeration.FoodCategory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * หน้าค้นหาอาหาร (เลือกจากหมวดหมู่) — ออกแบบบนผืนผ้าใบฐาน 420 x 800
 * ใช้ตัวช่วยสเกลเดียวกับ HomeScreen (s(), font(), label()) เพื่อให้ขนาดตรงกันทุกหน้า
 */
public class FoodSearchScreen extends BaseScreen {

    // ---------- สี ----------
    private static final Color HEADER_TOP   = new Color(0xE2BEEC);
    private static final Color HEADER_MID   = new Color(0xE9CDF1);
    private static final Color HEADER_LOW   = new Color(0xF1DEF7);
    private static final Color TAB_ACTIVE   = new Color(0xFCEBFD);
    private static final Color BACK_PILL    = new Color(0xD9D9D9);
    private static final Color LABEL_GRAY   = new Color(0x8C8C8C);
    private static final Color TEXT_DARK    = new Color(0x111111);

    // ---------- แท็บด้านบน ----------
    public static final int TAB_SEARCH = 0;
    public static final int TAB_CATEGORY = 1;
    public static final int TAB_MY_LIST = 2;

    // ---------- หมวดหมู่ตามดีไซน์ (เรียงซ้าย→ขวา, บน→ล่าง) ----------
    private static final class Category {
        final String label;
        final String image;
        final FoodCategory type;   // null = ยังไม่มีใน enum
        Category(String label, String image, FoodCategory type) {
            this.label = label; this.image = image; this.type = type;
        }
    }

    private static final Category[] CATEGORIES = {
        new Category("โปรตีน",        "Photo/ic_protein.png",   FoodCategory.PROTEIN),
        new Category("ไขมัน",          "Photo/ic_fat.png",       FoodCategory.FAT),
        new Category("แป้ง ธัญพืช",    "Photo/ic_grain.png",     FoodCategory.GRAIN),
        new Category("ผัก",            "Photo/ic_vegetable.png", FoodCategory.VEGETTABLE),
        new Category("ผลไม้",          "Photo/ic_fruit.png",     FoodCategory.FRIUT),
        new Category("ซุป",            "Photo/ic_soup.png",      FoodCategory.SOUP),
        new Category("เครื่องดื่ม",     "Photo/ic_drink.png",     FoodCategory.DRINK),
        new Category("กับข้าว",         "Photo/ic_sidedish.png",  null),
        new Category("จานเดียว",        "Photo/ic_onedish.png",   FoodCategory.ONE_DISH),
        new Category("เบเกอรี่",        "Photo/ic_bakery.png",    FoodCategory.BAKERY),
        new Category("ขนม",            "Photo/ic_snack.png",     FoodCategory.DESSERT),
        new Category("ของหวาน",        "Photo/ic_dessert.png",   null),
        new Category("ไอศกรีม",         "Photo/ic_icecream.png",  FoodCategory.ICE_CREAM),
        new Category("ซูชิ",            "Photo/ic_sushi.png",     FoodCategory.SUSHI),
        new Category("จากผู้ใช้อื่น",    "Photo/ic_customer.png",  FoodCategory.CUSTOM),
        new Category("อื่นๆ",           "Photo/ic_other.png",     FoodCategory.OTHER),
        new Category("KFC",           "Photo/ic_kfc.png",       FoodCategory.KFC),
    };

    private static final int[] PER_ROW = {3, 3, 3, 3, 3, 2};

    // ---------- สถานะ ----------
    private String mealName = "อาหารเย็น";
    private String dateText = "วันนี้";
    private int selectedTab = TAB_CATEGORY;
    private JTextField searchField;
    private final Timer resizeTimer = new Timer(120, e -> buildUI());

    public FoodSearchScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeTimer.restart();
            }
        });
    }

    /** ตั้งชื่อมื้อ/วันที่ที่แสดงบนหัวหน้า เช่น "อาหารเช้า", "วันนี้" */
    public void setMeal(String mealName, String dateText) {
        this.mealName = mealName;
        this.dateText = dateText;
    }

    // =====================================================
    //  สร้างหน้าจอ
    // =====================================================
    @Override
    public void buildUI() {
        HomeScreen.updateSharedScale(getWidth(), getHeight());
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        final int W = HomeScreen.BASE_W;
        final int H = HomeScreen.BASE_H;
        final int ox = (W - 420) / 2;           // เนื้อหา 420px อยู่กึ่งกลางเสมอ

        JPanel canvas = new JPanel(null);
        canvas.setBackground(Color.WHITE);
        canvas.setBounds(0, 0, HomeScreen.s(W), HomeScreen.s(H));

        // ----- พื้นหัวสีม่วง -----
        Header header = new Header(W);
        header.setBounds(0, 0, HomeScreen.s(W), HomeScreen.s(170));
        canvas.add(header);

        // ----- ปุ่มย้อนกลับ (ลูกศร) / ชื่อมื้อ / วันที่ -----
        ArrowBack backArrow = new ArrowBack();
        backArrow.setBounds(HomeScreen.s(ox + 8), HomeScreen.s(8),
                HomeScreen.s(50), HomeScreen.s(34));
        backArrow.addActionListener(e -> goBack());
        canvas.add(backArrow);

        JLabel title = centered(mealName, Font.PLAIN, 16, TEXT_DARK, ox + 110, 10, 200, 32);
        canvas.add(title);
        JLabel date = centered(dateText, Font.PLAIN, 11, TEXT_DARK, ox + 330, 10, 80, 32);
        canvas.add(date);

        // ----- ช่องค้นหา -----
        SearchBox box = new SearchBox();
        box.setBounds(HomeScreen.s(ox + 22), HomeScreen.s(45), HomeScreen.s(376), HomeScreen.s(44));
        canvas.add(box);

        searchField = new JTextField();
        searchField.setFont(HomeScreen.font(Font.PLAIN, 12));
        searchField.setForeground(TEXT_DARK);
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setOpaque(false);
        searchField.setBounds(HomeScreen.s(ox + 70), HomeScreen.s(52), HomeScreen.s(310), HomeScreen.s(30));
        PlaceholderSupport.install(searchField, "ใส่ชื่อที่ต้องการค้นหา...");
        searchField.addActionListener(e -> {
            if (!searchField.getText().trim().isEmpty()) {
                searchByKeyword();
            }
        });
        canvas.add(searchField);
        canvas.setComponentZOrder(searchField, 0);

        // ----- แท็บ: ค้นหา | ค้นหาจากหมวดหมู่ | รายการของฉัน -----
        TabButton tabSearch = new TabButton("ค้นหา", null, TAB_SEARCH);
        tabSearch.setBounds(HomeScreen.s(ox + 28), HomeScreen.s(100), HomeScreen.s(120), HomeScreen.s(50));
        canvas.add(tabSearch);

        TabButton tabCategory = new TabButton("ค้นหาจากหมวดหมู่", "Photo/food.png", TAB_CATEGORY);
        tabCategory.setBounds(HomeScreen.s(ox + 161), HomeScreen.s(96), HomeScreen.s(102), HomeScreen.s(63));
        canvas.add(tabCategory);

        TabButton tabMine = new TabButton("รายการของฉัน", null, TAB_MY_LIST);
        tabMine.setBounds(HomeScreen.s(ox + 278), HomeScreen.s(100), HomeScreen.s(130), HomeScreen.s(50));
        canvas.add(tabMine);
        canvas.setComponentZOrder(tabCategory, 0);

        // ----- ปุ่ม "กลับ" -----
        PillButton back = new PillButton("กลับ");
        back.setBounds(0, HomeScreen.s(180), HomeScreen.s(102), HomeScreen.s(37));
        back.addActionListener(e -> goBack());
        canvas.add(back);

        // ----- ตารางหมวดหมู่ 3 คอลัมน์ -----
        final int[] colX = {ox + 93, ox + 210, ox + 327};
        final int row0Icon = 267, row0Label = 308, pitch = 88;
        int idx = 0;
        for (int row = 0; row < PER_ROW.length; row++) {
            int n = PER_ROW[row];
            for (int c = 0; c < n && idx < CATEGORIES.length; c++, idx++) {
                int cx = (n == 3) ? colX[c] : ox + 210 + (c == 0 ? -59 : 59);
                CategoryButton cell = new CategoryButton(CATEGORIES[idx]);
                cell.setBounds(HomeScreen.s(cx - 55), HomeScreen.s(row0Icon + row * pitch - 38),
                        HomeScreen.s(110), HomeScreen.s(row0Label - row0Icon + 38 + 18));
                canvas.add(cell);
            }
        }

        // Swing วาดตัวที่ index 0 ไว้บนสุด จึงส่งพื้นหัวสีม่วงไปไว้ล่างสุดเสมอ
        canvas.setComponentZOrder(header, canvas.getComponentCount() - 1);

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(HomeScreen.s(W), HomeScreen.s(H)));
        layered.add(canvas, JLayeredPane.DEFAULT_LAYER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(layered);
        add(center, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static JLabel centered(String text, int style, int size, Color c, int x, int y, int w, int h) {
        JLabel l = HomeScreen.label(text, style, size, c, x, y, w, h);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private void goBack() {
        if (frame == null) return;
        String prev = frame.getPreviousScreen();
        frame.showScreen(prev != null && !MainFrame.FOOD.equals(prev) ? prev : MainFrame.DIARY);
    }

    // =====================================================
    //  การทำงาน (เดิม)
    // =====================================================
    public void searchByKeyword() {
        if (frame != null && searchField != null) {
            frame.showDailog("ค้นหา: " + searchField.getText().trim());
        }
    }

    public void showCategories() {
        selectedTab = TAB_CATEGORY;
        buildUI();
    }

    public void showMyFoods() {
        selectedTab = TAB_MY_LIST;
        buildUI();
    }

    public void showRecent() {
        selectedTab = TAB_SEARCH;
        buildUI();
    }

    public void selectFood() {
    }

    private void selectCategory(Category c) {
        if (frame != null) {
            frame.showDailog("หมวดหมู่: " + c.label);
        }
    }

    // =====================================================
    //  คอมโพเนนต์ย่อย
    // =====================================================

    /** พื้นหลังหัวม่วงแบบซ้อนชั้น (ตามดีไซน์) */
    private static final class Header extends JComponent {
        private final int baseW;
        Header(int baseW) { this.baseW = baseW; setOpaque(false); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            g.scale(HomeScreen.S, HomeScreen.S);
            double w = baseW;
            int ox = (int) ((w - 420) / 2);

            // ชั้นล่างสุด: โค้งใต้แท็บ (เริ่มประมาณ x=70 ลงไปถึง y=165)
            Path2D low = new Path2D.Double();
            low.moveTo(w, 0);
            low.lineTo(w, 165);
            low.lineTo(ox + 100, 165);
            low.curveTo(ox + 62, 165, ox + 40, 150, ox + 30, 125);
            low.lineTo(ox + 30, 0);
            low.closePath();
            g.setColor(HEADER_LOW);
            g.fill(low);

            // ชั้นกลาง
            Path2D mid = new Path2D.Double();
            mid.moveTo(w, 0);
            mid.lineTo(w, 125);
            mid.lineTo(ox + 62, 125);
            mid.curveTo(ox + 36, 125, ox + 22, 110, ox + 14, 92);
            mid.lineTo(0, 92);
            mid.lineTo(0, 0);
            mid.closePath();
            g.setColor(HEADER_MID);
            g.fill(mid);

            // ชั้นบนสุด (โค้งขวาล่าง)
            Path2D top = new Path2D.Double();
            top.moveTo(0, 0);
            top.lineTo(w, 0);
            top.lineTo(w, 82);
            top.curveTo(w, 100, w - 14, 106, w - 40, 104);
            top.lineTo(0, 104);
            top.closePath();
            g.setColor(HEADER_TOP);
            g.fill(top);
            g.dispose();
        }
    }

    /** ลูกศรย้อนกลับ ← */
    private static final class ArrowBack extends JButton {
        ArrowBack() {
            HomeScreen.flat(this);
            setBorder(BorderFactory.createEmptyBorder());
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            g.scale(HomeScreen.S, HomeScreen.S);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(8, 17, 40, 17);
            g.drawLine(8, 17, 17, 9);
            g.drawLine(8, 17, 17, 25);
            g.dispose();
        }
    }

    /** กล่องค้นหาสีขาวมน + แว่นขยาย */
    private static final class SearchBox extends JComponent {
        SearchBox() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            g.scale(HomeScreen.S, HomeScreen.S);
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, 376, 44, 44, 44));
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Double(23, 12, 13, 13));
            g.drawLine(34, 24, 40, 30);
            g.dispose();
        }
    }

    /** แท็บหัวหน้า (มีไอคอนได้) */
    private final class TabButton extends JButton {
        private final String text;
        private final BufferedImage icon;
        private final int tabId;

        TabButton(String text, String iconPath, int tabId) {
            this.text = text;
            this.tabId = tabId;
            this.icon = iconPath == null ? null : HomeScreen.loadFit(iconPath,
                    HomeScreen.s(30), HomeScreen.s(30));
            HomeScreen.flat(this);
            setBorder(BorderFactory.createEmptyBorder());
            addActionListener(e -> {
                if (tabId == TAB_CATEGORY) showCategories();
                else if (tabId == TAB_MY_LIST) showMyFoods();
                else showRecent();
            });
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            boolean active = tabId == selectedTab;
            int w = getWidth(), h = getHeight();
            if (active) {
                g.setColor(TAB_ACTIVE);
                g.fill(new RoundRectangle2D.Double(0, 0, w, h, HomeScreen.s(16), HomeScreen.s(16)));
            }
            g.setColor(Color.BLACK);
            if (icon != null) {
                g.drawImage(icon, (w - icon.getWidth()) / 2, HomeScreen.s(6), null);
                g.setFont(HomeScreen.font(Font.PLAIN, 8));
                HomeScreen.drawCentered(g, text, w / 2.0, h - HomeScreen.s(8));
            } else {
                g.setFont(HomeScreen.font(Font.PLAIN, 17));
                FontMetrics fm = g.getFontMetrics();
                HomeScreen.drawCentered(g, text, w / 2.0, (h + fm.getAscent() - fm.getDescent()) / 2.0 + HomeScreen.s(6));
            }
            g.dispose();
        }
    }

    /** ปุ่ม "กลับ" แบบแคปซูลสีเทา */
    private static final class PillButton extends JButton {
        private final String text;
        PillButton(String text) {
            this.text = text;
            HomeScreen.flat(this);
            setBorder(BorderFactory.createEmptyBorder());
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            int w = getWidth(), h = getHeight();
            // ขอบซ้ายชิดจอ: โค้งเฉพาะด้านขวา
            Area shape = new Area(new RoundRectangle2D.Double(0, 0, w, h, h, h));
            shape.add(new Area(new Rectangle2D.Double(0, 0, h / 2.0, h)));
            g.setColor(getModel().isRollover() ? BACK_PILL.darker() : BACK_PILL);
            g.fill(shape);
            g.setColor(new Color(0x7A7A7A));
            g.setFont(HomeScreen.font(Font.PLAIN, 15));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, HomeScreen.s(25), (h + fm.getAscent() - fm.getDescent()) / 2f);
            g.dispose();
        }
    }

    /** ปุ่มหมวดหมู่: ไอคอนบน + ชื่อด้านล่าง */
    private final class CategoryButton extends JButton {
        private final Category cat;
        private final BufferedImage icon;
        private boolean hover;

        CategoryButton(Category cat) {
            this.cat = cat;
            this.icon = HomeScreen.loadFit(cat.image, HomeScreen.s(60), HomeScreen.s(52));
            HomeScreen.flat(this);
            setBorder(BorderFactory.createEmptyBorder());
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
            addActionListener(e -> selectCategory(cat));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            HomeScreen.aa(g);
            int w = getWidth();
            if (hover) {
                g.setColor(new Color(0xF8EEFB));
                g.fill(new RoundRectangle2D.Double(0, 0, w, getHeight(), HomeScreen.s(14), HomeScreen.s(14)));
            }
            // ไอคอน: จัดกึ่งกลางในกรอบสูง 52 จุดฐาน เริ่มที่ y=12
            int boxTop = HomeScreen.s(12), boxH = HomeScreen.s(52);
            g.drawImage(icon, (w - icon.getWidth()) / 2, boxTop + (boxH - icon.getHeight()) / 2, null);
            g.setColor(LABEL_GRAY);
            g.setFont(HomeScreen.font(Font.PLAIN, 12));
            HomeScreen.drawCentered(g, cat.label, w / 2.0, HomeScreen.s(88));
            g.dispose();
        }
    }

    /** placeholder สำหรับ JTextField */
    private static final class PlaceholderSupport {
        static void install(JTextField field, String hint) {
            field.putClientProperty("hint", hint);
            field.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
                @Override protected void paintSafely(Graphics g) {
                    super.paintSafely(g);
                    if (field.getText().isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        g2.setColor(new Color(0x222222));
                        g2.setFont(field.getFont());
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(hint, field.getInsets().left,
                                (field.getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                        g2.dispose();
                    }
                }
            });
            field.setCaretColor(Color.BLACK);
        }
    }

    // เพื่อให้เรียกใช้ซ้ำได้จากหน้าอื่น
    public String getMealName() { return mealName; }
    

}