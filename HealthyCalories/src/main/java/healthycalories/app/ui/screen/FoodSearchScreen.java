package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.app.ui.BufferedImageIcon;
import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.item.FoodItem;
import main.java.healthycalories.model.log.FoodLog;
import main.java.healthycalories.persistence.FoodHistoryRepository;
import main.java.healthycalories.persistence.FoodLogRepository;
import main.java.healthycalories.persistence.PendingFoodRepository;

/** หน้าจัดการอาหาร มีรายการของฉัน ประวัติการค้นหา และหน้าค้นหาตามหมวดหมู่ */
public class FoodSearchScreen extends BaseScreen {
    private static final int DESIGN_WIDTH = 400;
    private static final int DESIGN_HEIGHT = 800;
    private final FoodLogRepository foodLogRepository = new FoodLogRepository();

    public static final String SCREEN_MINE = "SCREEN_MINE";         // หน้า FoodsearchScreen1 (รายการของฉัน)
    public static final String SCREEN_SEARCH = "SCREEN_SEARCH";     // หน้า FoodsearchScreen2 (ค้นหาล่าสุด)
    public static final String SCREEN_CATEGORY = "SCREEN_CATEGORY"; // หน้า FoodSearchScreen (หมวดหมู่)

    // ตัวจัดการสลับหน้าจอ (CardLayout)
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private String activeScreen = SCREEN_MINE;
    private double currentScaleX = -1;
    private double currentScaleY = -1;
    private String requestedStartScreen = SCREEN_MINE;
    private String selectedMealName = "อาหารเย็น";
    private boolean returnCategoryToApp;
    private ScreenMine mineScreen;
    private ScreenSearch searchScreen;
    private final Timer resizeTimer = new Timer(120, e -> rebuildIfScaleChanged());

    // บันทึกหน้าที่เปิดก่อนหน้า เพื่อให้ปุ่ม "กลับ" ย้อนถูกหน้า
    private String previousScreen = SCREEN_MINE;

