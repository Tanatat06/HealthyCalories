package main.java.healthycalories.model.log;
import java.util.ArrayList;
import java.util.Objects;
import java.time.LocalDate;

import main.java.healthycalories.model.contract.CsvConvertible;
import main.java.healthycalories.model.enumeration.MealType;

/** บันทึกสรุปประจำวันของสมาชิก ทั้งวันที่ น้ำหนัก และรายการอาหารที่รับประทาน */
public class DailyLog implements CsvConvertible {
    private String dailyLogId;
    private String memberId;
    private String data;
    private double weight;
    private ArrayList<FoodLog> allFoodLog;

    /** สร้างบันทึกเปล่าที่ไม่มีสมาชิกหรือวันที่กำหนด */
    public DailyLog() {
        this("", "");
    }

    /** สร้างบันทึกประจำวันและเตรียมรายการอาหารว่าง */
    public DailyLog(String memberId, String data) {
        this.memberId = memberId;
        this.data = data;
        this.dailyLogId = createDailyLogId(memberId, data);
        this.allFoodLog = new ArrayList<>();
    }

    /** สร้างภาพสรุปจากรายการอาหารโดยคัดลอกรายการเพื่อไม่แชร์สถานะเดิม */
    public static DailyLog summarize(String memberId, LocalDate date, double weight,
            Iterable<FoodLog> foodLogs) {
        if (date == null) {
            throw new IllegalArgumentException("Daily log date must not be null");
        }
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        if (foodLogs == null) {
            throw new IllegalArgumentException("Food logs must not be null");
        }
        DailyLog summary = new DailyLog(memberId, date.toString());
        summary.setWeight(weight);
        for (FoodLog foodLog : foodLogs) {
            if (foodLog != null) {
                FoodLog copy = new FoodLog(foodLog.getDailyLogId(), foodLog.getFood(),
                        foodLog.getMealType(), foodLog.getServings());
                summary.addFoodLog(copy);
            }
        }
        return summary;
    }

    /** สร้างรหัสบันทึกจากสมาชิกและวันที่ โดยละส่วนสมาชิกเมื่อไม่มีค่า */
    private static String createDailyLogId(String memberId, String data) {
        String member = memberId == null ? "" : memberId.trim();
        String date = data == null ? "" : data.trim();
        return member.isEmpty() ? date : member + "-" + date;
    }

    /** คืนรหัสบันทึกประจำวันที่สร้างจากสมาชิกและวันที่ */
    public String getDailyLogId() {
        return dailyLogId;
    }

    /** คืนรหัสสมาชิกของบันทึก */
    public String getMemberId() {
        return memberId;
    }

