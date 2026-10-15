package healthycalories.app.ui;

import healthycalories.app.HealthyCalories;
import healthycalories.app.ui.screen.*;
import healthycalories.app.ui.screen.SplashScreen;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** หน้าต่างหลักของแอป สลับหน้าจอด้วย CardLayout และจัดวางแถบหัว/เมนูด้านข้าง */
public class MainFrame extends JFrame {

    public static final String SPLASH     = "splash";
    public static final String LOGIN      = "login";
    public static final String REGISTER   = "register";
    public static final String ONBOARDING = "onboarding";
    public static final String HOME       = "home";
    public static final String DIARY      = "diary";
    public static final String FOOD       = "food";
    public static final String CALENDAR   = "calendar";
    public static final String WEIGHT     = "weight";
    public static final String PROFILE    = "profile";
    public static final String SETTINGS   = "settings";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final JPanel menuBar = new JPanel(new BorderLayout());
    private final Map<String, BaseScreen> screens = new LinkedHashMap<>();
    private final HealthyCalories app;
    private final SideMenu sideMenu;
    private String previousScreen;
    private String currentScreen;
    private final JButton menuToggle = new MenuToggleButton();
    private final JLayeredPane rootPane = new JLayeredPane();
    private final JPanel appContent = new JPanel(new BorderLayout());
    private final JPanel menuOverlay = new JPanel(null);
    private final JPanel dimmer = new JPanel() {
        @Override
        protected void paintComponent(Graphics graphics) {
            graphics.setColor(new Color(0, 0, 0, 110));
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }
    };
    private boolean menuOpen = false;

    public MainFrame(HealthyCalories app) {
        this.app = app;
        setTitle("HealthyCalories");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        rootPane.setLayout(null);
        appContent.setBounds(0, 0, getWidth(), getHeight());
        menuBar.setBackground(new Color(234, 187, 232));
        menuBar.setPreferredSize(new Dimension(0, 80));
        menuToggle.setToolTipText("เปิด/ปิดเมนู");
        menuToggle.getAccessibleContext().setAccessibleName("เปิดหรือปิดเมนู");
        menuToggle.addActionListener(e -> toggleMenu());
        menuBar.add(menuToggle, BorderLayout.WEST);

        JLabel brand = new JLabel("HealthyCalories", SwingConstants.CENTER);
        brand.setFont(new Font("Tahoma", Font.PLAIN, 28));
        brand.setForeground(Color.WHITE);
        menuBar.add(brand, BorderLayout.CENTER);
        appContent.add(menuBar, BorderLayout.NORTH);

        sideMenu = new SideMenu(this);
        appContent.add(cardPanel, BorderLayout.CENTER);

        menuOverlay.setOpaque(false);
        dimmer.setOpaque(false);
        dimmer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dimmer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                setMenuOpen(false);
            }
        });
        menuOverlay.add(dimmer);
        menuOverlay.add(sideMenu);
        menuOverlay.setVisible(false);

        rootPane.add(appContent, JLayeredPane.DEFAULT_LAYER);
        rootPane.add(menuOverlay, JLayeredPane.PALETTE_LAYER);
        rootPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutLayers();
            }
        });
        setContentPane(rootPane);
        layoutLayers();

        registerScreen(SPLASH,     new SplashScreen(app, this));
        registerScreen(LOGIN,      new LoginScreen(app, this));
        registerScreen(REGISTER,   new RegisterScreen(app, this));
        registerScreen(ONBOARDING, new OnboardingScreen(app, this));
        registerScreen(HOME,       new HomeScreen(app, this));
        registerScreen(DIARY,      new DiaryScreen(app, this));
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
        // เลือกหน้าจอจากชื่อที่ลงทะเบียน แล้วสั่งให้หน้านั้นโหลดและวาดข้อมูลใหม่
        BaseScreen screen = screens.get(name);

        if (screen == null) {
            showDailog("ไม่พบหน้าจอ " + name);
            return;
        }
        if (!name.equals(currentScreen)) {
            previousScreen = currentScreen;
            currentScreen = name;
        }
        sideMenu.refreshAuthenticationState();
        if (screens.get(HOME) instanceof HomeScreen) {
            ((HomeScreen) screens.get(HOME)).showHomeView();
        }
        setMenuOpen(false);
        boolean showHomeHeader = HOME.equals(name);
        menuBar.setVisible(showHomeHeader);
        menuToggle.setVisible(showHomeHeader);
        cardLayout.show(cardPanel, name);
        appContent.doLayout();
        cardPanel.doLayout();
        screen.refresh();
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public HealthyCalories getApp() {
        return app;
    }

    public void startOnboarding() {
        BaseScreen onboarding = screens.get(ONBOARDING);
        if (onboarding instanceof OnboardingScreen) {
            ((OnboardingScreen) onboarding).beginNewSetup();
        }
        showScreen(ONBOARDING);
    }

    public void editProfileAndGoal() {
        BaseScreen onboarding = screens.get(ONBOARDING);
        if (!(onboarding instanceof OnboardingScreen)) {
            showDailog("ไม่พบหน้ากรอกข้อมูลสมาชิก");
            return;
        }
        ((OnboardingScreen) onboarding).beginProfileEdit(app.getCurrentMember());
        showScreen(ONBOARDING);
    }

    public void setHomeHeaderVisible(boolean visible) {
        menuBar.setVisible(visible);
        appContent.revalidate();
        appContent.repaint();
    }

    public String getPreviousScreen() {
        return previousScreen;
    }

    private void toggleMenu() {
        setMenuOpen(!menuOpen);
    }

    public void closeMenu() {
        setMenuOpen(false);
    }

    public void openMenu() {
        setMenuOpen(true);
    }

    private void setMenuOpen(boolean open) {
        menuOpen = open;
        menuOverlay.setVisible(open);
        menuOverlay.repaint();
    }

    private void layoutLayers() {
        int width = rootPane.getWidth();
        int height = rootPane.getHeight();
        appContent.setBounds(0, 0, width, height);
        menuOverlay.setBounds(0, 0, width, height);
        int drawerWidth = Math.min(width, Math.max(240, Math.min(360, (int) (width * 0.58))));
        dimmer.setBounds(drawerWidth, 0, width - drawerWidth, height);
        sideMenu.setBounds(0, 0, drawerWidth, height);
    }

    public void showDailog(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void showFoodCategories() {
        BaseScreen foodScreen = screens.get(FOOD);
        if (foodScreen instanceof FoodSearchScreen) {
            ((FoodSearchScreen) foodScreen).openCategoryFromHome();
        } else {
            showScreen(FOOD);
        }
    }

    public void showFoodCategories(String mealName) {
        BaseScreen foodScreen = screens.get(FOOD);
        if (foodScreen instanceof FoodSearchScreen) {
            ((FoodSearchScreen) foodScreen).openCategoryFromHome(mealName);
        } else {
            showScreen(FOOD);
        }
    }

    private static class MenuToggleButton extends JButton {
        MenuToggleButton() {
            setPreferredSize(new Dimension(72, 80));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND));
            g.drawLine(20, 34, 62, 34);
            g.drawLine(20, 49, 62, 49);
            g.dispose();
        }
    }
}