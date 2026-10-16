package main.java.healthycalories.model.contract;

/** สัญญาสำหรับข้อมูลที่ติดตามแคลอรี่รายวันและคำนวณแคลอรี่คงเหลือได้ */
public interface CalorieTracker extends Trackable{
    /** คืนเป้าหมายแคลอรี่ต่อวัน */
    public double getDailyCalorie();
    /** กำหนดเป้าหมายแคลอรี่ต่อวัน */
    public void setDailyCalorie(double dailyCalorie);
    /** คำนวณแคลอรี่ที่เหลือจากบันทึกของวันที่ระบุ */
    public double getRemainingCalorie(String date);
    
}