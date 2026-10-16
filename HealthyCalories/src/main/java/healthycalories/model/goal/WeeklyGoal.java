package main.java.healthycalories.model.goal;

/** อัตราการเปลี่ยนแปลงน้ำหนักเป้าหมายต่อสัปดาห์ พร้อมชื่อที่แสดงแก่ผู้ใช้ */
public enum WeeklyGoal {
    VERY_FAST(1.0, "แบบรวดเร็วมาก"), 
    FAST(0.5, "แบบเร็ว"), 
    NORMAL(0.33, "แบบทำธรรมดา(แนะนำ)"), 
    SLOW(0.25, "แบบค่อยๆ");

   
    private final double kgPerWeek;
    private final String thaiName;

    
    /** กำหนดอัตรากิโลกรัมต่อสัปดาห์และข้อความภาษาไทยของเป้าหมาย */
    WeeklyGoal(double kgPerWeek, String thaiName) {
        this.kgPerWeek = kgPerWeek;
        this.thaiName = thaiName;
    }

  
    /** คืนอัตราน้ำหนักเป็นกิโลกรัมต่อสัปดาห์ */
    public double getKgPerWeek() {
        return kgPerWeek;
    }

    /** คืนชื่อเป้าหมายภาษาไทย */
    public String getThaiName() {
        return thaiName;
    }

    
    /** หาเป้าหมายที่มีอัตราตรงกับค่าที่ระบุอย่างแม่นยำ */
    public static WeeklyGoal fromValue(double kg) {
        if (!Double.isFinite(kg)) {
            throw new IllegalArgumentException("Weekly goal must be finite");
        }
        for (WeeklyGoal goal : values()) {
            if (Double.compare(goal.kgPerWeek, kg) == 0) {
                return goal;
            }
        }
        throw new IllegalArgumentException("Unknown weekly goal: " + kg);
    }
}