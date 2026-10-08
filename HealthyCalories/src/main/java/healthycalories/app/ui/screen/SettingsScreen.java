package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class SettingsScreen extends BaseScreen {

    public SettingsScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void onEditProfileAndGoal() {
    }

    public void onChangeDailyCalories() {
    }

    public void showAbout() {
    }

    @Override
    public void buildUI() {
        showPlaceholder("ตั้งค่า");
    }
}