package main.java.healthycalories.model.user;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import main.java.healthycalories.model.contract.*;
import main.java.healthycalories.model.enumeration.*;
import main.java.healthycalories.model.goal.*;
import main.java.healthycalories.model.log.DailyLog;
import main.java.healthycalories.model.log.FoodLog;
import java.util.Objects;

/** สมาชิกของแอปที่คำนวณเป้าหมายแคลอรี่และเก็บประวัติบันทึกประจำวัน */
public class Member extends Person implements CsvConvertible, CalorieTracker {
    private static final double KCAL_PER_KILOGRAM = 7700;
    private static final double DAILY_ACTIVITY_FACTOR = 1.2;

    private String memberID;
    private Gender gender;
    private int age;
    private double height;
    private double weight;
    private double startWeight;
    private double targerWeight;
    private WeeklyGoal weeklyGoal;
    private double dailyCalorie;
    private boolean dailyCalorieCustomized;
    private String startDate;
    private ArrayList<DailyLog> allDailyLog;

    /** สร้างสมาชิกเปล่าสำหรับรับข้อมูลจากแหล่งจัดเก็บ */
    public Member() {
        super("", "");
        this.allDailyLog = new ArrayList<>();
    }

    /** สร้างสมาชิกพร้อมข้อมูลส่วนบุคคลเริ่มต้นและค่าน้ำหนักตั้งต้น */
    public Member(String email, String password, Gender gender, int age, double height, double weight) {
        super(email, password);
        this.gender = gender;
        updateProfile(age, height, weight, weight);
        this.startWeight = weight;
        this.allDailyLog = new ArrayList<>();
    }

    /** คำนวณอัตราการเผาผลาญพื้นฐานจากข้อมูลร่างกายและสูตรตามเพศ */
    public double calBMR() {
        return 10 * weight + 6.25 * height - 5 * age
                + (gender == Gender.MALE ? 5 : -161);
    }

    /** คำนวณดัชนีมวลกาย โดยคืนศูนย์เมื่อส่วนสูงยังไม่ถูกกำหนด */
    public double calBMI() {
        if (height <= 0) {
            return 0;
        }
        double heightInMeters = height / 100;
        return weight / (heightInMeters * heightInMeters);
    }

    /** จัดระดับ BMI เป็นข้อความภาษาไทย */
    public String getBmiStatus() {
        double bmi = calBMI();
        if (bmi < 18.5) {
            return "ต่ำกว่าเกณฑ์";
        }
        if (bmi < 25) {
            return "มาตรฐาน";
        }
        if (bmi < 30) {
            return "เกินเกณฑ์";
        }
        return "อ้วน";
    }

    /** คืนเป้าหมายแคลอรี่รายวัน; หากไม่ได้กำหนดเองจะคำนวณโดยคุมขั้นต่ำที่ปลอดภัย */
    public double calDailyCalorie() {
        if (dailyCalorieCustomized) {
            return dailyCalorie;
        }
        dailyCalorie = Math.max(minimumSafeCalories(), requestedDailyCalories());
        return dailyCalorie;
    }

    /** ประมาณจำนวนวันจนถึงเป้าหมายน้ำหนักจากส่วนต่างพลังงานรายวัน */
    public int calDaysToGoal() {
        double weightDifference = Math.abs(targerWeight - weight);
        if (weightDifference < 0.01) {
            return 0;
        }

        double dailyDifference = Math.abs(calBMR() * DAILY_ACTIVITY_FACTOR - calDailyCalorie());
        if (dailyDifference <= 0) {
            return 0;
        }
        return (int) Math.ceil(weightDifference * KCAL_PER_KILOGRAM / dailyDifference);
    }

    /** ตรวจว่าเป้าหมายแคลอรี่ปัจจุบันไม่ต่ำกว่าขั้นต่ำที่กำหนดตามเพศ */
    public boolean isCalorieSafe() {
        if (dailyCalorieCustomized) {
            return dailyCalorie >= minimumSafeCalories();
        }
        return requestedDailyCalories() >= minimumSafeCalories();
    }

