package main.java.healthycalories.model.enumeration;

/** หมวดหมู่อาหาร พร้อมชื่อภาษาไทยและชื่อไฟล์ไอคอนที่ใช้ในหน้าจอ */
public enum FoodCategory {
    PROTEIN("โปรตีน", "ic_protein.png"),
    FAT("ไขมัน", "ic_fat.png"),
    GRAIN("แป้ง ธัญพืช", "ic_grain.png"),
    VEGETABLE("ผัก", "ic_vegetable.png"),
    FRUIT("ผลไม้", "ic_fruit.png"),
    SOUP("ซุป", "ic_soup.png"),
    DRINK("เครื่องดื่ม", "ic_drink.png"),
    ONE_DISH("จานเดียว", "ic_onedish.png"),
    BAKERY("เบเกอรี่", "ic_bakery.png"),
    DESSERT("ของหวาน", "ic_dessert.png"),
    ICE_CREAM("ไอศกรีม", "ic_icecream.png"),
    SUSHI("ซูชิ", "ic_sushi.png"),
    FASTFOOD("ฟาสต์ฟู้ด", "ic_kfc.png"),
    KFC("เคเอฟซี", "ic_kfc.png"),
    CUSTOM("จากผู้ใช้อื่น", "ic_customer.png"),
    OTHER("อื่นๆ", "ic_other.png");

    @Deprecated
    public static final FoodCategory VEGETTABLE = VEGETABLE;
    @Deprecated
    public static final FoodCategory FRIUT = FRUIT;

    private final String thaiName;
    private final String iconName;

    /** เก็บชื่อที่แสดงและไอคอนประจำหมวดหมู่ */
    FoodCategory(String thaiName, String iconName) {
        this.thaiName = thaiName;
        this.iconName = iconName;
    }

    /** คืนชื่อหมวดหมู่ภาษาไทยสำหรับแสดงผล */
    public String getThaiName() {
        return thaiName;
    }

    /** คืนชื่อไฟล์ไอคอนประจำหมวดหมู่ */
    public String getIconName() {
        return iconName;
    }

    /** คืนชื่อไฟล์ไอคอนผ่านชื่อเมธอดเดิมเพื่อรองรับผู้เรียกเก่า */
    public String geticon() {
        return getIconName();
    }
}