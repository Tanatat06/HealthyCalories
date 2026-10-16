package main.java.healthycalories.model.enumeration;

/** เพศที่เลือกใช้ในข้อมูลสมาชิก พร้อมป้ายชื่อภาษาไทย */
public enum Gender {
    MALE("ชาย"), FEMALE("หญิง"), OTHER("อื่นๆ");

    private final String thaiName;

    /** กำหนดข้อความภาษาไทยที่ใช้แสดงแทนเพศ */
    Gender(String thaiName) {
        this.thaiName = thaiName;
    }
    /** คืนชื่อเพศภาษาไทยสำหรับแสดงผล */
    public String getThaiName() {
        return thaiName;
    }
}