    /** เตรียมแท็บค้นหา รายการของฉัน และประวัติ พร้อมตรวจการเปลี่ยนขนาดหน้าต่าง */
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
        // สร้างแท็บย่อยและเปิดแท็บที่ผู้ใช้เลือกหรือที่เรียกมาจากหน้าแรก
        String screenToShow = requestedStartScreen;
        requestedStartScreen = SCREEN_MINE;
        rebuildUI(screenToShow);
    }

    /** เปิดแท็บหมวดหมู่จากหน้าแรกโดยใช้มื้ออาหารเริ่มต้น */
    public void openCategoryFromHome() {
        openCategoryFromHome("อาหารเย็น");
    }

    /** กำหนดมื้ออาหารและเปิดหน้าหมวดหมู่จากหน้าแรก */
    public void openCategoryFromHome(String mealName) {
        selectedMealName = mealName;
        previousScreen = SCREEN_MINE;
        returnCategoryToApp = true;
        requestedStartScreen = SCREEN_CATEGORY;
        frame.showScreen(MainFrame.FOOD);
    }

    private String getSelectedMealName() {
        return selectedMealName;
    }

    private MealType getSelectedMealType() {
        return getMealType(selectedMealName);
    }

    /** สร้างแท็บและปรับขนาดองค์ประกอบตามพื้นที่ที่ใช้งานจริง */
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
        mineScreen = new ScreenMine(this);
        addScreen(mineScreen, SCREEN_MINE, scaleX, scaleY);
        searchScreen = new ScreenSearch(this);
        addScreen(searchScreen, SCREEN_SEARCH, scaleX, scaleY);
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

    /** สร้าง UI ใหม่เมื่อการปรับขนาดหน้าต่างเปลี่ยนสเกล */
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

    /** เพิ่มแท็บลงใน CardLayout และปรับขนาดคอมโพเนนต์ภายใน */
    private void addScreen(JPanel screen, String name, double scaleX, double scaleY) {
        screen.setPreferredSize(new Dimension(
                scaled(DESIGN_WIDTH, scaleX), scaled(DESIGN_HEIGHT, scaleY)));
        scaleComponentTree(screen, scaleX, scaleY);
        mainContainer.add(screen, name);
    }

    /** แปลงค่าขนาดตามสเกลให้เป็นจำนวนพิกเซลอย่างน้อยหนึ่งพิกเซล */
    private static int scaled(int value, double scale) {
        return (int) Math.round(value * scale);
    }

    /** ตั้งรูปแบบปุ่มลบให้เหมาะกับสเกลปัจจุบัน */
    static void styleDeleteButton(JButton button, double scaleY) {
        button.setFont(new Font("Tahoma", Font.PLAIN, Math.max(9, scaled(11, scaleY))));
        button.setForeground(new Color(0xB00020));
        button.setFocusable(false);
        button.setMargin(new Insets(scaled(2, scaleY), scaled(6, scaleY),
                scaled(2, scaleY), scaled(6, scaleY)));
        Dimension size = new Dimension(scaled(52, scaleY), scaled(24, scaleY));
        button.setPreferredSize(size);
        button.setMinimumSize(size);
    }

    /** ค้นหารายการอาหารและอัปเดตเมนูผลลัพธ์ใต้ช่องค้นหา */
    private void showFoodMatches(JTextField searchField, JPopupMenu resultsPopup, String keyword) {
        resultsPopup.removeAll();
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isEmpty()) {
            resultsPopup.setVisible(false);
            return;
        }

        List<FoodItem> matches;
        try {
            matches = foodLogRepository.searchFood(normalizedKeyword);
        } catch (IOException e) {
            resultsPopup.setVisible(false);
            JOptionPane.showMessageDialog(this,
                    "ไม่สามารถอ่านรายชื่ออาหารจาก foods.csv ได้:\n" + e.getMessage(),
                    "ค้นหาอาหารไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int matchCount = matches.size();
        for (int i = 0; i < matches.size(); i++) {
            FoodItem food = matches.get(i);
            if (i < 10) {
                JMenuItem result = new JMenuItem(
                        food.getName() + "  -  " + formatCalories(food.getKcal()) + " kcal");
                result.setFont(new Font("Tahoma", Font.PLAIN,
                        Math.max(16, scaled(18, currentScaleY))));
                result.setForeground(Color.BLACK);
                result.setFocusable(false);
                result.setRequestFocusEnabled(false);
                result.setPreferredSize(new Dimension(
                        Math.max(searchField.getWidth(), scaled(300, currentScaleY)),
                        Math.max(34, scaled(40, currentScaleY))));
                result.addActionListener(e -> {
                    resultsPopup.setVisible(false);
                    addFoodToMyList(new FoodEntry(food.getItemId(), food.getName(),
                            food.getKcal()));
                });
                resultsPopup.add(result);
            }
            if (i == 9) {
                break;
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

    /** จัดรูปแบบเมนูแจ้งสถานะผลการค้นหาหรือไม่มีรายการ */
    private void styleResultMessage(JMenuItem item, JTextField searchField) {
        item.setFont(new Font("Tahoma", Font.PLAIN,
                Math.max(16, scaled(18, currentScaleY))));
        item.setPreferredSize(new Dimension(
                Math.max(searchField.getWidth(), scaled(300, currentScaleY)),
                Math.max(34, scaled(40, currentScaleY))));
    }

    /** แปลงแคลอรี่เป็นข้อความสั้นสำหรับแสดงในผลลัพธ์ */
    private static String formatCalories(double calories) {
        return calories == Math.rint(calories)
                ? Long.toString((long) calories)
                : Double.toString(calories);
    }

    private void showCategoryFoods(String category, JPanel results, JLabel resultCount,
                                   String searchText) {
        List<FoodEntry> matches = new ArrayList<>();
        try {
            String filter = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
            for (FoodItem food : foodLogRepository.filterByCategory(category)) {
                if (filter.isEmpty()
                        || food.getName().toLowerCase(Locale.ROOT).contains(filter)) {
                    matches.add(new FoodEntry(food.getItemId(), food.getName(), food.getKcal()));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "ไม่สามารถอ่านรายการอาหารจาก foods.csv ได้:\n" + e.getMessage(),
                    "โหลดอาหารไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        results.removeAll();
        resultCount.setText(category + "  ·  " + matches.size() + " รายการ");
        if (matches.isEmpty()) {
            JLabel empty = new JLabel("ยังไม่มีรายการอาหารในหมวดนี้", SwingConstants.CENTER);
            empty.setFont(new Font("Tahoma", Font.PLAIN, 15));
            empty.setForeground(new Color(0x8C8C8C));
            results.add(empty);
        } else {
            for (FoodEntry food : matches) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)),
                        BorderFactory.createEmptyBorder(9, 12, 9, 12)));
                JLabel name = new JLabel(food.name);
                name.setFont(new Font("Tahoma", Font.PLAIN, 14));
                JLabel calories = new JLabel(formatCalories(food.calories) + " kcal");
                calories.setFont(new Font("Tahoma", Font.PLAIN, 13));
                calories.setForeground(new Color(0x7A4A86));
                row.add(name, BorderLayout.CENTER);
                row.add(calories, BorderLayout.EAST);
                attachFoodSelectionListener(row, food);
                results.add(row);
            }
        }
        results.revalidate();
        results.repaint();
    }

    /** ผูกการเลือกอาหารจากคอมโพเนนต์เข้ากับการเพิ่มลงรายการ */
    private void attachFoodSelectionListener(Component component, FoodEntry food) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (component instanceof JComponent) {
            ((JComponent) component).setToolTipText("กดเพื่อเลือกจำนวนและเพิ่มเข้ารายการรอยืนยัน");
        }
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                addFoodToMyList(food);
            }
        });
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                attachFoodSelectionListener(child, food);
            }
        }
    }

    /** สรุปข้อมูลอาหารที่ต้องใช้แสดงและเลือกจากผลการค้นหา */
    private static final class FoodEntry {
        private final String id;
        private final String name;
        private final double calories;

        private FoodEntry(String id, String name, double calories) {
            this.id = id;
            this.name = name;
            this.calories = calories;
        }
    }

    /** บันทึกอาหารที่เลือกลงรายการส่วนตัวของสมาชิก */
    private void addFoodToMyList(FoodEntry entry) {
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 1000.0, 0.5));
        JComboBox<String> unit = new JComboBox<>(new String[] {
            "จาน", "ที่", "ชิ้น", "ถ้วย", "แก้ว", "ฟอง", "ห่อ", "กรัม"
        });
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("อาหาร"));
        form.add(new JLabel(entry.name));
        form.add(new JLabel("จำนวน"));
        form.add(quantity);
        form.add(new JLabel("หน่วย"));
        form.add(unit);
        form.add(new JLabel("แคลอรี่ต่อหน่วย"));
        form.add(new JLabel(formatCalories(entry.calories) + " kcal"));

        Object[] options = {"ยืนยัน", "ยกเลิก"};
        int choice = JOptionPane.showOptionDialog(this, form,
                "เพิ่มอาหารในรายการของฉัน", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice != 0) {
            return;
        }

        LocalDate date = LocalDate.now();
        String servingUnit = (String) unit.getSelectedItem();
        FoodItem item = new FoodItem(entry.id, entry.name, entry.calories,
                null, servingUnit, "");
        FoodLog foodLog = new FoodLog(date.toString(), item,
                getMealType(selectedMealName), ((Number) quantity.getValue()).doubleValue());
        try {
            new PendingFoodRepository(app.getCurrentUsername())
                    .add(foodLog, date.toString(), servingUnit);
            try {
                new FoodHistoryRepository(app.getCurrentUsername()).saveSelection(item);
            } catch (IOException historyException) {
                JOptionPane.showMessageDialog(this,
                        "เพิ่มอาหารเข้ารายการแล้ว แต่บันทึกประวัติการค้นหาไม่สำเร็จ:\n"
                                + historyException.getMessage(),
                        "บันทึกประวัติไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            }
            JOptionPane.showMessageDialog(this,
                    "เพิ่ม " + entry.name + " จำนวน "
                            + formatCalories(foodLog.getServings()) + " " + servingUnit
                            + " รวม " + formatCalories(foodLog.getTotalKcal())
                            + " kcal เข้ารายการแล้ว\nกดยืนยันในรายการของฉันเพื่อบันทึก",
                    "เพิ่มอาหารสำเร็จ", JOptionPane.INFORMATION_MESSAGE);
            showScreen(SCREEN_MINE);
        } catch (IOException | RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "เพิ่มอาหารเข้ารายการไม่สำเร็จ:\n" + e.getMessage(),
                    "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** แปลงชื่อมื้อภาษาไทยเป็นค่าประเภทมื้อที่โมเดลใช้ */
    private static MealType getMealType(String mealName) {
        switch (mealName) {
            case "อาหารเช้า":
                return MealType.BREAKFAST;
            case "อาหารเที่ยง":
                return MealType.LUNCH;
            case "อาหารว่าง":
                return MealType.SNACK;
            case "อาหารเย็น":
                return MealType.DINNER;
            default:
                throw new IllegalArgumentException("ไม่รู้จักประเภทมื้ออาหาร: " + mealName);
        }
    }

    /** ปรับขนาดคอมโพเนนต์ทุกชั้นตามสเกลแนวนอนและแนวตั้ง */
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

    /** สร้างกราฟิกที่แปลงพิกัดตรรกะให้ตรงกับคอมโพเนนต์ที่ย่อขยาย */
    private static Graphics2D createLogicalGraphics(Graphics graphics, JComponent component) {
        Graphics2D logicalGraphics = (Graphics2D) graphics.create();
        logicalGraphics.scale(getComponentScaleX(component), getComponentScaleY(component));
        return logicalGraphics;
    }

    /** คำนวณสเกลแนวนอนจากขนาดจริงเทียบกับขนาดออกแบบ */
    private static double getComponentScaleX(JComponent component) {
        Object scale = component.getClientProperty("foodSearchScaleX");
        return scale instanceof Number ? ((Number) scale).doubleValue() : 1.0;
    }

    /** คำนวณสเกลแนวตั้งจากขนาดจริงเทียบกับขนาดออกแบบ */
    private static double getComponentScaleY(JComponent component) {
        Object scale = component.getClientProperty("foodSearchScaleY");
        return scale instanceof Number ? ((Number) scale).doubleValue() : 1.0;
    }

    /** สลับแท็บภายในและจดจำแท็บก่อนหน้าเพื่อรองรับปุ่มย้อนกลับ */
    public void showScreen(String name) {
        if (!SCREEN_CATEGORY.equals(name)) {
            returnCategoryToApp = false;
        } else if (!returnCategoryToApp
                && !SCREEN_MINE.equals(activeScreen)
                && !SCREEN_SEARCH.equals(activeScreen)
                && !SCREEN_CATEGORY.equals(activeScreen)) {
            selectedMealName = "อาหารเย็น";
        }
        if (!name.equals(SCREEN_CATEGORY)) {
            previousScreen = name;
        }
        activeScreen = name;
        cardLayout.show(mainContainer, name);
        if (SCREEN_MINE.equals(name) && mineScreen != null) {
            mineScreen.refresh();
        } else if (SCREEN_SEARCH.equals(name) && searchScreen != null) {
            searchScreen.refresh(this, true);
        }
    }

    /** ย้อนกลับไปแท็บก่อนหน้า หรือกลับสู่งานหลักตามบริบทการเปิด */
    public void goBack() {
        if (returnCategoryToApp) {
            returnCategoryToApp = false;
            goBackToAppScreen();
            return;
        }
        showScreen(previousScreen);
    }

    /** ออกจากหน้าค้นหาและคืนไปยังหน้าจอแอปที่เรียกเข้ามา */
    private void goBackToAppScreen() {
        String destination = frame.getPreviousScreen();
        if (destination == null || MainFrame.FOOD.equals(destination)) {
            destination = MainFrame.HOME;
        }
        frame.showScreen(destination);
    }

    // ฟังก์ชันโหลดรูปภาพจาก ProjectFolders/Photo
    /** โหลดไอคอนหมวดหมู่และปรับให้มีขนาดตามพื้นที่ปุ่ม */
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
    /** แท็บรายการของฉัน: แสดงอาหารที่รอยืนยันและปุ่มบันทึกลงไดอารี่ */
    /** หน้าย่อยที่แสดงและจัดการรายการอาหารส่วนตัวของสมาชิก */
    static class ScreenMine extends JPanel {
        /** แสดงและจัดการรายการอาหารส่วนตัวของสมาชิก */
        private final FoodSearchScreen app;
        private final JPanel content = new JPanel(null);

        ScreenMine(FoodSearchScreen app) {
            this.app = app;
            setLayout(null);
            setBackground(Color.WHITE);

            add(createHeader(app, SCREEN_MINE, app::goBackToAppScreen));
            content.setBounds(15, 190, 370, 590);
            content.setBackground(Color.WHITE);
            add(content);
        }

        void refresh() {
            content.removeAll();
            List<PendingFoodRepository.PendingFood> pendingFoods;
            try {
                pendingFoods = new PendingFoodRepository(app.app.getCurrentUsername())
                        .loadAll();
                MealType selectedMeal = app.getSelectedMealType();
                pendingFoods.removeIf(pendingFood ->
                        pendingFood.getMealType() != selectedMeal);
            } catch (IOException e) {
                JLabel error = new JLabel("โหลดรายการอาหารที่รอยืนยันไม่สำเร็จ: " + e.getMessage(),
                        SwingConstants.CENTER);
                error.setForeground(new Color(0xB00020));
                error.setBounds(0, scaled(100, app.currentScaleY),
                        scaled(370, app.currentScaleX), scaled(60, app.currentScaleY));
                content.add(error);
                content.revalidate();
                content.repaint();
                return;
            }

            if (pendingFoods.isEmpty()) {
                JLabel boxImage = new JLabel("", SwingConstants.CENTER);
                boxImage.setBounds(scaled(85, app.currentScaleX),
                        scaled(125, app.currentScaleY), scaled(200, app.currentScaleX),
                        scaled(150, app.currentScaleY));
                boxImage.setIcon(loadIcon("box.png", scaled(190, app.currentScaleY),
                        scaled(140, app.currentScaleY)));
                content.add(boxImage);

                JLabel text1 = new JLabel("ยังไม่มีรายการอาหารที่รอยืนยันสำหรับ"
                        + app.getSelectedMealName(),
                        SwingConstants.CENTER);
                text1.setFont(new Font("Tahoma", Font.PLAIN,
                        Math.max(12, scaled(20, app.currentScaleY))));
                text1.setBounds(0, scaled(275, app.currentScaleY),
                        scaled(370, app.currentScaleX), scaled(32, app.currentScaleY));
                content.add(text1);

                JLabel text2 = new JLabel("(เพิ่มอาหารแล้วกดยืนยันเพื่อบันทึก)", SwingConstants.CENTER);
                text2.setFont(new Font("Tahoma", Font.PLAIN,
                        Math.max(10, scaled(14, app.currentScaleY))));
                text2.setForeground(Color.GRAY);
                text2.setBounds(0, scaled(307, app.currentScaleY),
                        scaled(370, app.currentScaleX), scaled(25, app.currentScaleY));
                content.add(text2);
            } else {
                JPanel rows = new JPanel();
                rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
                rows.setBackground(Color.WHITE);
                for (int i = pendingFoods.size() - 1; i >= 0; i--) {
                    PendingFoodRepository.PendingFood pendingFood = pendingFoods.get(i);
                    JPanel row = new JPanel(new BorderLayout(8, 3));
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0,
                                    new Color(0xEEEEEE)),
                            BorderFactory.createEmptyBorder(9, 10, 9, 10)));
                    JPanel description = new JPanel();
                    description.setOpaque(false);
                    description.setLayout(new BoxLayout(description, BoxLayout.Y_AXIS));
                    JLabel foodName = new JLabel(pendingFood.getFoodName());
                    foodName.setFont(new Font("Tahoma", Font.PLAIN,
                            Math.max(10, scaled(14, app.currentScaleY))));
                    JLabel detail = new JLabel(pendingFood.getDate() + " · "
                            + pendingFood.getMealType().getThaiName() + " · "
                            + formatCalories(pendingFood.getServings()) + " "
                            + pendingFood.getServingUnit());
                    detail.setFont(new Font("Tahoma", Font.PLAIN,
                            Math.max(9, scaled(11, app.currentScaleY))));
                    detail.setForeground(new Color(0x888888));
                    description.add(foodName);
                    description.add(detail);
                    JLabel calories = new JLabel(formatCalories(
                            pendingFood.getCaloriesPerServing() * pendingFood.getServings())
                            + " kcal");
                    calories.setForeground(new Color(0x7A4A86));
                    calories.setFont(new Font("Tahoma", Font.PLAIN,
                            Math.max(9, scaled(12, app.currentScaleY))));
                    JButton deleteButton = new JButton("ลบ");
                    styleDeleteButton(deleteButton, app.currentScaleY);
                    deleteButton.addActionListener(event -> {
                        int choice = JOptionPane.showConfirmDialog(this,
                                "ต้องการลบ " + pendingFood.getFoodName()
                                        + " ออกจากรายการที่รอยืนยันหรือไม่?",
                                "ยืนยันการลบ", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            return;
                        }
                        try {
                            if (new PendingFoodRepository(app.app.getCurrentUsername())
                                    .delete(pendingFood.getId())) {
                                refresh();
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "ไม่พบรายการที่ต้องการลบ",
                                        "ลบรายการไม่สำเร็จ", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(this,
                                    "ลบรายการอาหารไม่สำเร็จ:\n" + e.getMessage(),
                                    "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
                    actions.setOpaque(false);
                    actions.add(calories);
                    actions.add(deleteButton);
                    row.add(description, BorderLayout.CENTER);
                    row.add(actions, BorderLayout.EAST);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    rows.add(row);
                }
                JScrollPane scroll = new JScrollPane(rows);
                scroll.setBounds(0, 0, scaled(370, app.currentScaleX),
                        scaled(520, app.currentScaleY));
                scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));
                scroll.getVerticalScrollBar().setUnitIncrement(16);
                content.add(scroll);

                JButton confirmButton = new JButton("ยืนยันและบันทึกรายการ");
                confirmButton.setFont(new Font("Tahoma", Font.PLAIN,
                        Math.max(10, scaled(14, app.currentScaleY))));
                confirmButton.setBackground(new Color(0xD9B8E8));
                confirmButton.setFocusPainted(false);
                confirmButton.setBounds(0, scaled(532, app.currentScaleY),
                        scaled(370, app.currentScaleX), scaled(42, app.currentScaleY));
                confirmButton.addActionListener(event -> confirmPendingFoods());
                content.add(confirmButton);
            }
            content.revalidate();
            content.repaint();
        }

        /** ยืนยันรายการรอทั้งหมดและบันทึกลงไดอารี่ของวันที่เลือก */
        private void confirmPendingFoods() {
            PendingFoodRepository pendingRepository =
                    new PendingFoodRepository(app.app.getCurrentUsername());
            FoodLogRepository foodLogRepository =
                    new FoodLogRepository(app.app.getCurrentUsername());
            try {
                List<PendingFoodRepository.PendingFood> pendingFoods =
                        pendingRepository.loadAll();
                MealType selectedMeal = app.getSelectedMealType();
                pendingFoods.removeIf(pendingFood ->
                        pendingFood.getMealType() != selectedMeal);
                if (pendingFoods.isEmpty()) {
                    return;
                }
                for (PendingFoodRepository.PendingFood pendingFood : pendingFoods) {
                    LocalDate foodDate = LocalDate.parse(pendingFood.getDate());
                    FoodLog foodLog = new FoodLog(pendingFood.getDate(),
                            new FoodItem(pendingFood.getFoodItemId(), pendingFood.getFoodName(),
                                    pendingFood.getCaloriesPerServing(), null,
                                    pendingFood.getServingUnit(), ""),
                            pendingFood.getMealType(), pendingFood.getServings());
                    foodLog.setFoodLogId(pendingFood.getId());
                    if (!foodLogRepository.containsFoodLogId(pendingFood.getId())) {
                        foodLogRepository.save(foodLog, foodDate,
                                pendingFood.getServingUnit());
                    }
                    if (!pendingRepository.delete(pendingFood.getId())) {
                        throw new IOException("ไม่สามารถนำรายการที่ยืนยันออกจากคิวได้: "
                                + pendingFood.getFoodName());
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "บันทึกรายการอาหารมื้อ" + selectedMeal.getThaiName()
                                + "ลงบันทึกแล้ว",
                        "ยืนยันสำเร็จ", JOptionPane.INFORMATION_MESSAGE);
                app.frame.showScreen(MainFrame.DIARY);
            } catch (IOException | RuntimeException e) {
                JOptionPane.showMessageDialog(this,
                        "ยืนยันรายการอาหารไม่สำเร็จ:\n" + e.getMessage(),
                        "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                refresh();
            }
        }
    }

    // =========================================================================
    // หน้าที่ 2: ประวัติการค้นหาล่าสุด (FoodsearchScreen2)
    // =========================================================================
    /** แท็บค้นหา: แสดงประวัติอาหารที่ค้นหาและเลือกอาหารเพื่อเพิ่มรายการ */
    /** หน้าย่อยที่แสดงประวัติการค้นหาและรายการอาหารล่าสุด */
    static class ScreenSearch extends JPanel {
        /** แสดงประวัติการค้นหาและรายการอาหารล่าสุด */
        private final JPanel content = new JPanel(null);

        ScreenSearch(FoodSearchScreen app) {
            setLayout(null);
            setBackground(Color.WHITE);

            add(createHeader(app, SCREEN_SEARCH, app::goBackToAppScreen));
            content.setBounds(0, 180, DESIGN_WIDTH, DESIGN_HEIGHT - 180);
            content.setOpaque(false);
            add(content);
        }

        void refresh(FoodSearchScreen app, boolean alreadyScaled) {
            double scaleX = alreadyScaled ? app.currentScaleX : 1.0;
            double scaleY = alreadyScaled ? app.currentScaleY : 1.0;
            content.removeAll();
            List<FoodHistoryRepository.HistoryEntry> entries;
            try {
                entries = new FoodHistoryRepository(app.app.getCurrentUsername())
                        .loadRecent(50);
            } catch (IOException e) {
                JLabel error = new JLabel("โหลดประวัติการค้นหาไม่สำเร็จ: " + e.getMessage(),
                        SwingConstants.CENTER);
                error.setForeground(new Color(0xB00020));
                error.setFont(new Font("Tahoma", Font.PLAIN, Math.max(10, scaled(14, scaleY))));
                error.setBounds(scaled(12, scaleX), scaled(30, scaleY),
                        scaled(376, scaleX), scaled(60, scaleY));
                content.add(error);
                content.revalidate();
                content.repaint();
                return;
            }

            if (entries.isEmpty()) {
                JLabel emptyText = new JLabel("ยังไม่มีรายการล่าสุดในขณะนี้", SwingConstants.CENTER);
                emptyText.setFont(new Font("Tahoma", Font.PLAIN, Math.max(12, scaled(20, scaleY))));
                emptyText.setBounds(scaled(30, scaleX), scaled(330, scaleY),
                        scaled(340, scaleX), scaled(35, scaleY));
                content.add(emptyText);

                JLabel boxImage = new JLabel("", SwingConstants.CENTER);
                boxImage.setBounds(scaled(100, scaleX), scaled(210, scaleY),
                        scaled(200, scaleX), scaled(150, scaleY));
                boxImage.setIcon(loadIcon("box.png", scaled(190, scaleY),
                        scaled(140, scaleY)));
                content.add(boxImage);
                content.revalidate();
                content.repaint();
                return;
            }

            JPanel rows = new JPanel();
            rows.setBackground(Color.WHITE);
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (FoodHistoryRepository.HistoryEntry entry : entries) {
                JPanel row = new JPanel(new BorderLayout(8, 3));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)),
                        BorderFactory.createEmptyBorder(scaled(9, scaleY), scaled(12, scaleX),
                                scaled(9, scaleY), scaled(12, scaleX))));
                JPanel description = new JPanel();
                description.setOpaque(false);
                description.setLayout(new BoxLayout(description, BoxLayout.Y_AXIS));
                JLabel name = new JLabel(entry.getFoodName());
                name.setFont(new Font("Tahoma", Font.PLAIN, Math.max(10, scaled(14, scaleY))));
                JLabel selectedAt = new JLabel(entry.getSelectedAt().format(timestampFormat));
                selectedAt.setFont(new Font("Tahoma", Font.PLAIN, Math.max(9, scaled(11, scaleY))));
                selectedAt.setForeground(new Color(0x888888));
                description.add(name);
                description.add(selectedAt);
                JLabel calories = new JLabel(formatCalories(entry.getCalories()) + " kcal");
                calories.setFont(new Font("Tahoma", Font.PLAIN, Math.max(9, scaled(13, scaleY))));
                calories.setForeground(new Color(0x7A4A86));
                row.add(description, BorderLayout.CENTER);
                JButton deleteButton = new JButton("ลบ");
                styleDeleteButton(deleteButton, scaleY);
                deleteButton.addActionListener(event -> {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "ต้องการลบ " + entry.getFoodName() + " ออกจากประวัติการค้นหาหรือไม่?",
                            "ยืนยันการลบ", JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                    try {
                        if (new FoodHistoryRepository(app.app.getCurrentUsername())
                                .deleteSelection(entry)) {
                            refresh(app, alreadyScaled);
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "ไม่พบรายการประวัติที่ต้องการลบ",
                                    "ลบรายการไม่สำเร็จ", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (IOException | RuntimeException e) {
                        JOptionPane.showMessageDialog(this,
                                "ลบประวัติการค้นหาไม่สำเร็จ: " + e.getMessage(),
                                "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                    }
                });
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT,
                        scaled(6, scaleX), 0));
                actions.setOpaque(false);
                actions.add(calories);
                actions.add(deleteButton);
                row.add(actions, BorderLayout.EAST);
                rows.add(row);
            }
            JScrollPane scroll = new JScrollPane(rows);
            scroll.setBounds(scaled(12, scaleX), scaled(10, scaleY),
                    scaled(376, scaleX), scaled(590, scaleY));
            scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));
            scroll.getVerticalScrollBar().setUnitIncrement(scaled(16, scaleY));
            content.add(scroll);
            content.revalidate();
            content.repaint();
        }
    }

    /** สร้างปุ่มนำทางหมวดหมู่และกำหนดสถานะหน้าที่กำลังเปิด */
    private static JButton createCategoryNavButton(FoodSearchScreen app, boolean active) {
        String title = active
                ? "<html><center><b><u>ค้นหาจาก<br>หมวดหมู่</u></b></center></html>"
                : "ค้นหาจากหมวดหมู่";
        int iconWidth = active ? 24 : 35;
        int iconHeight = active ? 20 : 28;
        JButton categoryBtn = new JButton(title, loadIcon("food.png", iconWidth, iconHeight)) {
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
        Font categoryFont = new Font("Tahoma", Font.PLAIN, active ? 18 : 11);
        categoryBtn.setFont(active
                ? categoryFont.deriveFont(activeTabFontAttributes()) : categoryFont);
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

    /** สร้างหัวแท็บพร้อมปุ่มย้อนกลับและตัวเลือกประเภทรายการ */
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

        JLabel title = new JLabel(app.getSelectedMealName(), SwingConstants.CENTER);
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

        boolean searchActive = SCREEN_SEARCH.equals(activeTab);
        JButton tabSearch = new JButton("ค้นหา");
        Font searchFont = new Font("Tahoma", Font.PLAIN, 18);
        tabSearch.setFont(searchActive
                ? searchFont.deriveFont(activeTabFontAttributes()) : searchFont);
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
        tabMine.setFont(mineActive
                ? mineFont.deriveFont(activeTabFontAttributes()) : mineFont);
        styleHeaderTab(tabMine, mineActive);
        tabMine.setBounds(260, 105, 130, 55);
        tabMine.addActionListener(e -> app.showScreen(SCREEN_MINE));
        header.add(tabMine);
        return header;
    }

    /** ตั้งรูปแบบแท็บส่วนหัวและแสดงเส้นใต้แท็บที่เลือกอยู่ */
    private static void styleHeaderTab(AbstractButton tab, boolean active) {
        Color accent = new Color(0x7B3A87);
        tab.setForeground(active ? accent : Color.BLACK);
        tab.setHorizontalAlignment(SwingConstants.CENTER);
        tab.setVerticalAlignment(SwingConstants.CENTER);
        tab.setContentAreaFilled(false);
        tab.setBorderPainted(false);
        tab.setFocusPainted(false);
        tab.setOpaque(false);
        tab.setFocusable(false);
        tab.setMargin(new Insets(0, 4, 0, 4));
        if (active) {
            tab.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, accent));
        } else {
            tab.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        }
        if (!active) {
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    /** คืนคุณลักษณะฟอนต์ตัวหนาและขีดเส้นใต้สำหรับแท็บที่เลือก */
    private static Map<TextAttribute, Object> activeTabFontAttributes() {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        attributes.put(TextAttribute.SIZE, 21f);
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        return attributes;
    }

    // =========================================================================
    // หน้าที่ 3: เลือกตามหมวดหมู่ (FoodSearchScreen เดิม)
    // =========================================================================
    /** หน้าย่อยแท็บหมวดหมู่ที่แสดงอาหารให้เลือกตามมื้ออาหาร */
    static class ScreenCategory extends JPanel {
        private static final Color TAB_ACTIVE = new Color(0xFCEBFD);
        private static final Color BACK_PILL  = new Color(0xD9D9D9);
        private static final Color LABEL_GRAY = new Color(0x8C8C8C);

        /** ข้อมูลหมวดหมู่ที่ใช้สร้างตัวเลือกในหน้าค้นหา */
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

            JLabel title = new JLabel(app.getSelectedMealName(), SwingConstants.CENTER);
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
            styleHeaderTab(tabSearch, false);
            tabSearch.setBounds(20, 105, 110, 55);
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
            styleHeaderTab(tabMine, false);
            tabMine.setBounds(260, 105, 130, 55);
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
            final Category[] selectedCategory = {null};
            final boolean[] showingCategoryFoods = {false};
            add(backPill);

            JPanel categoryGrid = new JPanel(null);
            categoryGrid.setOpaque(false);
            categoryGrid.setBounds(0, 0, 400, 800);
            add(categoryGrid);

            JLabel resultCount = new JLabel("", SwingConstants.CENTER);
            resultCount.setFont(new Font("Tahoma", Font.PLAIN, 15));
            resultCount.setForeground(new Color(0x7A4A86));
            resultCount.setBounds(12, 228, 376, 28);
            resultCount.setVisible(false);
            add(resultCount);

            JPanel foodResults = new JPanel();
            foodResults.setBackground(Color.WHITE);
            foodResults.setLayout(new BoxLayout(foodResults, BoxLayout.Y_AXIS));
            JScrollPane foodScroll = new JScrollPane(foodResults);
            foodScroll.setBounds(12, 258, 376, 530);
            foodScroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));
            foodScroll.getVerticalScrollBar().setUnitIncrement(16);
            foodScroll.setVisible(false);
            add(foodScroll);

            backPill.addActionListener(e -> {
                if (showingCategoryFoods[0]) {
                    showingCategoryFoods[0] = false;
                    selectedCategory[0] = null;
                    categoryGrid.setVisible(true);
                    resultCount.setVisible(false);
                    foodScroll.setVisible(false);
                    backPill.setText("กลับ");
                    searchField.setText("");
                } else {
                    app.goBack();
                }
            });

            searchField.getDocument().addDocumentListener(new DocumentListener() {
                private void updateResults() {
                    if (showingCategoryFoods[0] && selectedCategory[0] != null) {
                        String filter = searchField.getText();
                        if (hint.equals(filter)) {
                            filter = "";
                        }
                        app.showCategoryFoods(selectedCategory[0].label, foodResults,
                                resultCount, filter);
                    }
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
                    categoryGrid.add(btn);
                    btn.addActionListener(e -> {
                        selectedCategory[0] = cat;
                        showingCategoryFoods[0] = true;
                        categoryGrid.setVisible(false);
                        resultCount.setVisible(true);
                        foodScroll.setVisible(true);
                        backPill.setText("หมวดหมู่");
                        searchField.setText("");
                        app.showCategoryFoods(cat.label, foodResults, resultCount, "");
                    });
                }
            }

            setComponentZOrder(header, getComponentCount() - 1);
        }
    }
}