package main.java.healthycalories.app.ui.screen;

import javax.swing.*;
import java.awt.*;
import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class OnboardingScreen extends BaseScreen {

    public OnboardingScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void chooseGender() {
    }

    public void submitBodyInfo() {
    }

    public void chooseGoal() {
    }

    public void showResult() {
    }

    public void next() {
        frame.showScreen(MainFrame.HOME);
    }

    public void back() {
        frame.showScreen(MainFrame.REGISTER);
    }

    @Override
    public void buildUI() {
        showPlaceholder("ตั้งค่าเริ่มต้น");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnBack = new JButton("ย้อนกลับ");
        JButton btnNext = new JButton("ถัดไป");
        btnBack.addActionListener(e -> back());
        btnNext.addActionListener(e -> next());
        buttons.add(btnBack);
        buttons.add(btnNext);

        add(buttons, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }
}