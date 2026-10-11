package main.java.healthycalories.persistence;

import java.util.ArrayList;

import main.java.healthycalories.model.item.FoodItem;

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
