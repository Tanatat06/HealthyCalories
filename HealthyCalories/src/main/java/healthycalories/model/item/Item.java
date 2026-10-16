package main.java.healthycalories.model.item;

/** ข้อมูลพื้นฐานร่วมของรายการที่มีรหัส ชื่อ และพลังงานเป็นกิโลแคลอรี่ */
public abstract class Item {
    private String itemId;
    private String name;
    private double kcal;

    /** สร้างรายการพร้อมทำความสะอาดข้อความและตรวจสอบค่าพลังงาน */
    protected Item(String itemId, String name, double kcal) {
        this.itemId = clean(itemId);
        this.name = clean(name);
        setKcal(kcal);
    }

    /** ตัดช่องว่างหัวท้ายและแทนข้อความ null ด้วยค่าว่าง */
    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    /** คืนรหัสรายการ */
    public String getItemId() {
        return itemId;
    }

    /** ปรับรหัสรายการหลังตัดช่องว่างหัวท้าย */
    public void setItemId(String itemId) {
        this.itemId = clean(itemId);
    }

    /** คืนชื่อรายการ */
    public String getName() {
        return name;
    }

    /** ปรับชื่อรายการหลังตัดช่องว่างหัวท้าย */
    public void setName(String name) {
        this.name = clean(name);
    }

    /** คืนพลังงานต่อหน่วยรายการ */
    public double getKcal() {
        return kcal;
    }

    /** ตั้งค่าพลังงาน โดยปฏิเสธค่าที่ไม่จำกัดหรือค่าติดลบ */
    public void setKcal(double kcal) {
        if (!Double.isFinite(kcal) || kcal < 0) {
            throw new IllegalArgumentException("Calories must be a finite non-negative value");
        }
        this.kcal = kcal;
    }

    /** ระบุว่ารายการเป็นรายการกำหนดเองหรือไม่; คลาสฐานถือว่าไม่ใช่ */
    public boolean isCustom() {
        return false;
    }

    /** แสดงชื่อ รหัส และพลังงานของรายการทางเอาต์พุตมาตรฐาน */
    public void information() {
        System.out.println(name + " (" + itemId + "): " + kcal + " kcal");
    }
}