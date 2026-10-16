package main.java.healthycalories.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.java.healthycalories.model.enumeration.MealType;
import main.java.healthycalories.model.log.FoodLog;

/** จัดการรายการอาหารที่ผู้ใช้เพิ่มไว้แต่ยังรอยืนยัน โดยแยกตามเจ้าของ */
public class PendingFoodRepository {
    private static final String HEADER = "id,memberEmail,date,foodItemId,foodName,kcalPerServing,"
            + "mealType,servings,servingUnit";
    private static final String LEGACY_HEADER = "id,date,foodItemId,foodName,kcalPerServing,"
            + "mealType,servings,servingUnit";
    private static long lastGeneratedId;

    private final File pendingFile;
    private final String memberEmail;

    /** สร้างที่เก็บรายการรอยืนยันสำหรับอีเมลสมาชิก */
    public PendingFoodRepository(String memberEmail) {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "pending_food.csv"),
                normalizeEmail(memberEmail));
    }

    PendingFoodRepository(File pendingFile) {
        this(pendingFile, "guest");
    }

    PendingFoodRepository(File pendingFile, String memberEmail) {
        this.pendingFile = pendingFile;
        this.memberEmail = normalizeEmail(memberEmail);
    }

    /** ทำมาตรฐานอีเมลและกำหนด guest ให้กับค่าที่ว่าง */
    private static String normalizeEmail(String memberEmail) {
        return memberEmail == null || memberEmail.trim().isEmpty()
                ? "guest" : memberEmail.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /** เรียกการโหลดเพื่อย้ายข้อมูลรายการรอยืนยันจากรูปแบบเดิม */
    public void claimLegacyRecords() throws IOException {
        loadAll();
    }

    /** แปลงบันทึกอาหารเป็นรายการรอยืนยันและบันทึกพร้อมรหัสใหม่ */
    public PendingFood add(FoodLog foodLog, String date, String servingUnit)
            throws IOException {
        if (foodLog == null || foodLog.getFood() == null || foodLog.getMealType() == null) {
            throw new IllegalArgumentException("Food, meal, and food log are required");
        }
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Food log date is required");
        }
        if (servingUnit == null || servingUnit.trim().isEmpty()) {
            throw new IllegalArgumentException("Serving unit is required");
        }

        PendingFood pendingFood = new PendingFood(nextId(), date,
                foodLog.getFoodItemId(), foodLog.getFood().getName(),
                foodLog.getFood().getKcal(), foodLog.getMealType(),
                foodLog.getServings(), servingUnit.trim());
        List<PendingFood> pendingFoods = loadAll();
        pendingFoods.add(pendingFood);
        writeAll(pendingFoods);
        return pendingFood;
    }

    /** สร้างรหัสเพิ่มขึ้นตามเวลาและป้องกันรหัสซ้ำในการเรียกพร้อมกัน */
    private static synchronized String nextId() {
        long currentTime = System.currentTimeMillis();
        lastGeneratedId = Math.max(currentTime, lastGeneratedId + 1);
        return Long.toString(lastGeneratedId);
    }

    /** โหลดรายการรอยืนยันของสมาชิกหลังย้ายไฟล์เก่าที่จำเป็น */
    public List<PendingFood> loadAll() throws IOException {
        migrateLegacyFilesIfNeeded();
        List<PendingFood> pendingFoods = new ArrayList<>();
        if (!pendingFile.isFile() || pendingFile.length() == 0) {
            return pendingFoods;
        }
        for (StoredPendingFood stored : readStoredFoods(pendingFile, "")) {
            if (!stored.memberEmail.trim().isEmpty()
                    && memberEmail.equals(normalizeEmail(stored.memberEmail))) {
                pendingFoods.add(stored.food);
            }
        }
        return pendingFoods;
    }

    /** รวมไฟล์แยกรายบัญชีรุ่นเก่าเข้ากับไฟล์หลัก */
    private void migrateLegacyFilesIfNeeded() throws IOException {
        File parent = pendingFile.getAbsoluteFile().getParentFile();
        if (parent == null) {
            return;
        }
        File[] legacyFiles = parent.listFiles((directory, name) ->
                name.startsWith("pending_food_") && name.endsWith(".csv"));
        if (legacyFiles == null) {
            throw new IOException("ไม่สามารถอ่านรายชื่อไฟล์รายการอาหารที่รอยืนยันได้");
        }
        if (legacyFiles.length == 0) {
            if (!pendingFile.isFile() || pendingFile.length() == 0
                    || hasCurrentHeader(pendingFile)) {
                return;
            }
        }

        List<StoredPendingFood> allStoredFoods = new ArrayList<>();
        if (pendingFile.isFile() && pendingFile.length() > 0) {
            allStoredFoods.addAll(readStoredFoods(pendingFile, ""));
        }
        for (File legacyFile : legacyFiles) {
            allStoredFoods.addAll(readStoredFoods(legacyFile, ownerFromLegacyFilename(legacyFile)));
        }
        writeStoredFoods(allStoredFoods);

        for (File legacyFile : legacyFiles) {
            if (!legacyFile.delete()) {
                throw new IOException("ย้ายรายการอาหารแล้ว แต่ลบไฟล์เก่าไม่สำเร็จ: "
                        + legacyFile.getName());
            }
        }
    }

    /** ตรวจหัวตารางและยืนยันว่าไฟล์ใช้รูปแบบ CSV ที่รองรับ */
    private static boolean hasCurrentHeader(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header != null && header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }
            if (!HEADER.equals(header) && !LEGACY_HEADER.equals(header)) {
                throw new IOException("รูปแบบ pending food CSV ไม่ถูกต้องใน " + file.getName());
            }
            return HEADER.equals(header);
        }
    }

    /** ถอดรหัสเจ้าของจากชื่อไฟล์เดิมที่อาจเก็บอีเมลเป็นรหัสอักขระ */
    private static String ownerFromLegacyFilename(File file) {
        String filename = file.getName();
        String suffix = filename.substring("pending_food_".length(),
                filename.length() - ".csv".length());
        if ("guest".equalsIgnoreCase(suffix)) {
            return "guest";
        }
        if (suffix.matches("\\d+(?:_\\d+)*")) {
            StringBuilder decoded = new StringBuilder();
            try {
                for (String code : suffix.split("_")) {
                    decoded.append((char) Integer.parseInt(code));
                }
                return normalizeEmail(decoded.toString());
            } catch (NumberFormatException e) {
                return "";
            }
        }
        return normalizeEmail(suffix);
    }

    /** อ่านข้อมูลทั้งแบบใหม่และเก่า พร้อมผูกเจ้าของที่ได้จากไฟล์ */
    private static List<StoredPendingFood> readStoredFoods(File file, String legacyOwner)
            throws IOException {
        List<StoredPendingFood> storedFoods = new ArrayList<>();
        if (!file.isFile() || file.length() == 0) {
            return storedFoods;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            if (header != null && header.startsWith("\uFEFF")) {
                header = header.substring(1);
            }
            boolean legacyFormat = LEGACY_HEADER.equals(header);
            if (!HEADER.equals(header) && !legacyFormat) {
                throw new IOException("รูปแบบ pending food CSV ไม่ถูกต้องใน " + file.getName());
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> columns = parseCsv(line);
                    if (columns.size() != (legacyFormat ? 8 : 9)) {
                        throw new IllegalArgumentException("Unexpected number of CSV columns");
                    }
                    int offset = legacyFormat ? 0 : 1;
                    String owner = legacyFormat ? legacyOwner : columns.get(1);
                    PendingFood food = new PendingFood(columns.get(0),
                            columns.get(1 + offset), columns.get(2 + offset),
                            columns.get(3 + offset), Double.parseDouble(columns.get(4 + offset)),
                            MealType.fromValue(columns.get(5 + offset)),
                            Double.parseDouble(columns.get(6 + offset)),
                            columns.get(7 + offset));
                    storedFoods.add(new StoredPendingFood(owner, food));
                } catch (RuntimeException e) {
                    throw new IOException("ข้อมูลรายการอาหารที่รอยืนยันไม่ถูกต้องใน "
                            + file.getName() + " บรรทัด " + lineNumber, e);
                }
            }
        }
        return storedFoods;
    }

    /** ลบรายการรอยืนยันด้วยรหัสและเขียนข้อมูลใหม่เมื่อพบรายการ */
    public boolean delete(String id) throws IOException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Pending food ID is required");
        }
        List<PendingFood> pendingFoods = loadAll();
        boolean removed = pendingFoods.removeIf(food -> food.getId().equals(id));
        if (removed) {
            writeAll(pendingFoods);
        }
        return removed;
    }

    /** เขียนรายการของทุกเจ้าของโดยแทนเฉพาะรายการของบัญชีปัจจุบัน */
    private void writeAll(List<PendingFood> pendingFoods) throws IOException {
        List<StoredPendingFood> storedFoods = new ArrayList<>();
        if (pendingFile.isFile() && pendingFile.length() > 0) {
            for (StoredPendingFood stored : readStoredFoods(pendingFile, "")) {
                if (stored.memberEmail.trim().isEmpty()
                        || !memberEmail.equals(normalizeEmail(stored.memberEmail))) {
                    storedFoods.add(stored);
                }
            }
        }
        for (PendingFood food : pendingFoods) {
            storedFoods.add(new StoredPendingFood(memberEmail, food));
        }
        writeStoredFoods(storedFoods);
    }

    /** เขียนระเบียนพร้อมเจ้าของลงไฟล์ชั่วคราวแล้วสลับอย่างปลอดภัย */
    private void writeStoredFoods(List<StoredPendingFood> storedFoods) throws IOException {
        File parent = pendingFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์เก็บรายการอาหารได้");
        }
        File temporaryFile = File.createTempFile("pending_food", ".tmp", parent);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile))) {
            writer.write(HEADER);
            writer.newLine();
            for (StoredPendingFood stored : storedFoods) {
                PendingFood food = stored.food;
                writer.write(toCsv(new String[] {
                    food.getId(),
                    stored.memberEmail,
                    food.getDate(),
                    food.getFoodItemId(),
                    food.getFoodName(),
                    Double.toString(food.getCaloriesPerServing()),
                    food.getMealType().name(),
                    Double.toString(food.getServings()),
                    food.getServingUnit()
                }));
                writer.newLine();
            }
        } catch (IOException e) {
            if (!temporaryFile.delete()) {
                e.addSuppressed(new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath()));
            }
            throw e;
        }
        try {
            File backupFile = new File(pendingFile.getAbsolutePath() + ".bak");
            if (backupFile.exists() && !backupFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์สำรองรายการอาหารเดิมได้");
            }
            if (pendingFile.exists() && !pendingFile.renameTo(backupFile)) {
                throw new IOException("ไม่สามารถสำรองไฟล์รายการอาหารได้");
            }
            if (!temporaryFile.renameTo(pendingFile)) {
                if (backupFile.exists() && !backupFile.renameTo(pendingFile)) {
                    throw new IOException("ไม่สามารถกู้คืนไฟล์รายการอาหารเดิมได้");
                }
                throw new IOException("ไม่สามารถแทนที่ไฟล์รายการอาหารได้");
            }
            if (backupFile.exists() && !backupFile.delete()) {
                throw new IOException("บันทึกข้อมูลแล้ว แต่ไม่สามารถลบไฟล์สำรองได้");
            }
        } finally {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath());
            }
        }
    }

    /** ระเบียนภายในที่จับคู่เจ้าของบัญชีกับรายการรอยืนยัน */
    private static final class StoredPendingFood {
        private final String memberEmail;
        private final PendingFood food;

        /** ผูกเจ้าของกับรายการระหว่างรวมและเขียนข้อมูลหลายบัญชี */
        private StoredPendingFood(String memberEmail, PendingFood food) {
            this.memberEmail = memberEmail == null ? "" : memberEmail;
            this.food = food;
        }
    }

    /** แปลงค่าให้เป็นแถว CSV ที่ escape อักขระตามรูปแบบ */
    private static String toCsv(String[] values) {
        return CsvManager.formatLine(values);
    }

    /** แยกแถว CSV โดยยกข้อผิดพลาดการอ่านเป็นข้อผิดพลาดรูปแบบข้อมูล */
    private static List<String> parseCsv(String line) {
        try {
            return CsvManager.parseLine(line);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /** ข้อมูลอาหารที่รอการยืนยันเพื่อเพิ่มลงบันทึกประจำวัน */
    public static final class PendingFood {
        private final String id;
        private final String date;
        private final String foodItemId;
        private final String foodName;
        private final double caloriesPerServing;
        private final MealType mealType;
        private final double servings;
        private final String servingUnit;

        /** สร้างรายการรอยืนยันหลังตรวจว่าฟิลด์สำคัญทั้งหมดมีค่าถูกต้อง */
        private PendingFood(String id, String date, String foodItemId, String foodName,
                double caloriesPerServing, MealType mealType, double servings,
                String servingUnit) {
            if (id == null || id.trim().isEmpty() || date == null || date.trim().isEmpty()
                    || foodItemId == null || foodName == null || foodName.trim().isEmpty()
                    || !Double.isFinite(caloriesPerServing) || caloriesPerServing < 0
                    || mealType == null || !Double.isFinite(servings) || servings <= 0
                    || servingUnit == null || servingUnit.trim().isEmpty()) {
                throw new IllegalArgumentException("Pending food details are invalid");
            }
            this.id = id;
            this.date = date;
            this.foodItemId = foodItemId;
            this.foodName = foodName;
            this.caloriesPerServing = caloriesPerServing;
            this.mealType = mealType;
            this.servings = servings;
            this.servingUnit = servingUnit;
        }

        public String getId() { return id; }
        public String getDate() { return date; }
        public String getFoodItemId() { return foodItemId; }
        public String getFoodName() { return foodName; }
        public double getCaloriesPerServing() { return caloriesPerServing; }
        public MealType getMealType() { return mealType; }
        public double getServings() { return servings; }
        public String getServingUnit() { return servingUnit; }
    }
}
