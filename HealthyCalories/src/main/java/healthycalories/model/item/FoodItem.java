package main.java.healthycalories.model.item;

import java.util.Objects;
import main.java.healthycalories.model.contract.CsvConvertible;
import main.java.healthycalories.model.enumeration.FoodCategory;

/** รายการอาหารที่เพิ่มหมวดหมู่ หน่วยบริโภค และไอคอนให้ข้อมูลรายการพื้นฐาน */
public class FoodItem extends Item implements CsvConvertible {
    private FoodCategory category;
    private String servingUnit;
    private String iconName;

    /** สร้างรายการอาหารว่างเปล่าสำหรับการเติมข้อมูลภายหลัง */
    public FoodItem() {
        this("", "", 0, null, "", "");
    }

    /** สร้างรายการอาหารพร้อมข้อมูลหมวดหมู่และหน่วยบริโภค */
    public FoodItem(String itemId, String name, double kcal, FoodCategory category,
                    String servingUnit, String iconName) {
        super(itemId, name, kcal);
        this.category = category;
        this.servingUnit = clean(servingUnit);
        this.iconName = clean(iconName);
    }

    /** ทำความสะอาดข้อความและป้องกันค่าข้อความ null */
    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    /** คืนหมวดหมู่ของอาหาร */
    public FoodCategory getCategory() {
        return category;
    }

    /** กำหนดหมวดหมู่ของอาหาร */
    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    /** คืนชื่อเรียกหน่วยบริโภคแบบเดิม */
    public String getUnit() {
        return servingUnit;
    }

    /** คืนหน่วยบริโภคที่ใช้แสดงในส่วนปริมาณอาหาร */
    public String getServingUnit() {
        return servingUnit;
    }

    /** กำหนดหน่วยบริโภคหลังตัดช่องว่างหัวท้าย */
    public void setServingUnit(String servingUnit) {
        this.servingUnit = clean(servingUnit);
    }

    /** คืนชื่อไฟล์ไอคอนของอาหาร */
    public String getIconName() {
        return iconName;
    }

    /** กำหนดชื่อไอคอนหลังตัดช่องว่างหัวท้าย */
    public void setIconName(String iconName) {
        this.iconName = clean(iconName);
    }

    /** ระบุว่าอาหารนี้มาจากรายการที่ผู้ใช้กำหนดเองหรือไม่ */
    @Override
    public boolean isCustom() {
        return category == FoodCategory.CUSTOM;
    }

    /** แสดงข้อมูลพื้นฐานและรายละเอียดหมวดหมู่กับหน่วยบริโภค */
    @Override
    public void information() {
        super.information();
        System.out.println("Category: " + (category == null ? "" : category.getThaiName())
                + "; serving: " + servingUnit);
    }

    /** ส่งออกข้อมูลอาหารเป็นคอลัมน์ตามลำดับรูปแบบ CSV */
    @Override
    public String[] toCsvRow() {
        return new String[] {
            getItemId(),
            getName(),
            Double.toString(getKcal()),
            category == null ? "" : category.name(),
            servingUnit,
            iconName
        };
    }

    /** โหลดข้อมูลอาหารจากแถว CSV และตรวจสอบจำนวนคอลัมน์กับค่าพลังงาน */
    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 6) {
            throw new IllegalArgumentException("Food item CSV row must contain 6 columns");
        }
        double kcal;
        try {
            kcal = Double.parseDouble(row[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid food calorie value: " + row[2], e);
        }
        FoodCategory parsedCategory = parseCategory(row[3]);
        setItemId(row[0]);
        setName(row[1]);
        setKcal(kcal);
        setCategory(parsedCategory);
        setServingUnit(row[4]);
        setIconName(row[5]);
    }

    /** แปลงชื่อหมวดหมู่โดยรองรับชื่อสะกดผิดจากข้อมูลรุ่นเก่า */
    private static FoodCategory parseCategory(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String categoryName = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("VEGETTABLE".equals(categoryName)) {
            categoryName = "VEGETABLE";
        } else if ("FRIUT".equals(categoryName)) {
            categoryName = "FRUIT";
        }
        try {
            return FoodCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown food category: " + value, e);
        }
    }

    /** เปรียบเทียบรายการอาหารด้วยรหัสรายการ */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FoodItem)) return false;
        FoodItem foodItem = (FoodItem) other;
        return Objects.equals(getItemId(), foodItem.getItemId());
    }

    /** สร้างค่าแฮชจากรหัสรายการให้สอดคล้องกับ equals */
    @Override
    public int hashCode() {
        return Objects.hash(getItemId());
    }
}