package main.java.healthycalories.app;
import java.util.ArrayList;
import main.java.healthycalories.model.user.*;
import main.java.healthycalories.persistence.*;

public class HealthyCalories {
    private String appName;
    private int countMember;
    private Member currentMember;
    protected ArrayList<Member> allMember;
    private FoodRepository foodRepo;
    private CsvManager csv;
    private PhotoManager photo;

    public  HealthyCalories(String appName){

    }
    public String getAppName(){
        return null;
    }
    public void setAppName(String appName){

    }
    public int getCountMember(){
        return 0;
    }
    public boolean register(String email,String password , String confirm){
        return false;
    }
    public boolean login(String email, String password){
        return false;
    }
    public void logout(){

    }
    public boolean resetPassword(String email,String newPassword){
        return false;
    }
    public Member getCurrentMember(){
        return false;
    }
    public void completeOnboarding (Member member){

    }
    public void saveWeight(String data,String weight){

    }
    public void addFoodToDiary(date , food , meal ,servings){

    }
    public void addMember(){

    }
    public void deleteMember(){

    }
    public void showAllMember(){

    }
    public void  editAppName(){

    }
    public void loadData(){

    }
    public void saveData(){

    }

}
