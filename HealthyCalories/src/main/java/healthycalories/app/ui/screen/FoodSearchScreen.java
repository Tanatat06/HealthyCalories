package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.app.ui.BufferedImageIcon;

public class FoodSearchScreen extends BaseScreen {
    private static final int DESIGN_WIDTH = 400;
    private static final int DESIGN_HEIGHT = 800;

    public static final String SCREEN_MINE = "SCREEN_MINE";         // หน้า FoodsearchScreen1 (รายการของฉัน)
    public static final String SCREEN_SEARCH = "SCREEN_SEARCH";     // หน้า FoodsearchScreen2 (ค้นหาล่าสุด)
    public static final String SCREEN_CATEGORY = "SCREEN_CATEGORY"; // หน้า FoodSearchScreen (หมวดหมู่)

    // ตัวจัดการสลับหน้าจอ (CardLayout)
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private String activeScreen = SCREEN_MINE;
    private double currentScaleX = -1;
    private double currentScaleY = -1;
    private List<String> foodNames;
    private String requestedStartScreen = SCREEN_MINE;
    private final Timer resizeTimer = new Timer(120, e -> rebuildIfScaleChanged());

    // บันทึกหน้าที่เปิดก่อนหน้า เพื่อให้ปุ่ม "กลับ" ย้อนถูกหน้า
    private String previousScreen = SCREEN_MINE;

    public FoodSearchScreen(HealthyCalories app, MainFrame frame) {
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
        String screenToShow = requestedStartScreen;
        requestedStartScreen = SCREEN_MINE;
        rebuildUI(screenToShow);
    }

    public void openCategoryFromHome() {
        requestedStartScreen = SCREEN_CATEGORY;
        frame.showScreen(MainFrame.FOOD);
    }

    private void rebuildUI(String screenToShow) {
        HomeScreen.updateSharedScale(getWidth(), getHeight());
        double scaleY = HomeScreen.S;
        int logicalWidth = getWidth() > 0
                ? Math.max(DESIGN_WIDTH, HomeScreen.BASE_W) : DESIGN_WIDTH;
        double scaleX = (double) logicalWidth / DESIGN_WIDTH * scaleY;
        currentScaleX = scaleX;
        currentScaleY = scaleY;
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        // นำทั้ง 3 หน้าจอมารวมไว้ใน Container เดียวกัน
        addScreen(new ScreenMine(this), SCREEN_MINE, scaleX, scaleY);
        addScreen(new ScreenSearch(this), SCREEN_SEARCH, scaleX, scaleY);
        addScreen(new ScreenCategory(this), SCREEN_CATEGORY, scaleX, scaleY);

        mainContainer.setPreferredSize(new Dimension(
                scaled(logicalWidth, scaleY), scaled(DESIGN_HEIGHT, scaleY)));
        JPanel centeredContent = new JPanel(new GridBagLayout());
        centeredContent.setBackground(Color.WHITE);
        centeredContent.add(mainContainer);
        add(centeredContent, BorderLayout.CENTER);

        showScreen(screenToShow);
        revalidate();
        repaint();
    }

    private void rebuildIfScaleChanged() {
        HomeScreen.updateSharedScale(getWidth(), getHeight());
        double scaleY = HomeScreen.S;
        int logicalWidth = getWidth() > 0
                ? Math.max(DESIGN_WIDTH, HomeScreen.BASE_W) : DESIGN_WIDTH;
        double scaleX = (double) logicalWidth / DESIGN_WIDTH * scaleY;
        if (Math.abs(scaleX - currentScaleX) > 0.01
                || Math.abs(scaleY - currentScaleY) > 0.01) {
            rebuildUI(activeScreen);
        }
    }

    private void addScreen(JPanel screen, String name, double scaleX, double scaleY) {
        screen.setPreferredSize(new Dimension(
                scaled(DESIGN_WIDTH, scaleX), scaled(DESIGN_HEIGHT, scaleY)));
        scaleComponentTree(screen, scaleX, scaleY);
        mainContainer.add(screen, name);
    }

