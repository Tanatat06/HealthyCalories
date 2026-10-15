package main.java.healthycalories.model.enumeration;
public enum MealType {
    BREAKFAST("อาหารเช้า"),
    LUNCH("อาหารเที่ยง"),
    DINNER("อาหารเย็น"),
    SNACK("อาหารว่าง");

    @Deprecated
    public static final MealType BREAFAST = BREAKFAST;

    private final String thaiName;
    MealType(String thaiName) {
        this.thaiName = thaiName;
    }

    public String getThaiName() {
        return thaiName;
    }

    public static MealType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Meal type must not be null");
        }
        return "BREAFAST".equalsIgnoreCase(value.trim())
                ? BREAKFAST
                : MealType.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}