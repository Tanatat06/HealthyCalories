package main.java.healthycalories.model.log;
import java.util.ArrayList;

import main.java.healthycalories.model.enumeration.MealType;

public class DailyLog {
    private String dailyLogId;
    private String memberId;
    private String data;
    private double weight;
    private ArrayList<FoodLog> allFoodLog;

    public DailyLog(String memberId, String data) {
        this.memberId = memberId;
        this.data = data;
        this.allFoodLog = new ArrayList<>();
    }

    public void addFoodLog(FoodLog log) {
        if (log != null) {
            allFoodLog.add(log);
        }
    }

    public void removeFoodLog(String loginId) {
        allFoodLog.removeIf(log -> log != null && loginId.equals(log.getFoodLogId()));
    }

    public void updateServings(String loginId, double servings) {
        for (FoodLog log : allFoodLog) {
            if (log != null && loginId.equals(log.getFoodLogId())) {
                log.setServings(servings);
                return;
            }
        }
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public boolean hasWeight() {
        return weight > 0;
    }

    public boolean isEmpty() {
        return allFoodLog.isEmpty();
    }

    public double getCaloisIn() {
        double total = 0;
        for (FoodLog log : allFoodLog) {
            if (log != null) {
                total += log.getTotalKcal();
            }
        }
        return total;
    }

    public double getMealCalories(MealType meal) {
        double total = 0;
        for (FoodLog log : allFoodLog) {
            if (log != null && log.getMealType() == meal) {
                total += log.getTotalKcal();
            }
        }
        return total;
    }

    public ArrayList<FoodLog> getFoodLogsByMeal(MealType meal) {
        ArrayList<FoodLog> result = new ArrayList<>();
        for (FoodLog log : allFoodLog) {
            if (log != null && log.getMealType() == meal) {
                result.add(log);
            }
        }
        return result;
    }

    public double getRemainingCalories(double goal) {
        return goal - getCaloisIn();
    }

    public void ShowLog() {
        System.out.println("DailyLog: " + dailyLogId + " " + allFoodLog.size());
    }

    public String[] toCsvRow() {
        return new String[] {
            dailyLogId,
            memberId,
            data,
            Double.toString(weight)
        };
    }

    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 4) {
            return;
        }
        this.dailyLogId = row[0];
        this.memberId = row[1];
        this.data = row[2];
        this.weight = Double.parseDouble(row[3]);
    }
}