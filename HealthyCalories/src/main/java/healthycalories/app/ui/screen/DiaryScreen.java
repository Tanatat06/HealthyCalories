package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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

        JLabel emptyMessage = new JLabel("ยังไม่มีรายการบันทึกในวันนี้", SwingConstants.CENTER);
        emptyMessage.setFont(HomeScreen.font(Font.PLAIN, 14));
        emptyMessage.setForeground(Color.BLACK);
        emptyMessage.setBounds(0, HomeScreen.s(350), HomeScreen.s(HomeScreen.BASE_W),
                HomeScreen.s(40));
        canvas.add(emptyMessage);

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
}
