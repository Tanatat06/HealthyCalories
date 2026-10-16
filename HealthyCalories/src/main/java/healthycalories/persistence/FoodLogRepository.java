package main.java.healthycalories.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.item.FoodItem;
import main.java.healthycalories.model.log.FoodLog;

/** จัดเก็บบันทึกอาหารแยกตามบัญชีและให้บริการค้นหา/จัดหมวดหมู่อาหาร */
public class FoodLogRepository {
    private static final String FOOD_CATALOG_HEADER = "NUM_ID,";
    private static final String HEADER = "date,foodLogId,dailyLogId,foodItemId,foodName,"
            + "kcalPerServing,mealType,servings,servingUnit,totalKcal,memberEmail";

    private final File foodLogsFile;
    private final String memberEmail;
    private ArrayList<FoodItem> allFood;

    /** สร้างที่เก็บสำหรับผู้ใช้ที่ยังไม่ได้ระบุอีเมล */
    public FoodLogRepository() {
        this("");
    }

    /** สร้างที่เก็บบันทึกที่ผูกกับอีเมลสมาชิก */
    public FoodLogRepository(String memberEmail) {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "food_logs.csv"), memberEmail);
    }

    FoodLogRepository(File foodLogsFile) {
        this(foodLogsFile, "");
    }

    FoodLogRepository(File foodLogsFile, String memberEmail) {
        this.foodLogsFile = foodLogsFile;
        this.memberEmail = normalizeEmail(memberEmail);
    }

    /** ทำมาตรฐานอีเมลเพื่อใช้กรองเจ้าของข้อมูลในไฟล์ */
    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /** ป้องกันการอ่านหรือแก้ไขข้อมูลบันทึกโดยไม่มีบัญชีสมาชิก */
    private void requireMemberEmail() {
        if (memberEmail.isEmpty()) {
            throw new IllegalStateException("ต้องเข้าสู่ระบบก่อนจัดการบันทึกอาหาร");
        }
    }

    /** เตรียมย้ายรายการอาหารรุ่นเก่ามาใช้ไฟล์หลักที่แยกเจ้าของ */
    public void claimLegacyRecords() throws IOException {
        if (!memberEmail.isEmpty()) {
            migrateLegacyFileIfNeeded();
        }
    }

    /** บันทึกรายการอาหารพร้อมวันที่ หน่วยบริโภค และอีเมลเจ้าของ */
    public void save(FoodLog foodLog, LocalDate date, String servingUnit) throws IOException {
        requireMemberEmail();
        if (foodLog == null || foodLog.getFood() == null || foodLog.getMealType() == null) {
            throw new IllegalArgumentException("Food, meal, and food log are required");
        }
        if (date == null) {
            throw new IllegalArgumentException("Food log date is required");
        }
        if (servingUnit == null || servingUnit.trim().isEmpty()) {
            throw new IllegalArgumentException("Serving unit is required");
        }

        File parent = foodLogsFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์สำหรับ food_logs.csv ได้");
        }
        migrateLegacyFileIfNeeded();
        boolean writeHeader = !foodLogsFile.exists() || foodLogsFile.length() == 0;
        try (FileWriter writer = new FileWriter(foodLogsFile, true)) {
            if (writeHeader) {
                writer.write(HEADER);
                writer.write(System.lineSeparator());
            }
            FoodItem food = foodLog.getFood();
            String[] values = {
                date.toString(),
                foodLog.getFoodLogId(),
                foodLog.getDailyLogId(),
                foodLog.getFoodItemId(),
                food.getName(),
                Double.toString(food.getKcal()),
                foodLog.getMealType().name(),
                Double.toString(foodLog.getServings()),
                servingUnit.trim(),
                Double.toString(foodLog.getTotalKcal()),
                memberEmail
            };
            writer.write(toCsv(values));
            writer.write(System.lineSeparator());
        }
    }

    /** โหลดรายการบันทึกที่เป็นของบัญชีปัจจุบัน พร้อมตรวจสอบข้อมูลแต่ละแถว */
    public ArrayList<LoggedFood> loadAll() throws IOException {
        ArrayList<LoggedFood> logs = new ArrayList<>();
        if (memberEmail.isEmpty() || !foodLogsFile.exists() || foodLogsFile.length() == 0) {
            return logs;
        }
        claimLegacyRecords();
        try (Scanner reader = new Scanner(foodLogsFile)) {
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                lineNumber++;
                if (lineNumber == 1) {
                    line = line.replace("\uFEFF", "");
                    if (line.startsWith("date,")) {
                        continue;
                    }
                }
                String[] row = parseCsv(line);
                if (row.length != 10 && row.length != 11) {
                    throw new IOException("รูปแบบ food_logs.csv ไม่ถูกต้องที่บรรทัด "
                            + lineNumber);
                }
                String rowEmail = row.length == 11 ? normalizeEmail(row[10]) : "";
                if (!memberEmail.equals(rowEmail)) {
                    continue;
                }
                try {
                    LocalDate date = LocalDate.parse(row[0]);
                    double kcal = Double.parseDouble(row[5]);
                    FoodItem food = new FoodItem(row[3], row[4], kcal, null, row[8], "");
                    FoodLog foodLog = new FoodLog();
                    foodLog.fromCsvRow(new String[] {row[1], row[2], row[3], row[6], row[7]},
                            food);
                    double savedTotal = Double.parseDouble(row[9]);
                    if (!Double.isFinite(savedTotal)
                            || Math.abs(savedTotal - foodLog.getTotalKcal()) > 0.000001) {
                        throw new IllegalArgumentException("ยอดแคลอรี่รวมไม่ตรงกับข้อมูล");
                    }
                    logs.add(new LoggedFood(date, foodLog, row[8]));
                } catch (RuntimeException e) {
                    throw new IOException("ข้อมูล food_logs.csv ไม่ถูกต้องที่บรรทัด "
                            + lineNumber, e);
                }
            }
        }
        return logs;
    }

    /** นับจำนวนวันที่มีรายการอาหารบันทึกโดยไม่นับวันซ้ำ */
    public int getDaysUsed() throws IOException {
        Set<LocalDate> loggedDates = new HashSet<>();
        for (LoggedFood log : loadAll()) {
            loggedDates.add(log.getDate());
        }
        return loggedDates.size();
    }

    /** ตรวจว่ามีรหัสรายการอาหารที่บันทึกนี้อยู่แล้วหรือไม่ */
    public boolean containsFoodLogId(String foodLogId) throws IOException {
        if (foodLogId == null || foodLogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Food log ID is required");
        }
        for (LoggedFood loggedFood : loadAll()) {
            if (foodLogId.equals(loggedFood.getFoodLog().getFoodLogId())) {
                return true;
            }
        }
        return false;
    }

    /** กรองรายการอาหารของบัญชีตามมื้อที่ระบุ */
    public ArrayList<LoggedFood> loadByMeal(MealType meal) throws IOException {
        if (meal == null) {
            throw new IllegalArgumentException("Meal type is required");
        }
        ArrayList<LoggedFood> matches = new ArrayList<>();
        for (LoggedFood loggedFood : loadAll()) {
            if (loggedFood.getFoodLog().getMealType() == meal) {
                matches.add(loggedFood);
            }
        }
        return matches;
    }

    /** กรองรายการอาหารของบัญชีตามวันที่ระบุ */
    public ArrayList<LoggedFood> loadByDate(LocalDate date) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Food log date is required");
        }
        ArrayList<LoggedFood> matches = new ArrayList<>();
        for (LoggedFood loggedFood : loadAll()) {
            if (date.equals(loggedFood.getDate())) {
                matches.add(loggedFood);
            }
        }
        return matches;
    }

    /** รวมแคลอรี่ของรายการอาหารทั้งหมดในวันที่ระบุ */
    public double getTotalKcalForDate(LocalDate date) throws IOException {
        double total = 0;
        for (LoggedFood loggedFood : loadByDate(date)) {
            total += loggedFood.getFoodLog().getTotalKcal();
        }
        return total;
    }

    /** ลบบันทึกอาหารทั้งหมดของบัญชีปัจจุบัน */
    public void clearAll() throws IOException {
        requireMemberEmail();
        ArrayList<String> foodLogIds = new ArrayList<>();
        for (LoggedFood loggedFood : loadAll()) {
            foodLogIds.add(loggedFood.getFoodLog().getFoodLogId());
        }
        for (String foodLogId : foodLogIds) {
            delete(foodLogId);
        }
    }

    /** ย้ายข้อมูลบันทึกรูปแบบเดิมและผูกแถวที่ไม่มีเจ้าของเมื่อมีบัญชีเดียว */
    private void migrateLegacyFileIfNeeded() throws IOException {
        if (!foodLogsFile.isFile() || foodLogsFile.length() == 0) {
            return;
        }
        String legacyHeader = "date,foodLogId,dailyLogId,foodItemId,foodName,"
                + "kcalPerServing,mealType,servings,servingUnit,totalKcal";
        boolean assignUnownedRows = new MemberRepository()
                .isOnlyRegisteredAccount(memberEmail);
        ArrayList<String> migratedLines = new ArrayList<>();
        boolean legacyFormat;
        try (Scanner reader = new Scanner(foodLogsFile, "UTF-8")) {
            if (!reader.hasNextLine()) {
                return;
            }
            String header = reader.nextLine().replace("\uFEFF", "");
            legacyFormat = legacyHeader.equals(header);
            if (!HEADER.equals(header) && !legacyFormat) {
                throw new IOException("รูปแบบส่วนหัว food_logs.csv ไม่ถูกต้อง");
            }
            migratedLines.add(HEADER);
            int lineNumber = 1;
            boolean changed = legacyFormat;
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                lineNumber++;
                String[] row = parseCsv(line);
                if (row.length != (legacyFormat ? 10 : 11)) {
                    throw new IOException("รูปแบบ food_logs.csv ไม่ถูกต้องที่บรรทัด "
                            + lineNumber);
                }
                if (legacyFormat) {
                    row = java.util.Arrays.copyOf(row, 11);
                    row[10] = assignUnownedRows ? memberEmail : "";
                } else if (row[10].trim().isEmpty() && assignUnownedRows) {
                    row[10] = memberEmail;
                    changed = true;
                }
                migratedLines.add(toCsv(row));
            }
            if (!changed) {
                return;
            }
        }

        File parent = foodLogsFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile(foodLogsFile.getName(), ".tmp", parent);
        File backupFile = new File(foodLogsFile.getAbsolutePath() + ".bak");
        try {
            try (FileWriter writer = new FileWriter(temporaryFile, false)) {
                for (String line : migratedLines) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
            }
            if (backupFile.exists() && !backupFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์สำรอง food_logs.csv เดิมได้");
            }
            if (!foodLogsFile.renameTo(backupFile)) {
                throw new IOException("ไม่สามารถสำรองไฟล์ food_logs.csv ได้");
            }
            if (!temporaryFile.renameTo(foodLogsFile)) {
                if (!backupFile.renameTo(foodLogsFile)) {
                    throw new IOException("ไม่สามารถกู้คืน food_logs.csv หลังปรับปรุงได้");
                }
                throw new IOException("ไม่สามารถบันทึกไฟล์ food_logs.csv ที่ปรับปรุงแล้วได้");
            }
            if (!backupFile.delete()) {
                throw new IOException("ปรับปรุง food_logs.csv แล้ว แต่ลบไฟล์สำรองไม่สำเร็จ");
            }
        } finally {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath());
            }
        }
    }

    /** โหลดแค็ตตาล็อกอาหารครั้งแรกและคืนสำเนาเพื่อไม่เปิดเผยคอลเลกชันภายใน */
    public ArrayList<FoodItem> loadFoodCatalog() throws IOException {
        if (allFood == null) {
            allFood = readFoodCatalog();
        }
        return new ArrayList<>(allFood);
    }

    /** ค้นหาชื่ออาหารโดยไม่สนตัวพิมพ์ใหญ่เล็กและรองรับคำค้นว่าง */
    public ArrayList<FoodItem> searchFood(String keyword) throws IOException {
        String filter = keyword == null ? "" : keyword.trim().toLowerCase();
        ArrayList<FoodItem> matches = new ArrayList<>();
        for (FoodItem food : loadFoodCatalog()) {
            if (filter.isEmpty() || food.getName().toLowerCase().contains(filter)) {
                matches.add(food);
            }
        }
        return matches;
    }

    /** คืนแค็ตตาล็อกอาหารทั้งหมดสำหรับหน้าตัวเลือกหมวดหมู่ */
    public ArrayList<FoodItem> filterByCategory() throws IOException {
        return loadFoodCatalog();
    }

    /** เลือกอาหารตามหมวดหมู่ที่จับคู่จากคำสำคัญในชื่ออาหาร */
    public ArrayList<FoodItem> filterByCategory(String category) throws IOException {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Food category is required");
        }
        ArrayList<FoodItem> matches = new ArrayList<>();
        for (FoodItem food : loadFoodCatalog()) {
            if (matchesCategory(category.trim(), food.getName())) {
                matches.add(food);
            }
        }
        return matches;
    }

    /** ค้นหาไฟล์แค็ตตาล็อกอาหารจากตำแหน่งที่แอปรองรับ */
    private ArrayList<FoodItem> readFoodCatalog() throws IOException {
        File[] candidates = {
            new File("ProjectFolders" + File.separator + "csv" + File.separator + "foods.csv"),
            new File("csv" + File.separator + "foods.csv"),
            new File("src" + File.separator + "main" + File.separator + "resources"
                    + File.separator + "csv" + File.separator + "foods.csv")
        };
        for (File candidate : candidates) {
            if (candidate.isFile()) {
                return parseFoodCatalog(candidate);
            }
        }
        throw new IOException("ไม่พบไฟล์ foods.csv");
    }

    /** อ่านแค็ตตาล็อก CSV และแปลงแต่ละแถวเป็นรายการอาหาร */
    private static ArrayList<FoodItem> parseFoodCatalog(File file) throws IOException {
        ArrayList<FoodItem> foods = new ArrayList<>();
        try (Scanner reader = new Scanner(file, "UTF-8")) {
            boolean firstRow = true;
            int lineNumber = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                lineNumber++;
                if (firstRow) {
                    line = line.replace("\uFEFF", "");
                    firstRow = false;
                    if (line.toUpperCase().startsWith(FOOD_CATALOG_HEADER)) {
                        continue;
                    }
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 3 || columns[0].trim().isEmpty()
                        || columns[1].trim().isEmpty()) {
                    throw new IOException("ข้อมูลอาหารไม่ครบที่บรรทัด " + lineNumber);
                }
                try {
                    double calories = Double.parseDouble(columns[2].trim());
                    FoodItem food = new FoodItem(columns[0].trim(), columns[1].trim(),
                            calories, null, "", "");
                    foods.add(food);
                } catch (IllegalArgumentException e) {
                    throw new IOException("ค่าแคลอรี่ไม่ถูกต้องที่บรรทัด " + lineNumber, e);
                }
            }
        }
        return foods;
    }

    /** จับคู่ชื่ออาหารกับคำสำคัญของหมวดหมู่ที่หน้าจอใช้ */
    private static boolean matchesCategory(String category, String name) {
        String food = name.toLowerCase();
        switch (category) {
            case "โปรตีน":
                return containsAny(food, "ไก่", "หมู", "เนื้อ", "ปลา", "กุ้ง", "ปู",
                        "ปลาหมึก", "ไข่", "เต้าหู้", "ถั่ว", "นม", "ชีส", "แฮม",
                        "ไส้กรอก", "เบคอน", "ทูน่า", "แซลมอน");
            case "ไขมัน":
                return containsAny(food, "ทอด", "หมูกรอบ", "เบคอน", "ชีส", "เนย",
                        "กะทิ", "ครีม", "มันหมู", "เฟรนช์ฟราย", "ไก่ทอด");
            case "แป้ง ธัญพืช":
                return containsAny(food, "ข้าว", "เส้น", "ก๋วยเตี๋ยว", "บะหมี่",
                        "มาม่า", "ขนมจีน", "ขนมปัง", "แป้ง", "ข้าวโอ๊ต",
                        "ซีเรียล", "ธัญพืช", "นาน", "โรตี", "พาสต้า");
            case "ผัก":
                return containsAny(food, "ผัก", "สลัด", "คะน้า", "ผักบุ้ง",
                        "กะหล่ำ", "ถั่วงอก", "บรอกโคลี", "ตำลึง", "แตงกวา",
                        "มะเขือ", "ฟัก", "แครอท", "เห็ด", "ส้มตำ");
            case "ผลไม้":
                return containsAny(food, "ผลไม้", "กล้วย", "มะม่วง", "แอปเปิล",
                        "ส้ม", "องุ่น", "สับปะรด", "แตงโม", "มะพร้าว", "ทุเรียน",
                        "สตรอว์เบอร์รี", "เบอร์รี", "มะละกอ", "ฝรั่ง", "อะโวคาโด");
            case "ซุป":
                return containsAny(food, "ซุป", "ต้มยำ", "ต้มข่า", "ต้มจืด",
                        "แกงจืด", "ข้าวต้ม", "โจ๊ก", "แกงส้ม", "แกงป่า");
            case "เครื่องดื่ม":
                return containsAny(food, "น้ำเปล่า", "น้ำผล", "น้ำส้ม", "น้ำแอป",
                        "น้ำองุ่น", "น้ำสับปะรด", "น้ำแตงโม", "น้ำมะพร้าว",
                        "น้ำมะนาว", "น้ำกระเจี๊ยบ", "น้ำเก๊กฮวย", "น้ำอัญชัน",
                        "น้ำใบเตย", "น้ำตะไคร้", "น้ำขิง", "น้ำบ๊วย", "น้ำลำไย",
                        "น้ำมะตูม", "น้ำทับทิม", "น้ำแครอท", "น้ำมะเขือเทศ",
                        "นม", "ชา", "กาแฟ", "โกโก้", "สมูทตี", "โซดา", "ชานม",
                        "โยเกิร์ตดื่ม");
            case "กับข้าว":
                return !food.contains("ข้าว") && containsAny(food, "ผัด", "ยำ",
                        "ต้ม", "แกง", "ทอด", "ลาบ", "ส้มตำ", "น้ำพริก", "สลัด",
                        "ย่าง", "นึ่ง", "พล่า", "หลน");
            case "จานเดียว":
                return containsAny(food, "ข้าว", "ผัดไทย", "ก๋วยเตี๋ยว", "บะหมี่",
                        "ขนมจีน", "สุกี้", "พาสต้า", "ราเมน", "อุด้ง", "แซนด์วิช",
                        "เบอร์เกอร์", "พิซซ่า");
            case "เบเกอรี่":
                return containsAny(food, "ขนมปัง", "ครัวซองต์", "แพนเค้ก", "วาฟเฟิล",
                        "เฟรนช์โทสต์", "เค้ก", "พาย", "คุกกี้", "มัฟฟิน", "โดนัท",
                        "เบเกอรี่");
            case "ขนม":
                return containsAny(food, "ขนม", "ปอเปี๊ยะ", "เกี๊ยว", "ซาลาเปา",
                        "ลูกชิ้น", "ไส้กรอก", "หมูปิ้ง", "ไก่ปิ้ง", "สะเต๊ะ",
                        "เฟรนช์ฟราย", "ป๊อปคอร์น");
            case "ของหวาน":
                return containsAny(food, "ขนมหวาน", "เค้ก", "พาย", "คุกกี้",
                        "มัฟฟิน", "โดนัท", "พุดดิ้ง", "บิงซู", "ไอศกรีม",
                        "ข้าวเหนียวมะม่วง", "บัวลอย", "ลอดช่อง", "วุ้น", "เฉาก๊วย",
                        "ขนมไทย", "แพนเค้ก", "วาฟเฟิล");
            case "ไอศกรีม":
                return food.contains("ไอศกรีม") || food.contains("บิงซู");
            case "ซูชิ":
                return food.contains("ซูชิ");
            case "จากผู้ใช้อื่น":
                return false;
            case "KFC":
                return food.contains("kfc") || food.contains("เคเอฟซี");
            case "อื่นๆ":
                return !isInKnownCategory(food);
            default:
                return false;
        }
    }

    /** ตรวจว่าอาหารตรงกับหมวดหมู่ที่รู้จักอย่างน้อยหนึ่งประเภทหรือไม่ */
    private static boolean isInKnownCategory(String food) {
        String[] categories = {
            "โปรตีน", "ไขมัน", "แป้ง ธัญพืช", "ผัก", "ผลไม้", "ซุป",
            "เครื่องดื่ม", "กับข้าว", "จานเดียว", "เบเกอรี่", "ขนม",
            "ของหวาน", "ไอศกรีม", "ซูชิ", "KFC"
        };
        for (String category : categories) {
            if (matchesCategory(category, food)) {
                return true;
            }
        }
        return false;
    }

    /** ตรวจว่าข้อความมีคำใดคำหนึ่งในชุดคำสำคัญ */
    private static boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /** ลบบันทึกที่ตรงกับรหัส โดยเขียนไฟล์ฉบับปรับปรุงอย่างปลอดภัย */
    public boolean delete(String foodLogId) throws IOException {
        requireMemberEmail();
        if (foodLogId == null || foodLogId.trim().isEmpty()) {
            throw new IllegalArgumentException("Food log ID is required");
        }
        if (!foodLogsFile.isFile()) {
            return false;
        }

        File parent = foodLogsFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile(foodLogsFile.getName(), ".tmp", parent);
        File backupFile = new File(foodLogsFile.getAbsolutePath() + ".bak");
        boolean removed = false;
        try {
            try (Scanner reader = new Scanner(foodLogsFile);
                    FileWriter writer = new FileWriter(temporaryFile, false)) {
                int lineNumber = 0;
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    lineNumber++;
                    if (lineNumber == 1 && line.replace("\uFEFF", "").startsWith("date,")) {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                        continue;
                    }
                    String[] row = parseCsv(line);
                    if (row.length != 10 && row.length != 11) {
                        throw new IOException("รูปแบบ food_logs.csv ไม่ถูกต้องที่บรรทัด "
                                + lineNumber);
                    }
                    String rowEmail = row.length == 11 ? normalizeEmail(row[10]) : "";
                    if (row[1].equals(foodLogId) && memberEmail.equals(rowEmail)) {
                        if (removed) {
                            throw new IOException("พบรหัสรายการอาหารซ้ำใน food_logs.csv: "
                                    + foodLogId);
                        }
                        removed = true;
                    } else {
                        writer.write(line);
                        writer.write(System.lineSeparator());
                    }
                }
            }
            if (removed) {
                if (backupFile.exists() && !backupFile.delete()) {
                    throw new IOException("ไม่สามารถลบไฟล์สำรองเดิมได้");
                }
                if (!foodLogsFile.renameTo(backupFile)) {
                    throw new IOException("ไม่สามารถสำรองไฟล์ food_logs.csv ได้");
                }
                if (!temporaryFile.renameTo(foodLogsFile)) {
                    if (!backupFile.renameTo(foodLogsFile)) {
                        throw new IOException("ไม่สามารถกู้คืน food_logs.csv หลังลบไม่สำเร็จได้");
                    }
                    throw new IOException("ไม่สามารถบันทึกไฟล์ food_logs.csv ที่แก้ไขแล้วได้");
                }
                if (!backupFile.delete()) {
                    throw new IOException("ลบรายการแล้ว แต่ไม่สามารถลบไฟล์สำรองได้");
                }
            }
            return removed;
        } finally {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath());
            }
        }
    }

    /** แปลงค่าระเบียนอาหารเป็นแถว CSV ที่ escape อักขระพิเศษ */
    private static String toCsv(String[] values) {
        return CsvManager.formatLine(values);
    }

    /** แยกแถว CSV สำหรับไฟล์บันทึกและส่งต่อข้อผิดพลาดรูปแบบ */
    private static String[] parseCsv(String line) throws IOException {
        return CsvManager.parseLine(line).toArray(new String[0]);
    }

    /** ผลลัพธ์บันทึกอาหารที่รวมวันที่ หน่วยบริโภค และโมเดลรายการอาหาร */
    public static final class LoggedFood {
        private final LocalDate date;
        private final FoodLog foodLog;
        private final String servingUnit;

        /** เก็บวันที่ รายการอาหาร และหน่วยบริโภคที่โหลดมาจากไฟล์ */
        private LoggedFood(LocalDate date, FoodLog foodLog, String servingUnit) {
            this.date = date;
            this.foodLog = foodLog;
            this.servingUnit = servingUnit;
        }

        public LocalDate getDate() {
            return date;
        }

        public FoodLog getFoodLog() {
            return foodLog;
        }

        public String getServingUnit() {
            return servingUnit;
        }
    }
}
