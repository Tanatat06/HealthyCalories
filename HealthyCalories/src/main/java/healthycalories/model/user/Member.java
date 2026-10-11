package main.java.healthycalories.model.user;
import java.util.ArrayList;
import main.java.healthycalories.model.contract.*;
import main.java.healthycalories.model.enumeration.*;
import main.java.healthycalories.model.goal.*;
import main.java.healthycalories.model.log.DailyLog;

public class Member extends Person implements CsvConvertible, CalorieTracker {
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
        return 0;
    }

    public double calBMI() {
        return 0;
    }

    public double calDailyCalorie() {
        return dailyCalorie;
    }

    public int calDaysToGoal() {
        return 0;
    }

    public boolean isCalorieSafe() {
        return true;
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
        return dailyCalorie;
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