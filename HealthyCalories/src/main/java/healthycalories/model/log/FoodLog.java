package main.java.healthycalories.model.log;

import java.util.*;
import main.java.healthycalories.model.contract.CsvConvertible;
import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.item.FoodItem;

public class FoodLog implements CsvConvertible {
    private String foodLogId;
    private String dailyLogId;
    private String foodItemId;
    private FoodItem food;
    private MealType meal;
    private double servings;

    public FoodLog() {
        this("", null, null, 0);
    }

    public FoodLog(String dailyLogId, FoodItem food, MealType meal, double servings) {
        this.dailyLogId = dailyLogId;
        this.food = food;
        this.foodItemId = food == null ? "" : food.getItemId();
        this.meal = meal;
        setServings(servings);
        this.foodLogId = createFoodLogId(dailyLogId, food);
    }

    private static String createFoodLogId(String dailyLogId, FoodItem food) {
        return (dailyLogId == null ? "" : dailyLogId) + "-"
                + (food == null ? "food" : food.getItemId()) + "-" + UUID.randomUUID();
    }

    public String getFoodLogId() {
        return foodLogId;
    }

    public void setFoodLogId(String foodLogId) {
        if (foodLogId == null || foodLogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Food log ID must not be empty");
        }
        this.foodLogId = foodLogId;
    }

    public String getDailyLogId() {
        return dailyLogId;
    }

    public void setDailyLogId(String dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    public String getFoodItemId() {
        return foodItemId;
    }

    public FoodItem getFood() {
        return food;
    }

    public MealType getMealType() {
        return meal;
    }

    public void setFood(FoodItem food) {
        this.food = food;
        this.foodItemId = food == null ? "" : food.getItemId();
        if (foodLogId == null || foodLogId.isEmpty()) {
            foodLogId = createFoodLogId(dailyLogId, food);
        }
    }

    public void setMealType(MealType meal) {
        this.meal = meal;
    }

    public void setServings(double servings) {
        if (!Double.isFinite(servings) || servings < 0) {
            throw new IllegalArgumentException("Servings must be a finite non-negative value");
        }
        this.servings = servings;
    }

    public double getServings() {
        return servings;
    }

    public double getTotalKcal() {
        if (food == null) {
            if (foodItemId != null && !foodItemId.isEmpty()) {
                throw new IllegalStateException("Food item has not been resolved: " + foodItemId);
            }
            return 0;
        }
        return food.getKcal() * servings;
    }

    @Override
    public String[] toCsvRow() {
        return new String[] {
            foodLogId,
            dailyLogId,
            foodItemId,
            meal != null ? meal.name() : "",
            Double.toString(servings)
        };
    }

    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Food log CSV row must contain 5 columns");
        }
        double parsedServings;
        try {
            parsedServings = Double.parseDouble(row[4]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid food servings: " + row[4], e);
        }
        MealType parsedMeal = row[3] == null || row[3].trim().isEmpty()
                ? null : MealType.fromValue(row[3]);
        if (!Double.isFinite(parsedServings) || parsedServings < 0) {
            throw new IllegalArgumentException("Servings must be a finite non-negative value");
        }
        String parsedFoodLogId = row[0];
        String parsedDailyLogId = row[1];
        String parsedFoodItemId = row[2] == null ? "" : row[2];
        this.foodLogId = parsedFoodLogId;
        this.dailyLogId = parsedDailyLogId;
        this.foodItemId = parsedFoodItemId;
        this.meal = parsedMeal;
        this.servings = parsedServings;
        this.food = null;
    }

    public void fromCsvRow(String[] row, FoodItem resolvedFood) {
        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Food log CSV row must contain 5 columns");
        }
        String rowFoodId = row[2] == null ? "" : row[2];
        if (resolvedFood == null || !Objects.equals(rowFoodId, resolvedFood.getItemId())) {
            throw new IllegalArgumentException("Food item does not match food log row");
        }
        fromCsvRow(row);
        this.food = resolvedFood;
    }
}