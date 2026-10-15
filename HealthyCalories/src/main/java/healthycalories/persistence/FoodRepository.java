package healthycalories.persistence;

import java.util.ArrayList;

import healthycalories.model.item.FoodItem;

public class FoodRepository {
    private ArrayList<FoodItem> allFood;

    public FoodRepository() {
        this.allFood = new ArrayList<>();
    }

    public ArrayList<FoodItem> searchFood(String keyword) {
        return allFood;
    }

    public ArrayList<FoodItem> filterByCategory() {
        return allFood;
    }
}