    /** คำนวณแคลอรี่รายวันตามพลังงานรักษาน้ำหนักและเป้าหมายรายสัปดาห์ */
    private double requestedDailyCalories() {
        double maintenanceCalories = calBMR() * DAILY_ACTIVITY_FACTOR;
        double weightDifference = targerWeight - weight;
        double calorieChange = weeklyGoal == null
                ? 0
                : weeklyGoal.getKgPerWeek() * KCAL_PER_KILOGRAM / 7;

        if (Math.abs(weightDifference) < 0.01) {
            return maintenanceCalories;
        }
        return maintenanceCalories + Math.signum(weightDifference) * calorieChange;
    }

    /** คืนค่าขั้นต่ำของแคลอรี่รายวันตามเพศที่เลือก */
    private double minimumSafeCalories() {
        return gender == Gender.MALE ? 1500 : 1200;
    }

    /** ปรับข้อมูลร่างกายพร้อมตรวจช่วงค่าอายุและค่าตัวเลขทั้งหมด */
    public void updateProfile(int age, double height, double weight, double targetWeight) {
        if (age < 0 || age > 130 || !Double.isFinite(height) || height < 0
                || !Double.isFinite(weight) || weight < 0
                || !Double.isFinite(targetWeight) || targetWeight < 0) {
            throw new IllegalArgumentException("Profile values must be finite and non-negative");
        }
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.targerWeight = targetWeight;
    }

    public String getMemberID() {
        return memberID;
    }

    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    /** กำหนดอายุโดยจำกัดค่าที่รองรับ */
    public void setAge(int age) {
        if (age < 0 || age > 130) {
            throw new IllegalArgumentException("Age must be between 0 and 130");
        }
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    /** กำหนดส่วนสูงโดยปฏิเสธค่าที่ไม่จำกัดหรือติดลบ */
    public void setHeight(double height) {
        if (!Double.isFinite(height) || height < 0) {
            throw new IllegalArgumentException("Height must be finite and non-negative");
        }
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    /** กำหนดน้ำหนักปัจจุบันโดยปฏิเสธค่าที่ไม่จำกัดหรือติดลบ */
    public void setWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0) {
            throw new IllegalArgumentException("Weight must be finite and non-negative");
        }
        this.weight = weight;
    }

    public double getStartWeight() {
        return startWeight;
    }

    /** กำหนดน้ำหนักเริ่มต้นโดยตรวจสอบว่าเป็นค่าจำกัดที่ไม่ติดลบ */
    public void setStartWeight(double startWeight) {
        if (!Double.isFinite(startWeight) || startWeight < 0) {
            throw new IllegalArgumentException("Start weight must be finite and non-negative");
        }
        this.startWeight = startWeight;
    }

    public double getTargetWeight() {
        return targerWeight;
    }

    /** กำหนดน้ำหนักเป้าหมายโดยตรวจสอบว่าเป็นค่าจำกัดที่ไม่ติดลบ */
    public void setTargetWeight(double targetWeight) {
        if (!Double.isFinite(targetWeight) || targetWeight < 0) {
            throw new IllegalArgumentException("Target weight must be finite and non-negative");
        }
        this.targerWeight = targetWeight;
    }

    public WeeklyGoal getWeeklyGoal() {
        return weeklyGoal;
    }

    /** กำหนดเป้าหมายการเปลี่ยนแปลงน้ำหนักรายสัปดาห์ */
    public void setWeeklyGoal(WeeklyGoal goal) {
        this.weeklyGoal = goal;
    }

    public String getStartDate() {
        return startDate;
    }

    /** กำหนดวันที่เริ่มแผน */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public double getDailyCalorie() {
        return dailyCalorie;
    }

