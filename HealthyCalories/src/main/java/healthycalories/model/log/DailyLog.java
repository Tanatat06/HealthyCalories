package healthycalories.model.log;
import java.util.ArrayList;
import java.util.Objects;
import java.time.LocalDate;

import healthycalories.model.contract.CsvConvertible;
import healthycalories.model.enumeration.MealType;

public class DailyLog implements CsvConvertible {
    private String dailyLogId;
    private String memberId;
    private String data;
    private double weight;
    private ArrayList<FoodLog> allFoodLog;

    public DailyLog() {
        this("", "");
    }

    public DailyLog(String memberId, String data) {
        this.memberId = memberId;
        this.data = data;
        this.dailyLogId = createDailyLogId(memberId, data);
        this.allFoodLog = new ArrayList<>();
    }

    public static DailyLog summarize(String memberId, LocalDate date, double weight,
            Iterable<FoodLog> foodLogs) {
        if (date == null) {
            throw new IllegalArgumentException("Daily log date must not be null");
        }
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        if (foodLogs == null) {
            throw new IllegalArgumentException("Food logs must not be null");
        }
        DailyLog summary = new DailyLog(memberId, date.toString());
        summary.setWeight(weight);
        for (FoodLog foodLog : foodLogs) {
            if (foodLog != null) {
                FoodLog copy = new FoodLog(foodLog.getDailyLogId(), foodLog.getFood(),
                        foodLog.getMealType(), foodLog.getServings());
                summary.addFoodLog(copy);
            }
        }
        return summary;
    }

    private static String createDailyLogId(String memberId, String data) {
        String member = memberId == null ? "" : memberId.trim();
        String date = data == null ? "" : data.trim();
        return member.isEmpty() ? date : member + "-" + date;
    }

    public String getDailyLogId() {
        return dailyLogId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
        refreshDailyLogId();
    }

    public void setDailyLogId(String dailyLogId) {
        this.dailyLogId = dailyLogId;
        synchronizeFoodLogIds();
    }

    public void addFoodLog(FoodLog log) {
        if (log != null) {
            log.setDailyLogId(dailyLogId);
            allFoodLog.add(log);
        }
    }

    public void removeFoodLog(String foodLogId) {
        allFoodLog.removeIf(log -> log != null && Objects.equals(foodLogId, log.getFoodLogId()));
    }

    public boolean updateServings(String foodLogId, double servings) {
        for (FoodLog log : allFoodLog) {
            if (log != null && Objects.equals(foodLogId, log.getFoodLogId())) {
                log.setServings(servings);
                return true;
            }
        }
        return false;
    }

    public void setWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        refreshDailyLogId();
    }

    private void refreshDailyLogId() {
        dailyLogId = createDailyLogId(memberId, data);
        synchronizeFoodLogIds();
    }

    private void synchronizeFoodLogIds() {
        for (FoodLog log : allFoodLog) {
            if (log != null) {
                log.setDailyLogId(dailyLogId);
            }
        }
    }

    public boolean hasWeight() {
        return weight > 0;
    }

    public boolean isEmpty() {
        return allFoodLog.isEmpty();
    }

    public double getCaloriesIn() {
        double total = 0;
        for (FoodLog log : allFoodLog) {
            if (log != null) {
                total += log.getTotalKcal();
            }
        }
        return total;
    }

    public double getCaloisIn() {
        return getCaloriesIn();
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

    public ArrayList<FoodLog> getFoodLogs() {
        return new ArrayList<>(allFoodLog);
    }

    public double getRemainingCalories(double goal) {
        return goal - getCaloriesIn();
    }

    public void ShowLog() {
        System.out.println("DailyLog: " + dailyLogId + " " + allFoodLog.size());
    }

    public void showLog() {
        ShowLog();
    }

    @Override
    public String[] toCsvRow() {
        return new String[] {
            dailyLogId,
            memberId,
            data,
            Double.toString(weight)
        };
    }

    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 4) {
            throw new IllegalArgumentException("Daily log CSV row must contain 4 columns");
        }
        double parsedWeight;
        try {
            parsedWeight = Double.parseDouble(row[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid daily log weight: " + row[3], e);
        }
        if (!Double.isFinite(parsedWeight) || parsedWeight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        this.dailyLogId = row[0];
        this.memberId = row[1];
        this.data = row[2];
        this.weight = parsedWeight;
        this.allFoodLog.clear();
    }

    public void fromCsvRow(String csvRow) {
        if (csvRow == null) {
            throw new IllegalArgumentException("Daily log CSV row must not be null");
        }
        fromCsvRow(csvRow.split(",", -1));
    }
}