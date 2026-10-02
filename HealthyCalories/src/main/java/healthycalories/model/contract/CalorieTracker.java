package main.java.healthycalories.model.contract;
public interface CalorieTracker extends Trackable{
    public double getDailyCalorie();
    public void setDailyCalorie(double dailyCalorie);
    public double getRemainingCalorie(String date);
    
}