    /** กำหนดเป้าหมายแคลอรี่ที่ผู้ใช้ตั้งเอง พร้อมตรวจช่วงความปลอดภัย */
    public void setDailyCalorie(double dailyCalorie) {
        if (!Double.isFinite(dailyCalorie) || dailyCalorie < minimumSafeCalories()
                || dailyCalorie > 10000) {
            throw new IllegalArgumentException("Daily calories must be between "
                    + (int) minimumSafeCalories() + " and 10000 kcal");
        }
        this.dailyCalorie = dailyCalorie;
        this.dailyCalorieCustomized = true;
    }

    /** ล้างค่าเป้าหมายแคลอรี่ที่ผู้ใช้ตั้งเอง เพื่อให้กลับไปคำนวณอัตโนมัติ */
    public void clearDailyCalorieOverride() {
        dailyCalorieCustomized = false;
        dailyCalorie = 0;
    }

    /** คำนวณแคลอรี่คงเหลือของวันที่ระบุ หรือคืนเป้าหมายเมื่อยังไม่มีบันทึกวันนั้น */
    public double getRemainingCalorie(String date) {
        for (DailyLog log : allDailyLog) {
            if (log != null && date != null && date.equals(log.getData())) {
                return log.getRemainingCalories(dailyCalorie);
            }
        }
        return dailyCalorie;
    }

    /** คำนวณความคืบหน้าไปสู่น้ำหนักเป้าหมายและจำกัดผลลัพธ์ไว้ระหว่าง 0–100 */
    public double getProgressPercent() {
        double totalChange = startWeight - targerWeight;
        if (Math.abs(totalChange) < 0.01) {
            return Math.abs(weight - targerWeight) < 0.01 ? 100 : 0;
        }
        double completedChange = startWeight - weight;
        return Math.max(0, Math.min(100, completedChange / totalChange * 100));
    }

    /** คืนผลต่างระหว่างน้ำหนักเริ่มต้นกับน้ำหนักปัจจุบัน */
    public double getWeightLost() {
        return startWeight - weight;
    }

    /** นับจำนวนวันที่มีบันทึกประจำวันไม่ซ้ำกัน */
    public int getDaysUsed() {
        Set<String> loggedDates = new HashSet<>();
        for (DailyLog log : allDailyLog) {
            if (log != null && log.getData() != null && !log.getData().trim().isEmpty()) {
                loggedDates.add(log.getData().trim());
            }
        }
        return loggedDates.size();
    }

    /** เพิ่มบันทึกประจำวันและผูกกับรหัสสมาชิกเมื่อมีรหัส */
    public void addDailyLog(DailyLog log) {
        if (log != null) {
            if (memberID != null && !memberID.trim().isEmpty()) {
                log.setMemberId(memberID);
            }
            allDailyLog.add(log);
        }
    }

    /** ค้นหาบันทึกของวันที่ระบุ หรือสร้างและเพิ่มบันทึกใหม่เมื่อไม่พบ */
    public DailyLog getOrCreateDailyLog(String data) {
        for (DailyLog log : allDailyLog) {
            if (log != null && Objects.equals(data, log.getData())) {
                return log;
            }
        }
        DailyLog log = new DailyLog(memberID, data);
        allDailyLog.add(log);
        return log;
    }

    /** คืนบันทึกที่มีข้อมูลน้ำหนักเป็นประวัติน้ำหนัก */
    public ArrayList<DailyLog> getWeightHistory() {
        ArrayList<DailyLog> history = new ArrayList<>();
        for (DailyLog log : allDailyLog) {
            if (log != null && log.hasWeight()) {
                history.add(log);
            }
        }
        return history;
    }

    /** สร้างสรุปรายวันพร้อมน้ำหนักที่บันทึกไว้ในวันนั้น (ถ้ามี) */
    public DailyLog summarizeDay(LocalDate date, Iterable<FoodLog> foodLogs) {
        if (date == null) {
            throw new IllegalArgumentException("Daily summary date must not be null");
        }
        double weight = 0;
        for (DailyLog log : allDailyLog) {
            if (log != null && date.toString().equals(log.getData()) && log.hasWeight()) {
                weight = log.getWeight();
                break;
            }
        }
        return DailyLog.summarize(memberID, date, weight, foodLogs);
    }