    /** กำหนดสมาชิกและสร้างรหัสบันทึกใหม่พร้อมปรับรหัสรายการอาหารที่ผูกอยู่ */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
        refreshDailyLogId();
    }

    /** กำหนดรหัสบันทึกและซิงโครไนซ์รหัสในรายการอาหารทั้งหมด */
    public void setDailyLogId(String dailyLogId) {
        this.dailyLogId = dailyLogId;
        synchronizeFoodLogIds();
    }

    /** เพิ่มรายการอาหารและผูกรหัสรายการเข้ากับบันทึกประจำวันนี้ */
    public void addFoodLog(FoodLog log) {
        if (log != null) {
            log.setDailyLogId(dailyLogId);
            allFoodLog.add(log);
        }
    }

    /** ลบรายการอาหารด้วยรหัสรายการอาหาร */
    public void removeFoodLog(String foodLogId) {
        allFoodLog.removeIf(log -> log != null && Objects.equals(foodLogId, log.getFoodLogId()));
    }

    /** ปรับจำนวนบริโภคของรายการที่ระบุ และคืน false หากไม่พบ */
    public boolean updateServings(String foodLogId, double servings) {
        for (FoodLog log : allFoodLog) {
            if (log != null && Objects.equals(foodLogId, log.getFoodLogId())) {
                log.setServings(servings);
                return true;
            }
        }
        return false;
    }

    /** กำหนดน้ำหนักที่บันทึก โดยรับเฉพาะค่าจำกัดที่ไม่ติดลบ */
    public void setWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        this.weight = weight;
    }

    /** คืนค่าน้ำหนักที่บันทึกในวันนั้น */
    public double getWeight() {
        return weight;
    }

    /** คืนวันที่ในรูปข้อความตามรูปแบบที่ใช้ในบันทึก */
    public String getData() {
        return data;
    }

    /** กำหนดวันที่และสร้างรหัสบันทึกใหม่พร้อมปรับรหัสรายการอาหาร */
    public void setData(String data) {
        this.data = data;
        refreshDailyLogId();
    }

    /** ปรับรหัสบันทึกเมื่อสมาชิกหรือวันที่เปลี่ยนไป */
    private void refreshDailyLogId() {
        dailyLogId = createDailyLogId(memberId, data);
        synchronizeFoodLogIds();
    }

    /** อัปเดตรหัสบันทึกประจำวันในรายการอาหารที่ผูกอยู่ทั้งหมด */
    private void synchronizeFoodLogIds() {
        for (FoodLog log : allFoodLog) {
            if (log != null) {
                log.setDailyLogId(dailyLogId);
            }
        }
    }

    /** ระบุว่ามีการบันทึกน้ำหนักที่มากกว่าศูนย์หรือไม่ */
    public boolean hasWeight() {
        return weight > 0;
    }

    /** ระบุว่าไม่มีรายการอาหารอยู่ในบันทึก */
    public boolean isEmpty() {
        return allFoodLog.isEmpty();
    }

    /** รวมแคลอรี่จากรายการอาหารทั้งหมด */
    public double getCaloriesIn() {
        double total = 0;
        for (FoodLog log : allFoodLog) {
            if (log != null) {
                total += log.getTotalKcal();
            }
        }
        return total;
    }

    /** คืนผลรวมแคลอรี่ผ่านชื่อเมธอดเดิมเพื่อรองรับผู้เรียกเก่า */
    public double getCaloisIn() {
        return getCaloriesIn();
    }

    /** รวมแคลอรี่เฉพาะมื้อที่ระบุ */
    public double getMealCalories(MealType meal) {
        double total = 0;
        for (FoodLog log : allFoodLog) {
            if (log != null && log.getMealType() == meal) {
                total += log.getTotalKcal();
            }
        }
        return total;
    }

    /** คืนสำเนารายการอาหารของมื้อที่ระบุ */
    public ArrayList<FoodLog> getFoodLogsByMeal(MealType meal) {
        ArrayList<FoodLog> result = new ArrayList<>();
        for (FoodLog log : allFoodLog) {
            if (log != null && log.getMealType() == meal) {
                result.add(log);
            }
        }
        return result;
    }

    /** คืนสำเนารายการอาหารทั้งหมดเพื่อป้องกันการแก้คอลเลกชันภายในโดยตรง */
    public ArrayList<FoodLog> getFoodLogs() {
        return new ArrayList<>(allFoodLog);
    }

    /** คำนวณแคลอรี่คงเหลือจากเป้าหมายและยอดที่รับประทานแล้ว */
    public double getRemainingCalories(double goal) {
        return goal - getCaloriesIn();
    }

    /** แสดงรหัสบันทึกและจำนวนรายการอาหาร */
    public void ShowLog() {
        System.out.println("DailyLog: " + dailyLogId + " " + allFoodLog.size());
    }

    /** เรียกการแสดงสรุปผ่านชื่อเมธอดรูปแบบมาตรฐาน */
    public void showLog() {
        ShowLog();
    }

    /** ส่งออกรหัส สมาชิก วันที่ และน้ำหนักเป็นคอลัมน์ CSV */
    @Override
    public String[] toCsvRow() {
        return new String[] {
            dailyLogId,
            memberId,
            data,
            Double.toString(weight)
        };
    }

    /** โหลดสถานะจากคอลัมน์ CSV และตรวจสอบรูปแบบน้ำหนัก */
    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 4) {
            throw new IllegalArgumentException("Daily log CSV row must contain 4 columns");
        }
        double parsedWeight;
        try {
            parsedWeight = Double.parseDouble(row[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid daily log weight: " + row[3], e);
        }
        if (!Double.isFinite(parsedWeight) || parsedWeight < 0) {
            throw new IllegalArgumentException("Weight must be a finite non-negative value");
        }
        this.dailyLogId = row[0];
        this.memberId = row[1];
        this.data = row[2];
        this.weight = parsedWeight;
        this.allFoodLog.clear();
    }

    /** แยกแถวข้อความ CSV แบบง่ายก่อนส่งต่อให้ตัวแปลงคอลัมน์ */
    public void fromCsvRow(String csvRow) {
        if (csvRow == null) {
            throw new IllegalArgumentException("Daily log CSV row must not be null");
        }
        fromCsvRow(csvRow.split(",", -1));
    }
}