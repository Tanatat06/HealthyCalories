package main.java.healthycalories.model.enumeration;
public enum MealType {
    BREAFAST("อาหารเช้า"),LUNCH("อาหารเที่ยง")
    ,DINNER("อาหารเย็น"),SNACK("อ่าหารว่าง");
    
    private final String thaiName;
    MealType(String thaiName ){
    this.thaiName = thaiName;
    }
    public String getThaiName(){
        return thaiName;
    }
}