    private static int scaled(int value, double scale) {
        return (int) Math.round(value * scale);
    }

    private void showFoodMatches(JTextField searchField, JPopupMenu resultsPopup, String keyword) {
        resultsPopup.removeAll();
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isEmpty()) {
            resultsPopup.setVisible(false);
            return;
        }

        try {
            if (foodNames == null) {
                foodNames = loadFoodNames();
            }
        } catch (IOException e) {
            resultsPopup.setVisible(false);
            JOptionPane.showMessageDialog(this,
                    "ไม่สามารถอ่านรายชื่ออาหารจาก foods.csv ได้:\n" + e.getMessage(),
                    "ค้นหาอาหารไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int matchCount = 0;
        for (String name : foodNames) {
            if (!name.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                continue;
            }
            matchCount++;
            if (matchCount <= 10) {
                JMenuItem result = new JMenuItem(name);
                result.setFont(new Font("Tahoma", Font.PLAIN,
                        Math.max(16, scaled(18, currentScaleY))));
                result.setForeground(Color.BLACK);
                result.setFocusable(false);
                result.setRequestFocusEnabled(false);
                result.setPreferredSize(new Dimension(
                        Math.max(searchField.getWidth(), scaled(250, currentScaleY)),
                        Math.max(34, scaled(40, currentScaleY))));
                result.addActionListener(e -> {
                    searchField.setText(name);
                    resultsPopup.setVisible(false);
                });
                resultsPopup.add(result);
            }
        }

        if (matchCount == 0) {
            JMenuItem noResults = new JMenuItem("ไม่พบชื่ออาหารที่ตรงกัน");
            styleResultMessage(noResults, searchField);
            noResults.setFocusable(false);
            noResults.setRequestFocusEnabled(false);
            noResults.setEnabled(false);
            resultsPopup.add(noResults);
        } else if (matchCount > 10) {
            JMenuItem remaining = new JMenuItem("และอีก " + (matchCount - 10) + " รายการ");
            styleResultMessage(remaining, searchField);
            remaining.setFocusable(false);
            remaining.setRequestFocusEnabled(false);
            remaining.setEnabled(false);
            resultsPopup.add(remaining);
        }

        int popupWidth = Math.max(searchField.getWidth(), scaled(300, currentScaleY));
        int popupHeight = Math.min(resultsPopup.getComponentCount(),
                11) * Math.max(34, scaled(40, currentScaleY));
        resultsPopup.setFont(new Font("Tahoma", Font.PLAIN,
                Math.max(16, scaled(18, currentScaleY))));
        resultsPopup.setPopupSize(popupWidth, popupHeight);
        if (!resultsPopup.isVisible()) {
            resultsPopup.show(searchField, 0, searchField.getHeight());
        }
    }

    private void styleResultMessage(JMenuItem item, JTextField searchField) {
        item.setFont(new Font("Tahoma", Font.PLAIN,
                Math.max(16, scaled(18, currentScaleY))));
        item.setPreferredSize(new Dimension(
                Math.max(searchField.getWidth(), scaled(300, currentScaleY)),
                Math.max(34, scaled(40, currentScaleY))));
    }

    private static List<String> loadFoodNames() throws IOException {
        String resource = "/csv/foods.csv";
        URL url = FoodSearchScreen.class.getResource(resource);
        if (url == null) {
            url = FoodSearchScreen.class.getResource(
                    "/healthycalories/app/ui/screen/csv/foods.csv");
        }
        if (url != null) {
            try (BufferedReader reader = new BufferedReader(
                    new java.io.InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                return extractFoodNames(reader);
            }
        }

        Path[] candidates = {
            Paths.get("ProjectFolders", "csv", "foods.csv"),
            Paths.get("csv", "foods.csv"),
            Paths.get("src", "main", "resources", "csv", "foods.csv")
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                try (BufferedReader reader = Files.newBufferedReader(candidate, StandardCharsets.UTF_8)) {
                    return extractFoodNames(reader);
                }
            }
        }
        throw new IOException("ไม่พบไฟล์ foods.csv");
    }

    private static List<String> extractFoodNames(BufferedReader reader) throws IOException {
        List<String> names = new ArrayList<>();
        String line;
        boolean firstRow = true;
        while ((line = reader.readLine()) != null) {
            if (firstRow) {
                line = line.replace("\uFEFF", "");
                firstRow = false;
                if (line.toUpperCase(Locale.ROOT).startsWith("NUM_ID,")) {
                    continue;
                }
            }
            String[] columns = line.split(",", 3);
            if (columns.length >= 2 && !columns[1].trim().isEmpty()) {
                names.add(columns[1].trim());
            }
        }
        return names;
    }

    private static void scaleComponentTree(Component component, double scaleX, double scaleY) {
        Rectangle bounds = component.getBounds();
        component.setBounds(scaled(bounds.x, scaleX), scaled(bounds.y, scaleY),
                scaled(bounds.width, scaleX), scaled(bounds.height, scaleY));

        if (component instanceof JComponent) {
            JComponent swingComponent = (JComponent) component;
            swingComponent.putClientProperty("foodSearchScaleX", scaleX);
            swingComponent.putClientProperty("foodSearchScaleY", scaleY);
            Font font = swingComponent.getFont();
            if (font != null) {
                swingComponent.setFont(font.deriveFont(
                        Math.max(8f, (float) (font.getSize2D() * scaleY))));
            }
            Icon icon = null;
            if (swingComponent instanceof AbstractButton) {
                icon = ((AbstractButton) swingComponent).getIcon();
            } else if (swingComponent instanceof JLabel) {
                icon = ((JLabel) swingComponent).getIcon();
            }
            if (icon instanceof BufferedImageIcon) {
                BufferedImageIcon imageIcon = (BufferedImageIcon) icon;
                BufferedImageIcon scaledIcon = new BufferedImageIcon(imageIcon.getImage(),
                        scaled(icon.getIconWidth(), scaleY),
                        scaled(icon.getIconHeight(), scaleY));
                if (swingComponent instanceof AbstractButton) {
                    ((AbstractButton) swingComponent).setIcon(scaledIcon);
                } else if (swingComponent instanceof JLabel) {
                    ((JLabel) swingComponent).setIcon(scaledIcon);
                }
            }
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                scaleComponentTree(child, scaleX, scaleY);
            }
        }
    }

