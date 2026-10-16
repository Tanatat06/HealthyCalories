package main.java.healthycalories.model.contract;

/** สัญญาพื้นฐานสำหรับข้อมูลที่คำนวณค่าและแสดงสรุปได้ */
public interface Trackable {
    /** คำนวณค่าที่ติดตามตามข้อมูลของออบเจ็กต์ */
    public double calculate();
    /** แสดงสรุปข้อมูลที่ติดตาม */
    public void showSummary();
}