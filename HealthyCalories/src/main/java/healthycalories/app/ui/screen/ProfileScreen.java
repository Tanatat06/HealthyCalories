package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class ProfileScreen extends BaseScreen {

    public ProfileScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void showStats() {
    }

    @Override
    public void buildUI() {
        showPlaceholder("โปรไฟล์");
    }
}