package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.log.DailyLog;
import main.java.healthycalories.model.log.FoodLog;
import main.java.healthycalories.persistence.FoodLogRepository;

/** หน้าบันทึกอาหารของวันที่เลือก แยกรายการตามมื้อและแสดงแคลอรี่รวม */
public class DiaryScreen extends BaseScreen {

    private final Timer resizeTimer = new Timer(120, e -> buildUI());

    public DiaryScreen(HealthyCalories app, MainFrame frame) {
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
        // โหลดเฉพาะรายการของวันที่เลือก แล้วสร้างหัวข้อและรายการอาหารแต่ละมื้อ
        HomeScreen.updateSharedScale(getWidth(), getHeight());
        removeAll();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(new Dimension(HomeScreen.s(HomeScreen.BASE_W),
                HomeScreen.s(HomeScreen.BASE_H)));

        JPanel canvas = new JPanel(null);
        canvas.setBackground(Color.WHITE);
        canvas.setBounds(0, 0, HomeScreen.s(HomeScreen.BASE_W),
                HomeScreen.s(HomeScreen.BASE_H));

        List<FoodLogRepository.LoggedFood> logs;
        DailyLog dailySummary = null;
        boolean loaded = true;
        LocalDate selectedDate = app.getSelectedDiaryDate();
        try {
            FoodLogRepository repository = new FoodLogRepository(app.getCurrentUsername());
            logs = repository.loadByDate(selectedDate);
            List<FoodLog> foodLogs = new ArrayList<>();
            for (FoodLogRepository.LoggedFood loggedFood : logs) {
                foodLogs.add(loggedFood.getFoodLog());
            }
            dailySummary = DailyLog.summarize("", selectedDate, 0, foodLogs);
        } catch (IOException e) {
            JLabel error = new JLabel("โหลดข้อมูลอาหารไม่สำเร็จ: " + e.getMessage(),
                    SwingConstants.CENTER);
            error.setForeground(new Color(0xB00020));
            error.setFont(HomeScreen.font(Font.PLAIN, 13));
            error.setBounds(HomeScreen.s(12), HomeScreen.s(35),
                    HomeScreen.s(HomeScreen.BASE_W - 24), HomeScreen.s(50));
            canvas.add(error);
            logs = java.util.Collections.emptyList();
            loaded = false;
        }

        if (loaded) {
            JLabel title = new JLabel(String.format(java.util.Locale.ROOT,
                    "<html><center>บันทึกวันที่ %02d/%02d/%04d<br>รับประทานแล้ว %.0f kcal</center></html>",
                    selectedDate.getDayOfMonth(), selectedDate.getMonthValue(),
                    selectedDate.getYear(), dailySummary.getCaloriesIn()),
                    SwingConstants.CENTER);
            title.setFont(HomeScreen.font(Font.PLAIN, 18));
            title.setForeground(HomeScreen.PURPLE);
            title.setBounds(HomeScreen.s(55), HomeScreen.s(8),
                    HomeScreen.s(HomeScreen.BASE_W - 110), HomeScreen.s(52));
            canvas.add(title);
        }

        if (loaded && logs.isEmpty()) {
            JLabel emptyMessage = new JLabel(String.format(java.util.Locale.ROOT,
                    "ยังไม่มีรายการบันทึกวันที่ %02d/%02d/%04d",
                    selectedDate.getDayOfMonth(), selectedDate.getMonthValue(),
                    selectedDate.getYear()), SwingConstants.CENTER);
            emptyMessage.setFont(HomeScreen.font(Font.PLAIN, 14));
            emptyMessage.setForeground(Color.BLACK);
            emptyMessage.setBounds(0, HomeScreen.s(350), HomeScreen.s(HomeScreen.BASE_W),
                    HomeScreen.s(40));
            canvas.add(emptyMessage);
        } else {
            JPanel rows = new JPanel();
            rows.setBackground(Color.WHITE);
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            for (MealType meal : MealType.values()) {
                List<FoodLogRepository.LoggedFood> mealLogs = new ArrayList<>();
                for (FoodLogRepository.LoggedFood loggedFood : logs) {
                    if (loggedFood.getFoodLog().getMealType() == meal) {
                        mealLogs.add(loggedFood);
                    }
                }
                if (mealLogs.isEmpty()) {
                    continue;
                }

                JLabel mealHeader = new JLabel(String.format(java.util.Locale.ROOT,
                        "%s · %.0f kcal", meal.getThaiName(),
                        dailySummary.getMealCalories(meal)));
                mealHeader.setFont(HomeScreen.font(Font.BOLD, 14));
                mealHeader.setForeground(HomeScreen.PURPLE);
                mealHeader.setBorder(BorderFactory.createEmptyBorder(
                        HomeScreen.s(12), HomeScreen.s(14), HomeScreen.s(6),
                        HomeScreen.s(14)));
                rows.add(mealHeader);

                for (int i = mealLogs.size() - 1; i >= 0; i--) {
                    FoodLogRepository.LoggedFood loggedFood = mealLogs.get(i);
                    JPanel row = new JPanel(new BorderLayout(10, 2));
                    row.setBackground(Color.WHITE);
                    row.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0,
                                    new Color(0xEEEEEE)),
                            BorderFactory.createEmptyBorder(HomeScreen.s(10),
                                    HomeScreen.s(14), HomeScreen.s(10),
                                    HomeScreen.s(14))));
                    JPanel description = new JPanel();
                    description.setOpaque(false);
                    description.setLayout(new BoxLayout(description, BoxLayout.Y_AXIS));
                    JLabel foodName = new JLabel(loggedFood.getFoodLog().getFood().getName());
                    foodName.setFont(HomeScreen.font(Font.PLAIN, 15));
                    JLabel details = new JLabel(String.format(java.util.Locale.ROOT, "%s %s",
                            formatCalories(loggedFood.getFoodLog().getServings()),
                            loggedFood.getServingUnit()));
                    details.setFont(HomeScreen.font(Font.PLAIN, 11));
                    details.setForeground(new Color(0x888888));
                    description.add(foodName);
                    description.add(details);
                    JLabel calories = new JLabel(String.format(java.util.Locale.ROOT, "%.0f kcal",
                            loggedFood.getFoodLog().getTotalKcal()));
                    calories.setFont(HomeScreen.font(Font.PLAIN, 14));
                    calories.setForeground(HomeScreen.PURPLE);
                    row.add(description, BorderLayout.CENTER);
                    JButton deleteButton = new JButton("ลบ");
                    deleteButton.setFont(HomeScreen.font(Font.PLAIN, 11));
                    deleteButton.setForeground(new Color(0xB00020));
                    deleteButton.setFocusable(false);
                    deleteButton.addActionListener(event -> {
                        int choice = JOptionPane.showConfirmDialog(this,
                                "ต้องการลบ " + loggedFood.getFoodLog().getFood().getName()
                                        + " ออกจากบันทึกหรือไม่?",
                                "ยืนยันการลบ", JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            return;
                        }
                        try {
                            if (new FoodLogRepository(app.getCurrentUsername())
                                    .delete(loggedFood.getFoodLog().getFoodLogId())) {
                                frame.showScreen(MainFrame.DIARY);
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "ไม่พบรายการอาหารที่ต้องการลบ",
                                        "ลบรายการไม่สำเร็จ", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(this,
                                    "ลบรายการอาหารไม่สำเร็จ: " + e.getMessage(),
                                    "เกิดข้อผิดพลาด", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
                    actions.setOpaque(false);
                    actions.add(calories);
                    actions.add(deleteButton);
                    row.add(actions, BorderLayout.EAST);
                    rows.add(row);
                }
            }
            JScrollPane scroll = new JScrollPane(rows);
            scroll.setBounds(HomeScreen.s(12), HomeScreen.s(70),
                    HomeScreen.s(HomeScreen.BASE_W - 24), HomeScreen.s(640));
            scroll.setBorder(BorderFactory.createLineBorder(new Color(0xEEEEEE)));
            scroll.getVerticalScrollBar().setUnitIncrement(HomeScreen.s(16));
            canvas.add(scroll);
        }

        HomeScreen.NavBar nav = new HomeScreen.NavBar(true);
        nav.setBounds(0, HomeScreen.s(730), HomeScreen.s(HomeScreen.BASE_W),
                HomeScreen.s(70));
        nav.info.addActionListener(e -> frame.showScreen(MainFrame.HOME));
        nav.diary.addActionListener(e -> frame.showScreen(MainFrame.DIARY));
        nav.cal.addActionListener(e -> frame.showScreen(MainFrame.CALENDAR));
        canvas.add(nav);
        layered.add(canvas, JLayeredPane.DEFAULT_LAYER);

        HomeScreen.FabButton fab = new HomeScreen.FabButton();
        fab.setBounds(HomeScreen.s(HomeScreen.BASE_W / 2 - 40), HomeScreen.s(690),
                HomeScreen.s(84), HomeScreen.s(84));
        fab.addActionListener(e -> frame.showScreen(MainFrame.FOOD));
        layered.add(fab, JLayeredPane.PALETTE_LAYER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(layered);
        add(center, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private static String formatCalories(double calories) {
        return calories == Math.rint(calories)
                ? Long.toString((long) calories)
                : Double.toString(calories);
    }
}