    /** คืนสำเนารายการบันทึกประจำวัน */
    public ArrayList<DailyLog> getDailyLogs() {
        return new ArrayList<>(allDailyLog);
    }

    /** คืนวันที่มีบันทึกในเดือนและปีที่ระบุ พร้อมตรวจข้อมูลวันที่เสียหาย */
    public ArrayList<String> getLoggedDates(int month, int year) {
        if (month < 1 || month > 12 || year < 1) {
            throw new IllegalArgumentException("Month and year must be valid calendar values");
        }
        ArrayList<String> dates = new ArrayList<>();
        for (DailyLog log : allDailyLog) {
            if (log == null || log.getData() == null) {
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(log.getData());
                if (date.getMonthValue() == month && date.getYear() == year
                        && !dates.contains(log.getData())) {
                    dates.add(log.getData());
                }
            } catch (DateTimeParseException e) {
                throw new IllegalStateException("Invalid daily log date: " + log.getData(), e);
            }
        }
        return dates;
    }

    /** คำนวณค่าเป้าหมายแคลอรี่ผ่านสัญญา Trackable */
    public double calculate() {
        return calDailyCalorie();
    }

    /** แสดงสรุปสมาชิก น้ำหนัก BMI, BMR และเป้าหมายแคลอรี่ */
    public void showSummary() {
        System.out.println("Member: " + getEmail());
        System.out.println("Weight: " + weight + " kg; target: " + targerWeight + " kg");
        System.out.println("BMI: " + calBMI() + "; BMR: " + calBMR());
        System.out.println("Daily calorie goal: " + calDailyCalorie() + " kcal");
    }

    @Override
    public void information() {
        super.information();
    }

    /** ส่งออกข้อมูลสมาชิกที่รองรับในรูปคอลัมน์ CSV */
    @Override
    public String[] toCsvRow() {
        return new String[] {
            memberID,
            gender != null ? gender.name() : "",
            Integer.toString(age),
            Double.toString(height),
            Double.toString(weight),
            Double.toString(targerWeight),
            startDate == null ? "" : startDate
        };
    }

    /** โหลดข้อมูลสมาชิกจากคอลัมน์ CSV และคำนวณค่าเริ่มต้นหลังตรวจรูปแบบ */
    @Override
    public void fromCsvRow(String[] cols) {
        if (cols == null || cols.length < 7) {
            throw new IllegalArgumentException("Member CSV row must contain 7 columns");
        }
        Gender parsedGender;
        int parsedAge;
        double parsedHeight;
        double parsedWeight;
        double parsedTargetWeight;
        try {
            parsedGender = cols[1] == null || cols[1].trim().isEmpty()
                    ? null : Gender.valueOf(cols[1].trim().toUpperCase(java.util.Locale.ROOT));
            parsedAge = Integer.parseInt(cols[2]);
            parsedHeight = Double.parseDouble(cols[3]);
            parsedWeight = Double.parseDouble(cols[4]);
            parsedTargetWeight = Double.parseDouble(cols[5]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid member CSV row", e);
        }
        updateProfile(parsedAge, parsedHeight, parsedWeight, parsedTargetWeight);
        this.memberID = cols[0];
        this.gender = parsedGender;
        this.startWeight = parsedWeight;
        this.startDate = cols[6];
        this.dailyCalorie = calDailyCalorie();
    }

    /** แยกแถวข้อความแบบ CSV อย่างง่ายก่อนส่งต่อให้ตัวแปลงคอลัมน์ */
    public void fromCsvRow(String row) {
        if (row == null) {
            throw new IllegalArgumentException("Member CSV row must not be null");
        }
        fromCsvRow(row.split(",", -1));
    }
}