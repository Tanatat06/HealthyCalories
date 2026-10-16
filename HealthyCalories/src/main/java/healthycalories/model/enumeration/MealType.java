package main.java.healthycalories.model.enumeration;

/** มื้ออาหารที่สามารถจัดประเภทให้รายการอาหารได้ */
public enum MealType {
    BREAKFAST("อาหารเช้า"),
    LUNCH("อาหารเที่ยง"),
    DINNER("อาหารเย็น"),
    SNACK("อาหารว่าง");

    @Deprecated
    public static final MealType BREAFAST = BREAKFAST;

    private final String thaiName;
    /** กำหนดชื่อมื้ออาหารภาษาไทยสำหรับแสดงผล */
    MealType(String thaiName) {
        this.thaiName = thaiName;
    }

    /** คืนชื่อมื้ออาหารภาษาไทย */
    public String getThaiName() {
        return thaiName;
    }

    /** แปลงค่าที่จัดเก็บเป็นมื้ออาหาร และรองรับชื่อสะกดแบบเดิม */
    public static MealType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Meal type must not be null");
        }
        return "BREAFAST".equalsIgnoreCase(value.trim())
                ? BREAKFAST
                : MealType.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}