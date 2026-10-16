package main.java.healthycalories.persistence;

import java.util.ArrayList;

import main.java.healthycalories.model.item.FoodItem;

/** ที่เก็บรายการอาหารในหน่วยความจำสำหรับค้นหาและกรองข้อมูล */
public class FoodRepository {
    private ArrayList<FoodItem> allFood;

    /** สร้างที่เก็บพร้อมรายการอาหารว่าง */
    public FoodRepository() {
        this.allFood = new ArrayList<>();
    }

    /** คืนรายการอาหารที่ตรงกับคำค้น (ปัจจุบันส่งคืนรายการทั้งหมดที่เก็บไว้) */
    public ArrayList<FoodItem> searchFood(String keyword) {
        return allFood;
    }

    /** คืนรายการอาหารที่เก็บไว้สำหรับการเลือกตามหมวดหมู่ */
    public ArrayList<FoodItem> filterByCategory() {
        return allFood;
    }
}
