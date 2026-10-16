package main.java.healthycalories.model.log;

import java.util.*;
import main.java.healthycalories.model.contract.CsvConvertible;
import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.item.FoodItem;

/** รายการอาหารที่ผูกกับมื้อและบันทึกประจำวัน พร้อมจำนวนหน่วยที่รับประทาน */
public class FoodLog implements CsvConvertible {
    private String foodLogId;
    private String dailyLogId;
    private String foodItemId;
    private FoodItem food;
    private MealType meal;
    private double servings;

    /** สร้างรายการอาหารเปล่าสำหรับใช้ระหว่างโหลดข้อมูล */
    public FoodLog() {
        this("", null, null, 0);
    }

    /** สร้างรายการอาหาร พร้อมกำหนดรหัสที่ไม่ซ้ำและจำนวนบริโภค */
    public FoodLog(String dailyLogId, FoodItem food, MealType meal, double servings) {
        this.dailyLogId = dailyLogId;
        this.food = food;
        this.foodItemId = food == null ? "" : food.getItemId();
        this.meal = meal;
        setServings(servings);
        this.foodLogId = createFoodLogId(dailyLogId, food);
    }

    /** สร้างรหัสรายการเฉพาะโดยรวมรหัสวัน รหัสอาหาร และ UUID */
    private static String createFoodLogId(String dailyLogId, FoodItem food) {
        return (dailyLogId == null ? "" : dailyLogId) + "-"
                + (food == null ? "food" : food.getItemId()) + "-" + UUID.randomUUID();
    }

    /** คืนรหัสเฉพาะของรายการอาหารที่บันทึก */
    public String getFoodLogId() {
        return foodLogId;
    }

    /** กำหนดรหัสรายการโดยปฏิเสธค่าว่าง */
    public void setFoodLogId(String foodLogId) {
        if (foodLogId == null || foodLogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Food log ID must not be empty");
        }
        this.foodLogId = foodLogId;
    }

    /** คืนรหัสบันทึกประจำวันที่รายการนี้สังกัด */
    public String getDailyLogId() {
        return dailyLogId;
    }

    /** ย้ายรายการอาหารนี้ไปผูกกับรหัสบันทึกประจำวัน */
    public void setDailyLogId(String dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    /** คืนรหัสอาหารแม้ยังไม่ได้โหลดออบเจ็กต์อาหาร */
    public String getFoodItemId() {
        return foodItemId;
    }

    /** คืนข้อมูลอาหารที่ผูกอยู่ */
    public FoodItem getFood() {
        return food;
    }

    /** คืนมื้ออาหารของรายการนี้ */
    public MealType getMealType() {
        return meal;
    }

    /** กำหนดอาหารและปรับรหัสอาหารให้ตรงกับออบเจ็กต์ใหม่ */
    public void setFood(FoodItem food) {
        this.food = food;
        this.foodItemId = food == null ? "" : food.getItemId();
        if (foodLogId == null || foodLogId.isEmpty()) {
            foodLogId = createFoodLogId(dailyLogId, food);
        }
    }

    /** กำหนดมื้ออาหารของรายการ */
    public void setMealType(MealType meal) {
        this.meal = meal;
    }

    /** กำหนดจำนวนบริโภค โดยยอมรับเฉพาะค่าจำกัดที่ไม่ติดลบ */
    public void setServings(double servings) {
        if (!Double.isFinite(servings) || servings < 0) {
            throw new IllegalArgumentException("Servings must be a finite non-negative value");
        }
        this.servings = servings;
    }

    /** คืนจำนวนหน่วยบริโภคที่บันทึก */
    public double getServings() {
        return servings;
    }

    /** คำนวณแคลอรี่รวม และแจ้งข้อผิดพลาดหากยังแก้รหัสอาหารไม่สำเร็จ */
    public double getTotalKcal() {
        if (food == null) {
            if (foodItemId != null && !foodItemId.isEmpty()) {
                throw new IllegalStateException("Food item has not been resolved: " + foodItemId);
            }
            return 0;
        }
        return food.getKcal() * servings;
    }

    /** ส่งออกรหัสรายการ รหัสวัน รหัสอาหาร มื้อ และจำนวนเป็น CSV */
    @Override
    public String[] toCsvRow() {
        return new String[] {
            foodLogId,
            dailyLogId,
            foodItemId,
            meal != null ? meal.name() : "",
            Double.toString(servings)
        };
    }

    /** โหลดข้อมูลรายการจากคอลัมน์ CSV โดยยังไม่ผูกออบเจ็กต์อาหาร */
    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Food log CSV row must contain 5 columns");
        }
        double parsedServings;
        try {
            parsedServings = Double.parseDouble(row[4]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid food servings: " + row[4], e);
        }
        MealType parsedMeal = row[3] == null || row[3].trim().isEmpty()
                ? null : MealType.fromValue(row[3]);
        if (!Double.isFinite(parsedServings) || parsedServings < 0) {
            throw new IllegalArgumentException("Servings must be a finite non-negative value");
        }
        String parsedFoodLogId = row[0];
        String parsedDailyLogId = row[1];
        String parsedFoodItemId = row[2] == null ? "" : row[2];
        this.foodLogId = parsedFoodLogId;
        this.dailyLogId = parsedDailyLogId;
        this.foodItemId = parsedFoodItemId;
        this.meal = parsedMeal;
        this.servings = parsedServings;
        this.food = null;
    }

    /** โหลดข้อมูล CSV พร้อมตรวจสอบรหัสอาหารกับรายการอาหารที่ resolve แล้ว */
    public void fromCsvRow(String[] row, FoodItem resolvedFood) {
        if (row == null || row.length < 5) {
            throw new IllegalArgumentException("Food log CSV row must contain 5 columns");
        }
        String rowFoodId = row[2] == null ? "" : row[2];
        if (resolvedFood == null || !Objects.equals(rowFoodId, resolvedFood.getItemId())) {
            throw new IllegalArgumentException("Food item does not match food log row");
        }
        fromCsvRow(row);
        this.food = resolvedFood;
    }
}