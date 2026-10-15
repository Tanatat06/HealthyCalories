package healthycalories.model.enumeration;

public enum FoodCategory {
    PROTEIN("โปรตีน", "ic_protein.jpg"),
    FAT("ไขมัน", "ic_fat.jpg"),
    GRAIN("แป้ง ธัญพืช", "ic_grain.jpg"),
    VEGETABLE("ผัก", "ic_vegetable.jpg"),
    FRUIT("ผลไม้", "ic_fruit.jpg"),
    SOUP("ซุป", "ic_soup.jpg"),
    DRINK("เครื่องดื่ม", "ic_drink.jpg"),
    ONE_DISH("จานเดียว", "ic_onedish.jpg"),
    BAKERY("เบเกอรี่", "ic_bakery.jpg"),
    DESSERT("ของหวาน", "ic_dessert.jpg"),
    ICE_CREAM("ไอศกรีม", "ic_icecream.jpg"),
    SUSHI("ซูชิ", "ic_sushi.jpg"),
    FASTFOOD("ฟาสต์ฟู้ด", "ic_kfc.jpg"),
    KFC("เคเอฟซี", "ic_kfc.jpg"),
    CUSTOM("จากผู้ใช้อื่น", "ic_customer.jpg"),
    OTHER("อื่นๆ", "ic_other.jpg");

    @Deprecated
    public static final FoodCategory VEGETTABLE = VEGETABLE;
    @Deprecated
    public static final FoodCategory FRIUT = FRUIT;

    private final String thaiName;
    private final String iconName;

    FoodCategory(String thaiName, String iconName) {
        this.thaiName = thaiName;
        this.iconName = iconName;
    }

    public String getThaiName() {
        return thaiName;
    }

    public String getIconName() {
        return iconName;
    }

    public String geticon() {
        return getIconName();
    }
}