package main.java.healthycalories.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.java.healthycalories.model.enumeration.Gender;
import main.java.healthycalories.model.goal.WeeklyGoal;
import main.java.healthycalories.model.log.DailyLog;
import main.java.healthycalories.model.user.Member;

public class DailyCalorieRepository {
    private static final String HEADER = "recordType,memberEmail,gender,age,heightCm,"
            + "weightKg,targetWeightKg,weeklyGoal,startWeightKg,dailyCalories";
    private static final String LEGACY_HEADER = "recordType,gender,age,heightCm,weightKg,"
            + "targetWeightKg,weeklyGoal,startWeightKg,dailyCalories";
    private static final String PROFILE_RECORD = "CALORIE_PROFILE";
    private static final String WEIGHT_RECORD = "DAILY_WEIGHT";

    private final File dailyLogsFile;
    private final String memberEmail;

    public DailyCalorieRepository() {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "daily_logs.csv"), "guest");
    }

    public DailyCalorieRepository(String username) {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "daily_logs.csv"), normalizeEmail(username));
    }

    DailyCalorieRepository(File dailyLogsFile) {
        this(dailyLogsFile, "test-user");
    }

    DailyCalorieRepository(File dailyLogsFile, String username) {
        this.dailyLogsFile = dailyLogsFile;
        this.memberEmail = normalizeEmail(username);
    }

    private static String normalizeEmail(String username) {
        return username == null || username.trim().isEmpty()
                ? "guest" : username.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static String fileKey(String username) {
        String normalizedUsername = username == null || username.trim().isEmpty()
                ? "guest" : username.trim().toLowerCase(java.util.Locale.ROOT);
        return Integer.toHexString(normalizedUsername.hashCode());
    }

    public void save(Member member) throws IOException {
        if (member == null) {
            throw new IllegalArgumentException("Member profile must not be null");
        }
        if (member.getGender() == null || member.getWeeklyGoal() == null) {
            throw new IllegalArgumentException("Member gender and weekly goal are required");
        }

        double dailyCalories = member.getDailyCalorie();
        if (dailyCalories <= 0) {
            dailyCalories = member.calDailyCalorie();
        }
        File parent = dailyLogsFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์สำหรับข้อมูลโปรไฟล์ได้");
        }

        boolean writeHeader = !dailyLogsFile.exists() || dailyLogsFile.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dailyLogsFile, true))) {
            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(String.join(",",
                    PROFILE_RECORD,
                    memberEmail,
                    member.getGender().name(),
                    Integer.toString(member.getAge()),
                    Double.toString(member.getHeight()),
                    Double.toString(member.getWeight()),
                    Double.toString(member.getTargetWeight()),
                    member.getWeeklyGoal().name(),
                    Double.toString(member.getStartWeight()),
                    Double.toString(dailyCalories)));
            writer.newLine();
        }
    }

    public void saveWeight(Member member, LocalDate date, double weightKg) throws IOException {
        if (member == null) {
            throw new IllegalArgumentException("Member profile must not be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Weight log date must not be null");
        }
        if (!Double.isFinite(weightKg) || weightKg <= 0 || weightKg > 500) {
            throw new IllegalArgumentException("Weight must be between 0 and 500 kg");
        }

        File parent = dailyLogsFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์สำหรับข้อมูลโปรไฟล์ได้");
        }

        boolean writeHeader = !dailyLogsFile.exists() || dailyLogsFile.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dailyLogsFile, true))) {
            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(String.join(",",
                    WEIGHT_RECORD,
                    memberEmail,
                    date.toString(),
                    Double.toString(weightKg)));
            writer.newLine();
        }

        DailyLog log = member.getOrCreateDailyLog(date.toString());
        log.setWeight(weightKg);
        member.setWeight(weightKg);
    }

    public boolean removeWeight(LocalDate date) throws IOException {
        if (date == null) {
            throw new IllegalArgumentException("Weight log date must not be null");
        }
        if (!dailyLogsFile.isFile()) {
            return false;
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(dailyLogsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        java.util.List<String> updatedLines = new java.util.ArrayList<>();
        boolean removed = false;
        for (String line : lines) {
            String[] columns = line.split(",", -1);
            if (columns.length == 4 && WEIGHT_RECORD.equals(columns[0])
                    && memberEmail.equals(columns[1]) && date.toString().equals(columns[2])) {
                removed = true;
            } else {
                updatedLines.add(line);
            }
        }
        if (!removed) {
            return false;
        }

        File parent = dailyLogsFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile("daily_logs", ".tmp", parent);
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile))) {
                for (String line : updatedLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceDailyLogsFile(temporaryFile);
        } catch (IOException e) {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                e.addSuppressed(new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath()));
            }
            throw e;
        }
        return true;
    }

    public Member loadLatest() throws IOException {
        if (!dailyLogsFile.isFile()) {
            return null;
        }

        Member latest = null;
        LocalDate latestWeightDate = null;
        int latestProfileLine = -1;
        int latestWeightLine = -1;
        Map<LocalDate, Double> weights = new HashMap<>();
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(dailyLogsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isEmpty() || line.equals(HEADER)) {
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length == 10 && PROFILE_RECORD.equals(columns[0])
                        && memberEmail.equals(normalizeEmail(columns[1]))) {
                    try {
                        latest = parseProfile(columns, 2);
                        latestProfileLine = lineNumber;
                    } catch (IllegalArgumentException e) {
                        throw new IOException("ข้อมูลโปรไฟล์แคลอรี่ไม่ถูกต้องที่บรรทัด "
                                + lineNumber, e);
                    }
                } else if (columns.length == 4 && WEIGHT_RECORD.equals(columns[0])
                        && memberEmail.equals(normalizeEmail(columns[1]))) {
                    try {
                        LocalDate date = LocalDate.parse(columns[2]);
                        double weight = Double.parseDouble(columns[3]);
                        if (!Double.isFinite(weight) || weight <= 0 || weight > 500) {
                            throw new IllegalArgumentException("Weight is outside valid range");
                        }
                        weights.put(date, weight);
                        latestWeightLine = lineNumber;
                    } catch (IllegalArgumentException e) {
                        throw new IOException("ข้อมูลน้ำหนักไม่ถูกต้องที่บรรทัด "
                                + lineNumber, e);
                    }
                }
            }
        }
        if (latest != null) {
            for (Map.Entry<LocalDate, Double> entry : weights.entrySet()) {
                DailyLog log = latest.getOrCreateDailyLog(entry.getKey().toString());
                log.setWeight(entry.getValue());
                if (latestWeightDate == null || !entry.getKey().isBefore(latestWeightDate)) {
                    latestWeightDate = entry.getKey();
                }
            }
            if (latestWeightDate != null && latestWeightLine > latestProfileLine) {
                latest.setWeight(weights.get(latestWeightDate));
            }
        }
        return latest;
    }

    public void migrateLegacyFiles(List<String> registeredEmails,
            boolean assignUnownedRowsToCurrentUser) throws IOException {
        File parent = dailyLogsFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์สำหรับข้อมูลโปรไฟล์ได้");
        }

        List<String> mergedLines = new ArrayList<>();
        Set<String> knownLines = new java.util.HashSet<>();
        boolean sharedFileExists = dailyLogsFile.isFile() && dailyLogsFile.length() > 0;
        boolean sharedOwnershipChanged = false;
        if (sharedFileExists) {
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(dailyLogsFile))) {
                String header = reader.readLine();
                if (header != null && header.startsWith("\uFEFF")) {
                    header = header.substring(1);
                }
                if (HEADER.equals(header)) {
                    mergedLines.add(HEADER);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String migrated = claimUnownedRowsToCurrentUser(line,
                                assignUnownedRowsToCurrentUser);
                        sharedOwnershipChanged |= !line.equals(migrated);
                        mergedLines.add(migrated);
                        knownLines.add(migrated);
                    }
                } else if (LEGACY_HEADER.equals(header)) {
                    mergedLines.add(HEADER);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String migrated = migrateLegacyRow(line,
                                assignUnownedRowsToCurrentUser ? memberEmail : "");
                        mergedLines.add(migrated);
                        knownLines.add(migrated);
                    }

                } else {
                    throw new IOException("รูปแบบส่วนหัว daily_logs.csv ไม่ถูกต้อง");
                }
            }
        } else {
            mergedLines.add(HEADER);
        }

        File[] oldFiles = parent.listFiles((directory, name) ->
                name.startsWith("daily_logs_") && name.endsWith(".csv"));
        if (oldFiles == null) {
            throw new IOException("ไม่สามารถอ่านรายชื่อไฟล์ประวัติน้ำหนักได้");
        }
        Map<String, String> emailByFileKey = new HashMap<>();
        if (registeredEmails != null) {
            for (String email : registeredEmails) {
                emailByFileKey.put(fileKey(email), normalizeEmail(email));
            }
        }

        List<File> filesToDelete = new ArrayList<>();
        for (File oldFile : oldFiles) {
            if (!oldFile.isFile() || oldFile.length() == 0) {
                continue;
            }
            String fileKey = oldFile.getName().substring("daily_logs_".length(),
                    oldFile.getName().length() - ".csv".length());
            String owner = emailByFileKey.getOrDefault(fileKey, "");
            try (BufferedReader reader = new BufferedReader(new java.io.FileReader(oldFile))) {
                String header = reader.readLine();
                if (header != null && header.startsWith("\uFEFF")) {
                    header = header.substring(1);
                }
                if (!LEGACY_HEADER.equals(header)) {
                    throw new IOException("รูปแบบส่วนหัวไม่ถูกต้องใน " + oldFile.getName());
                }
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    String migrated;
                    try {
                        migrated = migrateLegacyRow(line, owner);
                    } catch (IllegalArgumentException e) {
                        throw new IOException("ข้อมูลไม่ถูกต้องใน " + oldFile.getName()
                                + " บรรทัด " + lineNumber, e);
                    }
                    if (knownLines.add(migrated)) {
                        mergedLines.add(migrated);
                    }
                }
            }
            filesToDelete.add(oldFile);
        }

        if (!sharedFileExists || sharedOwnershipChanged || !filesToDelete.isEmpty()) {
            writeAllLines(mergedLines);
        }
        for (File oldFile : filesToDelete) {
            if (!oldFile.delete()) {
                throw new IOException("ย้ายข้อมูลแล้ว แต่ลบไฟล์เก่าไม่สำเร็จ: "
                        + oldFile.getName());
            }
        }
    }

    private String claimUnownedRowsToCurrentUser(String line, boolean mayClaim) {
        if (!mayClaim) {
            return line;
        }
        String[] columns = line.split(",", -1);
        if (columns.length != 10 && columns.length != 4) {
            return line;
        }
        if (!PROFILE_RECORD.equals(columns[0]) && !WEIGHT_RECORD.equals(columns[0])) {
            return line;
        }
        String owner = normalizeEmail(columns[1]);
        if (owner.isEmpty() || "guest".equals(owner)) {
            columns[1] = memberEmail;
            return String.join(",", columns);
        }
        return line;
    }

    private String migrateLegacyRow(String line, String owner) {
        String[] columns = line.split(",", -1);
        if (PROFILE_RECORD.equals(columns[0]) && columns.length == 9) {
            String[] migrated = new String[10];
            migrated[0] = columns[0];
            migrated[1] = owner;
            System.arraycopy(columns, 1, migrated, 2, 8);
            return String.join(",", migrated);
        }
        if (WEIGHT_RECORD.equals(columns[0]) && columns.length == 3) {
            return String.join(",", columns[0], owner, columns[1], columns[2]);
        }
        throw new IllegalArgumentException("รูปแบบข้อมูลบรรทัดเดิมไม่ถูกต้อง");
    }

    private void writeAllLines(List<String> lines) throws IOException {
        File parent = dailyLogsFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile("daily_logs", ".tmp", parent);
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceDailyLogsFile(temporaryFile);
        } catch (IOException e) {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                e.addSuppressed(new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath()));
            }
            throw e;
        }
    }

    private void replaceDailyLogsFile(File temporaryFile) throws IOException {
        File backupFile = null;
        if (dailyLogsFile.exists()) {
            File parent = dailyLogsFile.getAbsoluteFile().getParentFile();
            backupFile = File.createTempFile("daily_logs_backup", ".tmp", parent);
            if (!backupFile.delete()) {
                throw new IOException("ไม่สามารถเตรียมไฟล์สำรองข้อมูลได้");
            }
            if (!dailyLogsFile.renameTo(backupFile)) {
                throw new IOException("ไม่สามารถสำรองไฟล์ข้อมูลเดิมได้");
            }
        }

        if (!temporaryFile.renameTo(dailyLogsFile)) {
            IOException error = new IOException("ไม่สามารถแทนที่ไฟล์ข้อมูลได้");
            if (backupFile != null && !backupFile.renameTo(dailyLogsFile)) {
                error.addSuppressed(new IOException("ไม่สามารถกู้คืนไฟล์ข้อมูลเดิมได้: "
                        + backupFile.getAbsolutePath()));
            }
            throw error;
        }
        if (backupFile != null && !backupFile.delete()) {
            throw new IOException("แทนที่ไฟล์ข้อมูลแล้ว แต่ลบไฟล์สำรองไม่สำเร็จ: "
                    + backupFile.getAbsolutePath());
        }
    }

    private static Member parseProfile(String[] columns, int firstValue) {
        if (columns.length != firstValue + 8) {
            throw new IllegalArgumentException("Unexpected number of profile columns");
        }
        Gender gender = Gender.valueOf(columns[firstValue]);
        int age = Integer.parseInt(columns[firstValue + 1]);
        double height = Double.parseDouble(columns[firstValue + 2]);
        double weight = Double.parseDouble(columns[firstValue + 3]);
        double targetWeight = Double.parseDouble(columns[firstValue + 4]);
        WeeklyGoal weeklyGoal = WeeklyGoal.valueOf(columns[firstValue + 5]);
        double startWeight = Double.parseDouble(columns[firstValue + 6]);
        double dailyCalories = Double.parseDouble(columns[firstValue + 7]);

        Member member = new Member("", "", gender, age, height, weight);
        member.updateProfile(age, height, weight, targetWeight);
        member.setWeeklyGoal(weeklyGoal);
        member.setStartWeight(startWeight);
        member.setDailyCalorie(dailyCalories);
        return member;
    }
}
