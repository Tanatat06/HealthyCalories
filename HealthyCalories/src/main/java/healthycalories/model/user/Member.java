package main.java.healthycalories.model.user;
import java.util.ArrayList;
import main.java.healthycalories.model.contract.*;
import main.java.healthycalories.model.enumeration.*;
import main.java.healthycalories.model.goal.*;
import main.java.healthycalories.model.log.DailyLog;

public class Member extends Person implements CsvConvertible, CalorieTracker {
    private static final double KCAL_PER_KILOGRAM = 7700;
    private static final double DAILY_ACTIVITY_FACTOR = 1.2;

    private String memberID;
    private Gender gender;
    private int age;
    private double height;
    private double weight;
    private double startWeight;
    private double targerWeight;
    private WeeklyGoal weeklyGoal;
    private double dailyCalorie;
    private String startDate;
    private ArrayList<DailyLog> allDailyLog;

    public Member() {
        super("", "");
        this.allDailyLog = new ArrayList<>();
    }

    public Member(String email, String password, Gender gender, int age, double height, double weight) {
        super(email, password);
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.startWeight = weight;
        this.targerWeight = weight;
        this.allDailyLog = new ArrayList<>();
    }

    public double calBMR() {
        return 10 * weight + 6.25 * height - 5 * age
                + (gender == Gender.MALE ? 5 : -161);
    }

    public double calBMI() {
        if (height <= 0) {
            return 0;
        }
        double heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    public double calDailyCalorie() {
        dailyCalorie = Math.max(minimumSafeCalories(), requestedDailyCalories());
        return dailyCalorie;
    }

    public int calDaysToGoal() {
        double weightDifference = Math.abs(targerWeight - weight);
        if (weightDifference < 0.01) {
            return 0;
        }

        double dailyDifference = Math.abs(calBMR() * DAILY_ACTIVITY_FACTOR - calDailyCalorie());
        if (dailyDifference <= 0) {
            return 0;
        }
        return (int) Math.ceil(weightDifference * KCAL_PER_KILOGRAM / dailyDifference);
    }

    public boolean isCalorieSafe() {
        return requestedDailyCalories() >= minimumSafeCalories();
    }

    private double requestedDailyCalories() {
        double maintenanceCalories = calBMR() * DAILY_ACTIVITY_FACTOR;
        double weightDifference = targerWeight - weight;
        double calorieChange = weeklyGoal == null
                ? 0
                : weeklyGoal.getKgPerWeek() * KCAL_PER_KILOGRAM / 7;

        if (Math.abs(weightDifference) < 0.01) {
            return maintenanceCalories;
        }
        return maintenanceCalories + Math.signum(weightDifference) * calorieChange;
    }

    private double minimumSafeCalories() {
        return gender == Gender.MALE ? 1500 : 1200;
    }

    public void updateProfile(int age, double height, double weight, double targetWeight) {
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.targerWeight = targetWeight;
    }

    public void setWeeklyGoal(WeeklyGoal goal) {
        this.weeklyGoal = goal;
    }

    public double getDailyCalorie() {
        return dailyCalorie;
    }

    public void setDailyCalorie(double dailyCalorie) {
        this.dailyCalorie = dailyCalorie;
    }

    public double getRemainingCalorie(String date) {
        return dailyCalorie;
    }

    public double getProgressPercent() {
        return 0;
    }

    public double getWeightLost() {
        return 0;
    }

    public int getDaysUsed() {
        return 0;
    }

    public void addDailyLog(DailyLog log) {
        if (log != null) {
            allDailyLog.add(log);
        }
    }

    public DailyLog getOrCreateDailyLog(String data) {
        for (DailyLog log : allDailyLog) {
            if (log != null && data.equals(log.toCsvRow()[2])) {
                return log;
            }
        }
        DailyLog log = new DailyLog(memberID, data);
        allDailyLog.add(log);
        return log;
    }

    public ArrayList<DailyLog> getWeightHistory() {
        return allDailyLog;
    }

    public ArrayList<String> getLoggedDates(int month, int year) {
        return new ArrayList<>();
    }

    public double calculate() {
        return calDailyCalorie();
    }

    public void showSummary() {
        System.out.println("Member summary");
    }

    @Override
    public void information() {
        super.information();
    }

    public String[] toCsvRow() {
        return new String[] {
            memberID,
            gender != null ? gender.name() : "",
            Integer.toString(age),
            Double.toString(height),
            Double.toString(weight),
            Double.toString(targerWeight),
            startDate == null ? "" : startDate
        };
    }

    public void fromCsvRow(String row) {
        if (row == null || row.isEmpty()) {
            return;
        }
        String[] cols = row.split(",");
        if (cols.length > 0) {
            this.memberID = cols[0];
        }
    }
}