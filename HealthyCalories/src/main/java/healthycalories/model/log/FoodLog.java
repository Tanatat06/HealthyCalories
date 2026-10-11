package main.java.healthycalories.model.log;

import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.item.FoodItem;

public class FoodLog {
    private String foodLogId;
    private String dailyLogId;
    private FoodItem food;
    private MealType meal;
    private double servings;

    public FoodLog(String dailyLogId, FoodItem food, MealType meal, double servings) {
        this.dailyLogId = dailyLogId;
        this.food = food;
        this.meal = meal;
        this.servings = servings;
        this.foodLogId = dailyLogId + "-" + (food != null ? food.getItemId() : "food");
    }

    public String getFoodLogId() {
        return foodLogId;
    }

    public String getDailyLogId() {
        return dailyLogId;
    }

    public FoodItem getFood() {
        return food;
    }

    public MealType getMealType() {
        return meal;
    }

    public void setServings(double servings) {
        this.servings = servings;
    }

    public double getServings() {
        return servings;
    }

    public double getTotalKcal() {
        if (food == null) {
            return 0;
        }
        return food.getKcal() * servings;
    }

    public String[] toCsvRow() {
        return new String[] {
            foodLogId,
            dailyLogId,
            food != null ? food.getItemId() : "",
            meal != null ? meal.name() : "",
            Double.toString(servings)
        };
    }

    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 5) {
            return;
        }
        this.foodLogId = row[0];
        this.dailyLogId = row[1];
        this.meal = MealType.valueOf(row[3]);
        this.servings = Double.parseDouble(row[4]);
    }
}