    private static Graphics2D createLogicalGraphics(Graphics graphics, JComponent component) {
        Graphics2D logicalGraphics = (Graphics2D) graphics.create();
        logicalGraphics.scale(getComponentScaleX(component), getComponentScaleY(component));
        return logicalGraphics;
    }

    private static double getComponentScaleX(JComponent component) {
        Object scale = component.getClientProperty("foodSearchScaleX");
        return scale instanceof Number ? ((Number) scale).doubleValue() : 1.0;
    }

    private static double getComponentScaleY(JComponent component) {
        Object scale = component.getClientProperty("foodSearchScaleY");
        return scale instanceof Number ? ((Number) scale).doubleValue() : 1.0;
    }

    public void showScreen(String name) {
        if (!name.equals(SCREEN_CATEGORY)) {
            previousScreen = name;
        }
        activeScreen = name;
        cardLayout.show(mainContainer, name);
    }

    public void goBack() {
        showScreen(previousScreen);
    }

    private void goBackToAppScreen() {
        String destination = frame.getPreviousScreen();
        if (destination == null || MainFrame.FOOD.equals(destination)) {
            destination = MainFrame.HOME;
        }
        frame.showScreen(destination);
    }

    // ฟังก์ชันโหลดรูปภาพจาก ProjectFolders/Photo
    // หากยังไม่มีไฟล์รูป จะสร้าง Placeholder รองรับไว้เพื่อไม่ให้โปรแกรม Error
    public static BufferedImageIcon loadIcon(String path, int w, int h) {
        String fileName = new File(path).getName();
        BufferedImage image = null;
        try {
            File photo = new File("ProjectFolders" + File.separator + "Photo", fileName);
            if (photo.isFile()) {
                image = ImageIO.read(photo);
            }
        } catch (IOException e) {
            throw new IllegalStateException("อ่านรูปภาพไม่สำเร็จ: " + path, e);
        }

        if (image != null) {
            return new BufferedImageIcon(image, w, h);
        }

        // กรณีไม่พบรูป: วาดกรอบสีพาสเทลสำรองไว้ให้อัตโนมัติ
        BufferedImage fallback = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fallback.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(230, 210, 240));
        g2.fillRoundRect(1, 1, w - 2, h - 2, 10, 10);
        g2.dispose();
        return new BufferedImageIcon(fallback);
    }

    // =========================================================================
    // หน้าที่ 1: รายการของฉัน (FoodsearchScreen1)
    // =========================================================================
    static class ScreenMine extends JPanel {
        ScreenMine(FoodSearchScreen app) {
            setLayout(null);
            setBackground(Color.WHITE);

            add(createHeader(app, SCREEN_MINE, app::goBackToAppScreen));

            JLabel boxImage = new JLabel("", SwingConstants.CENTER);
            boxImage.setBounds(100, 330, 200, 150);
            boxImage.setIcon(loadIcon("box.png", 190, 140));
            add(boxImage);

            JLabel text1 = new JLabel("ยังไม่มีรายการของฉัน", SwingConstants.CENTER);
            text1.setFont(new Font("Tahoma", Font.PLAIN, 20));
            text1.setBounds(50, 480, 300, 32);
            add(text1);

            JLabel text2 = new JLabel("(โปรดเพิ่มรายการของฉัน)", SwingConstants.CENTER);
            text2.setFont(new Font("Tahoma", Font.PLAIN, 14));
            text2.setForeground(Color.GRAY);
            text2.setBounds(50, 512, 300, 25);
            add(text2);
        }
    }

    // =========================================================================
    // หน้าที่ 2: ประวัติการค้นหาล่าสุด (FoodsearchScreen2)
    // =========================================================================
    static class ScreenSearch extends JPanel {
        ScreenSearch(FoodSearchScreen app) {
            setLayout(null);
            setBackground(Color.WHITE);

            add(createHeader(app, SCREEN_SEARCH, app::goBackToAppScreen));

            JLabel emptyText = new JLabel("ยังไม่มีรายการล่าสุดในขณะนี้", SwingConstants.CENTER);
            emptyText.setFont(new Font("Tahoma", Font.PLAIN, 20));
            emptyText.setBounds(30, 510, 340, 35);
            add(emptyText);

            JLabel boxImage = new JLabel("", SwingConstants.CENTER);
            boxImage.setBounds(100, 390, 200, 150);
            boxImage.setIcon(loadIcon("box.png", 190, 140));
            add(boxImage);
        }
    }

    private static JButton createCategoryNavButton(FoodSearchScreen app) {
        return createCategoryNavButton(app, false);
    }

    private static JButton createCategoryNavButton(FoodSearchScreen app, boolean active) {
        JButton categoryBtn = new JButton("ค้นหาจากหมวดหมู่", loadIcon("food.png", 35, 28)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                double scaleY = getComponentScaleY(this);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(new Color(0xFCEBFD));
                } else if (getModel().isPressed()) {
                    g2.setColor(new Color(235, 200, 238));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(246, 220, 246));
                } else {
                    g2.setColor(new Color(252, 236, 252));
                }
                int arc = scaled(25, scaleY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        categoryBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
        categoryBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
        categoryBtn.setHorizontalTextPosition(SwingConstants.CENTER);
        categoryBtn.setIconTextGap(2);
        categoryBtn.setMargin(new Insets(0, 0, 0, 0));
        categoryBtn.setContentAreaFilled(false);
        categoryBtn.setBorderPainted(false);
        categoryBtn.setFocusPainted(false);
        categoryBtn.setOpaque(false);
        categoryBtn.setRolloverEnabled(true);
        categoryBtn.setFocusable(false);
        categoryBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        categoryBtn.addActionListener(e -> app.showScreen(SCREEN_CATEGORY));
        return categoryBtn;
    }

    private static JPanel createHeader(FoodSearchScreen app, String activeTab, Runnable backAction) {
        JPanel header = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = createLogicalGraphics(graphics, this);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(214, 170, 230));
                g.fillRoundRect(60, 100, 340, 90, 80, 80);
                g.setColor(new Color(224, 190, 238));
                g.fillRoundRect(-40, -60, 480, 200, 80, 80);
                g.fillRoundRect(20, 80, 380, 90, 80, 80);
                g.dispose();
            }
        };
        header.setBounds(0, 0, DESIGN_WIDTH, 180);
        header.setOpaque(false);

        JButton back = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                double scale = getComponentScaleY(this);
                g.scale(scale, scale);
                g.drawLine(8, 17, 35, 17);
                g.drawLine(8, 17, 17, 9);
                g.drawLine(8, 17, 17, 25);
                g.dispose();
            }
        };
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setFocusable(false);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setBounds(15, 5, 50, 45);
        back.addActionListener(e -> backAction.run());
        header.add(back);

        JLabel title = new JLabel("อาหารเย็น", SwingConstants.CENTER);
        title.setFont(new Font("Tahoma", Font.PLAIN, 20));
        title.setBounds(100, 10, 200, 35);
        header.add(title);

        JLabel date = new JLabel("วันนี้", SwingConstants.RIGHT);
        date.setFont(new Font("Tahoma", Font.PLAIN, 14));
        date.setBounds(300, 15, 80, 25);
        header.add(date);

        JPanel searchBox = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = scaled(40, getComponentScaleY(this));
                g.setColor(Color.WHITE);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g.setColor(new Color(200, 170, 215));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1.8f));
                double scale = getComponentScaleY(this);
                g.scale(scale, scale);
                g.draw(new Ellipse2D.Double(20, 13, 13, 13));
                g.drawLine(31, 25, 37, 31);
                g.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setBounds(25, 55, 350, 48);
        header.add(searchBox);

        String hint = "ใส่ชื่อที่ต้องการค้นหา...";
        JTextField searchField = new JTextField(hint);
        searchField.setFont(new Font("Tahoma", Font.PLAIN, 14));
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setBounds(50, 8, 285, 32);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals(hint)) {
                    searchField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText(hint);
                }
            }
        });
        searchBox.add(searchField);

        JPopupMenu resultsPopup = new JPopupMenu();
        resultsPopup.setFocusable(false);
        resultsPopup.setRequestFocusEnabled(false);
        Timer resultsUpdateTimer = new Timer(180, e -> {
            String keyword = searchField.getText();
            if (!hint.equals(keyword)) {
                app.showFoodMatches(searchField, resultsPopup, keyword);
            } else {
                resultsPopup.setVisible(false);
            }
        });
        resultsUpdateTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateResults() {
                // Debounce document updates so typing and Thai IME composition are not interrupted.
                resultsUpdateTimer.restart();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateResults();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateResults();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateResults();
            }
        });
        searchField.addActionListener(e -> {
            if (resultsPopup.isVisible() && resultsPopup.getComponentCount() > 0) {
                Component firstResult = resultsPopup.getComponent(0);
                if (firstResult instanceof JMenuItem && firstResult.isEnabled()) {
                    ((JMenuItem) firstResult).doClick();
                }
            }
        });

        Map<TextAttribute, Object> underline = new HashMap<>();
        underline.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        boolean searchActive = SCREEN_SEARCH.equals(activeTab);
        JButton tabSearch = new JButton("ค้นหา");
        Font searchFont = new Font("Tahoma", Font.PLAIN, 18);
        tabSearch.setFont(searchActive ? searchFont.deriveFont(underline) : searchFont);
        styleHeaderTab(tabSearch, searchActive);
        tabSearch.setBounds(20, 105, 110, 55);
        tabSearch.addActionListener(e -> app.showScreen(SCREEN_SEARCH));
        header.add(tabSearch);

        boolean categoryActive = SCREEN_CATEGORY.equals(activeTab);
        JButton tabCategory = createCategoryNavButton(app, categoryActive);
        tabCategory.setBounds(145, 105, 110, 80);
        header.add(tabCategory);

        boolean mineActive = SCREEN_MINE.equals(activeTab);
        JButton tabMine = new JButton("รายการของฉัน");
        Font mineFont = new Font("Tahoma", Font.PLAIN, 18);
        tabMine.setFont(mineActive ? mineFont.deriveFont(underline) : mineFont);
        styleHeaderTab(tabMine, mineActive);
        tabMine.setBounds(260, 105, 130, 55);
        tabMine.addActionListener(e -> app.showScreen(SCREEN_MINE));
        header.add(tabMine);
        return header;
    }

    private static void styleHeaderTab(AbstractButton tab, boolean active) {
        tab.setForeground(Color.BLACK);
        tab.setHorizontalAlignment(SwingConstants.CENTER);
        tab.setVerticalAlignment(SwingConstants.CENTER);
        tab.setContentAreaFilled(false);
        tab.setBorderPainted(false);
        tab.setFocusPainted(false);
        tab.setOpaque(false);
        tab.setFocusable(false);
        tab.setMargin(new Insets(0, 4, 0, 4));
        if (!active) {
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    // =========================================================================
    // หน้าที่ 3: เลือกตามหมวดหมู่ (FoodSearchScreen เดิม)
    // =========================================================================
    static class ScreenCategory extends JPanel {
        private static final Color HEADER_TOP = new Color(0xE2BEEC);
        private static final Color HEADER_MID = new Color(0xE9CDF1);
        private static final Color HEADER_LOW = new Color(0xF1DEF7);
        private static final Color TAB_ACTIVE = new Color(0xFCEBFD);
        private static final Color BACK_PILL  = new Color(0xD9D9D9);
        private static final Color LABEL_GRAY = new Color(0x8C8C8C);

        static class Category {
            final String label;
            final String image;
            Category(String label, String image) {
                this.label = label;
                this.image = image;
            }
        }

        // คงชื่อไฟล์รูปภาพต้นฉบับทั้ง 17 รายการ
        private static final Category[] CATEGORIES = {
            new Category("โปรตีน",        "ic_protein.png"),
            new Category("ไขมัน",          "ic_fat.png"),
            new Category("แป้ง ธัญพืช",    "ic_grain.png"),
            new Category("ผัก",            "ic_vegetable.png"),
            new Category("ผลไม้",          "ic_fruit.png"),
            new Category("ซุป",            "ic_soup.png"),
            new Category("เครื่องดื่ม",     "ic_drink.png"),
            new Category("กับข้าว",         "ic_sidedish.png"),
            new Category("จานเดียว",        "ic_onedish.png"),
            new Category("เบเกอรี่",        "ic_bakery.png"),
            new Category("ขนม",            "ic_snack.png"),
            new Category("ของหวาน",        "ic_dessert.png"),
            new Category("ไอศกรีม",         "ic_icecream.png"),
            new Category("ซูชิ",            "ic_sushi.png"),
            new Category("จากผู้ใช้อื่น",    "ic_customer.png"),
            new Category("อื่นๆ",           "ic_other.png"),
            new Category("KFC",           "ic_kfc.png")
        };

        private static final int[] PER_ROW = {3, 3, 3, 3, 3, 2};

        ScreenCategory(FoodSearchScreen app) {
            setLayout(null);
            setBackground(Color.WHITE);

            JPanel header = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = createLogicalGraphics(g, this);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(214, 170, 230));
                    g2.fillRoundRect(60, 100, 340, 90, 80, 80);
                    g2.setColor(new Color(224, 190, 238));
                    g2.fillRoundRect(-40, -60, 480, 200, 80, 80);
                    g2.fillRoundRect(20, 80, 380, 90, 80, 80);
                    g2.dispose();
                }
            };
            header.setBounds(0, 0, 400, 180);
            header.setOpaque(false);
            add(header);

            // ปุ่มย้อนกลับด้านบน
            JButton backArrow = new JButton() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    double scale = getComponentScaleY(this);
                    g.scale(scale, scale);
                    g.drawLine(8, 17, 35, 17);
                    g.drawLine(8, 17, 17, 9);
                    g.drawLine(8, 17, 17, 25);
                    g.dispose();
                }
            };
            backArrow.setContentAreaFilled(false);
            backArrow.setBorderPainted(false);
            backArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backArrow.setBounds(15, 5, 50, 45);
            backArrow.addActionListener(e -> app.goBack());
            header.add(backArrow);

            JLabel title = new JLabel("อาหารเย็น", SwingConstants.CENTER);
            title.setFont(new Font("Tahoma", Font.PLAIN, 20));
            title.setBounds(100, 10, 200, 35);
            header.add(title);

            JLabel date = new JLabel("วันนี้", SwingConstants.RIGHT);
            date.setFont(new Font("Tahoma", Font.PLAIN, 14));
            date.setBounds(300, 15, 80, 25);
            header.add(date);

            // ช่องค้นหา
            JComponent searchBox = new JComponent() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Color.WHITE);
                    double scaleY = getComponentScaleY(this);
                    g.fill(new RoundRectangle2D.Double(0, 0,
                            getWidth(), getHeight(), scaled(40, scaleY), scaled(40, scaleY)));
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(1.8f));
                    g.scale(scaleY, scaleY);
                    g.draw(new Ellipse2D.Double(20, 13, 13, 13));
                    g.drawLine(31, 25, 37, 31);
                    g.dispose();
                }
            };
            searchBox.setBounds(25, 55, 350, 48);
            header.add(searchBox);

            final String hint = "ใส่ชื่อที่ต้องการค้นหา...";
            JTextField searchField = new JTextField(hint);
            searchField.setFont(new Font("Tahoma", Font.PLAIN, 14));
            searchField.setBorder(null);
            searchField.setOpaque(false);
            searchField.setBounds(50, 8, 285, 32);
            searchField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (searchField.getText().equals(hint)) searchField.setText("");
                }
                public void focusLost(FocusEvent e) {
                    if (searchField.getText().isEmpty()) searchField.setText(hint);
                }
            });
            searchBox.add(searchField);

            // แท็บ "ค้นหา"
            JButton tabSearch = new JButton("ค้นหา");
            tabSearch.setFont(new Font("Tahoma", Font.PLAIN, 18));
            tabSearch.setForeground(Color.BLACK);
            tabSearch.setContentAreaFilled(false);
            tabSearch.setBorderPainted(false);
            tabSearch.setFocusPainted(false);
            tabSearch.setOpaque(false);
            tabSearch.setFocusable(false);
            tabSearch.setBounds(20, 105, 110, 55);
            tabSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabSearch.addActionListener(e -> app.showScreen(SCREEN_SEARCH));
            header.add(tabSearch);

            // แท็บ "ค้นหาจากหมวดหมู่" (ใช้รูป food.png จาก ProjectFolders/Photo)
            JButton tabCat = new JButton("ค้นหาจากหมวดหมู่", loadIcon("food.png", 30, 30)) {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(TAB_ACTIVE);
                    double scaleY = getComponentScaleY(this);
                    int arc = scaled(16, scaleY);
                    g.fill(new RoundRectangle2D.Double(0, 0,
                            getWidth(), getHeight(), arc, arc));
                    g.dispose();
                    super.paintComponent(g0);
                }
            };
            tabCat.setFont(new Font("Tahoma", Font.PLAIN, 11));
            tabCat.setVerticalTextPosition(SwingConstants.BOTTOM);
            tabCat.setHorizontalTextPosition(SwingConstants.CENTER);
            tabCat.setContentAreaFilled(false);
            tabCat.setBorderPainted(false);
            tabCat.setFocusPainted(false);
            tabCat.setOpaque(false);
            tabCat.setFocusable(false);
            tabCat.setMargin(new Insets(0, 0, 0, 0));
            tabCat.setIconTextGap(4);
            tabCat.setBounds(145, 105, 110, 80);
            header.add(tabCat);

            // แท็บ "รายการของฉัน"
            JButton tabMine = new JButton("รายการของฉัน");
            tabMine.setFont(new Font("Tahoma", Font.PLAIN, 18));
            tabMine.setForeground(Color.BLACK);
            tabMine.setContentAreaFilled(false);
            tabMine.setBorderPainted(false);
            tabMine.setFocusPainted(false);
            tabMine.setOpaque(false);
            tabMine.setFocusable(false);
            tabMine.setBounds(260, 105, 130, 55);
            tabMine.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tabMine.addActionListener(e -> app.showScreen(SCREEN_MINE));
            header.add(tabMine);

            remove(header);
            header = createHeader(app, SCREEN_CATEGORY, app::goBack);
            add(header);

            // ปุ่มกลับแบบ Pill ด้านซ้าย
            JButton backPill = new JButton("กลับ") {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    Area shape = new Area(new RoundRectangle2D.Double(0, 0, w, h, h, h));
                    shape.add(new Area(new Rectangle2D.Double(0, 0, h / 2.0, h)));
                    g.setColor(BACK_PILL);
                    g.fill(shape);
                    g.dispose();
                    super.paintComponent(g0);
                }
            };
            backPill.setContentAreaFilled(false);
            backPill.setBorderPainted(false);
            backPill.setForeground(new Color(0x7A7A7A));
            backPill.setFont(new Font("Tahoma", Font.PLAIN, 15));
            backPill.setHorizontalAlignment(SwingConstants.LEFT);
            backPill.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
            backPill.setBounds(0, 190, 95, 36);
            backPill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backPill.addActionListener(e -> app.goBack());
            add(backPill);

            // จัดวางปุ่มหมวดหมู่อาหารทั้ง 17 รายการ
            final int[] colX = {75, 200, 325};
            final int row0Y = 325, pitch = 86;
            int idx = 0;
            for (int row = 0; row < PER_ROW.length; row++) {
                int n = PER_ROW[row];
                for (int c = 0; c < n && idx < CATEGORIES.length; c++, idx++) {
                    int cx = (n == 3) ? colX[c] : 200 + (c == 0 ? -60 : 60);
                    Category cat = CATEGORIES[idx];

                    JButton btn = new JButton(cat.label, loadIcon(cat.image, 45, 40)) {
                        @Override protected void paintComponent(Graphics g0) {
                            if (getModel().isRollover()) {
                                Graphics2D g = (Graphics2D) g0.create();
                                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g.setColor(new Color(0xF8EEFB));
                                double scaleY = getComponentScaleY(this);
                                int arc = scaled(14, scaleY);
                                g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                                g.dispose();
                            }
                            super.paintComponent(g0);
                        }
                    };
                    btn.setFont(new Font("Tahoma", Font.PLAIN, 11));
                    btn.setForeground(LABEL_GRAY);
                    btn.setVerticalTextPosition(SwingConstants.BOTTOM);
                    btn.setHorizontalTextPosition(SwingConstants.CENTER);
                    btn.setContentAreaFilled(false);
                    btn.setBorderPainted(false);
                    btn.setFocusPainted(false);
                    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    btn.setBounds(cx - 50, row0Y + row * pitch - 25, 100, 75);
                    btn.addActionListener(e -> JOptionPane.showMessageDialog(this, "เลือกหมวดหมู่: " + cat.label));
                    add(btn);
                }
            }

            setComponentZOrder(header, getComponentCount() - 1);
        }
    }
}