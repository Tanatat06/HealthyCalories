package main.java.healthycalories.app.ui;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.screen.*;
import main.java.healthycalories.app.ui.screen.SplashScreen;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainFrame extends JFrame {

    public static final String SPLASH     = "splash";
    public static final String LOGIN      = "login";
    public static final String REGISTER   = "register";
    public static final String ONBOARDING = "onboarding";
    public static final String HOME       = "home";
    public static final String FOOD       = "food";
    public static final String CALENDAR   = "calendar";
    public static final String WEIGHT     = "weight";
    public static final String PROFILE    = "profile";
    public static final String SETTINGS   = "settings";

    // หน้าที่ไม่ต้องแสดงเมนูด้านข้าง
    private static final Set<String> NO_MENU_SCREENS =
            Set.of(SPLASH, LOGIN, REGISTER, ONBOARDING);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final Map<String, BaseScreen> screens = new LinkedHashMap<>();
    private final HealthyCalories app;
    private final SideMenu sideMenu;

    public MainFrame(HealthyCalories app) {
        this.app = app;
        setTitle("HealthyCalories - Desktop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        sideMenu = new SideMenu(this);
        add(sideMenu, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        registerScreen(SPLASH,     new SplashScreen(app, this));
        registerScreen(LOGIN,      new LoginScreen(app, this));
        registerScreen(REGISTER,   new RegisterScreen(app, this));
        registerScreen(ONBOARDING, new OnboardingScreen(app, this));
        registerScreen(HOME,       new HomeScreen(app, this));
        registerScreen(FOOD,       new FoodSearchScreen(app, this));
        registerScreen(CALENDAR,   new CalendarScreen(app, this));
        registerScreen(WEIGHT,     new WeightHistoryScreen(app, this));
        registerScreen(PROFILE,    new ProfileScreen(app, this));
        registerScreen(SETTINGS,   new SettingsScreen(app, this));

        showScreen(SPLASH);
    }

    public void registerScreen(String name, BaseScreen screen) {
        screens.put(name, screen);
        cardPanel.add(screen, name);
    }

    public void showScreen(String name) {
        BaseScreen screen = screens.get(name);

        if (screen == null) {
            showDailog("ไม่พบหน้าจอ " + name);
            return;
        }
        screen.refresh();
        sideMenu.setVisible(!NO_MENU_SCREENS.contains(name));
        cardLayout.show(cardPanel, name);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public void showDailog(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}