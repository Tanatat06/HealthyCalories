package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class HomeScreen extends BaseScreen {

    public HomeScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void showMealRing() {
    }

    public void changeDay() {
    }

    public void showSummaryCard() {
    }

    public void onSaveWeight() {
    }

    public void onAddFood() {
    }

    @Override
    public void buildUI() {
        showPlaceholder("หน้าหลัก");
    }
}