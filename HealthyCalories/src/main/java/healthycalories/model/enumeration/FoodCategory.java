package main.java.healthycalories.model.enumeration;

public enum FoodCategory {
    PROTEIN("โปรตีน","ic_protien"),FAT("ไขมัน","ic_fat"),
    GRAIN("แป้ง ธัญพืข","ic_grain"),VEGETTABLE("ผัก","ic_vagettable")
    ,FRIUT("ผลไม้","ic_fruit"),SOUP("ซุป","ic_soup")
    ,DRINK("เครื่องดื่ม","ic_drink"),ONE_DISH("จ่านเดี่ยว","ic_onedish")
    ,BAKERY("เบเกอรี่","ic_bekery"),DESSERT("ขนม","ic_dessert"),
    ICE_CREAM("ไอศกรีม","ic_icecream"),SUSHI("ซูชิ","ic_sushi")
    ,FASTFOOD("ฟาสฟุ๊ดส์","ic_fastfood"),KFC("เคเอฟซี","ic)kfc")
    ,CUSTOM("จากผู้ใช้อื่น","ic_customer"),OTHER("อื่นๆ","ic_other");
    private final String thaiName;
    private final String iconName;

    FoodCategory(String thaiName , String iconNmae){
        this.thaiName = thaiName;
        this.iconName = iconNmae;
    }
    public String getThaiName(){
        return thaiName;
    }
    public String geticon() {
        return iconName;
    }

    
} 