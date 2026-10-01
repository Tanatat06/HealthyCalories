package main.java.healthycalories.model.user;
import java.util.ArrayList;

import main.java.healthycalories.model.contract.*;
import main.java.healthycalories.model.enumeration.*;
import main.java.healthycalories.model.goal.*;
import main.java.healthycalories.model.log.DailyLog;
public class Member extends Person implements CsvConvertible,CalorieTracker {
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

    public Member(){
        super("", "");
        
    }
    public Member(email,password,gender,age,height,weight){

    }
    public double calBMR(){

    }
    public double calBMI(){

    }
    public double calDailyCalorie(){

    }
    public int calDaysToGoal(){

    }
    public boolean isCalorieSafe(){

    }
    public void updateProfile(age,height,weight,targerWeight){

    }
    public void setWeeklyGoal(WeeklyGoal goal){

    }
    public double getDailyCalorie (){

    }
    public double setDailyCalorie(double dailyCalorie){

    }
    public double getRemainingCalorie(String date){

    }
    public double getProgressPercent(){

    }
    public double getWeightLost(){

    }
    public int getDaysUsed(){

    }
    public void addDailyLog(DailyLog log){

    }
    public DailyLog getOrCreateDailyLog (String data){

    }
    public ArrayList<DailyLog> getWeightHistory(){

    }
    public ArrayList<String> getLoggedDates(int month , int year){

    }
    public double calculate(){

    }
    public void showSummary(){

    }
    public void information(){

    }
    public String[] toCsvRow(){
        return new String[]{

        }

    }
    public void fromCsvRow(String row){
        
    }


   
}