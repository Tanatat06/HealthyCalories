package main.java.healthycalories.app.ui.screen;

import main.java.healthycalories.app.HealthyCalories;
import main.java.healthycalories.app.ui.MainFrame;

public class FoodSearchScreen extends BaseScreen {

    public FoodSearchScreen(HealthyCalories app, MainFrame frame) {
        super(app, frame);
    }

    public void searchByKeyword() {
    }

    public void showCategories() {
    }

    public void showMyFoods() {
    }

    public void showRecent() {
    }

    public void selectFood() {
    }

    @Override
    public void buildUI() {
        showPlaceholder("ค้นหาอาหาร");
    }
}