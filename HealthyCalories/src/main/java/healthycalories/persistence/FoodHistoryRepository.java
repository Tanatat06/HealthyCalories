package main.java.healthycalories.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import main.java.healthycalories.model.item.FoodItem;

/** บันทึกและเรียกดูประวัติอาหารที่ผู้ใช้เลือก โดยแยกข้อมูลตามสมาชิก */
public class FoodHistoryRepository {
    private static final String FILE_NAME = "searchhistory.csv";
    private static final String LEGACY_FILE_NAME = "foodhistory.csv";
    private static final String LEGACY_HEADER = "selectedAt,foodItemId,foodName,kcal";
    private static final String HEADER = LEGACY_HEADER + ",memberEmail";
    private final File historyFile;
    private final String memberEmail;

    /** สร้างที่เก็บประวัติสำหรับผู้ใช้ที่ยังไม่ระบุบัญชี */
    public FoodHistoryRepository() {
        this("");
    }

    /** สร้างที่เก็บประวัติซึ่งผูกกับอีเมลสมาชิก */
    public FoodHistoryRepository(String memberEmail) {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + FILE_NAME), memberEmail);
    }

    FoodHistoryRepository(File historyFile) {
        this(historyFile, "");
    }

    FoodHistoryRepository(File historyFile, String memberEmail) {
        this.historyFile = historyFile;
        this.memberEmail = normalizeEmail(memberEmail);
    }

    /** ทำมาตรฐานอีเมลสำหรับกรองรายการประวัติ */
    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    /** ป้องกันการเขียนประวัติหากยังไม่มีบัญชีสมาชิก */
    private void requireMemberEmail() {
        if (memberEmail.isEmpty()) {
            throw new IllegalStateException("ต้องเข้าสู่ระบบก่อนบันทึกประวัติอาหาร");
        }
    }

    /** ย้ายไฟล์/ระเบียนประวัติเดิมก่อนเริ่มใช้งาน */
    public void claimLegacyRecords() throws IOException {
        if (!memberEmail.isEmpty()) {
            migrateLegacyFileIfNeeded();
        }
    }

    /** เพิ่มรายการอาหารที่เลือกพร้อมเวลาและเจ้าของลงในประวัติ */
    public void saveSelection(FoodItem food) throws IOException {
        requireMemberEmail();
        if (food == null || food.getName() == null || food.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Food selection is required");
        }
        File parent = historyFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("ไม่สามารถสร้างโฟลเดอร์สำหรับ searchhistory.csv ได้");
        }
        migrateLegacyFileIfNeeded();
        boolean writeHeader = !historyFile.exists() || historyFile.length() == 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile, true))) {
            if (writeHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(toCsv(new String[] {
                LocalDateTime.now().toString(),
                food.getItemId(),
                food.getName(),
                Double.toString(food.getKcal()),
                memberEmail
            }));
            writer.newLine();
        }
    }

    /** โหลดประวัติของบัญชี เรียงจากใหม่ไปเก่าและจำกัดจำนวนผลลัพธ์ */
    public List<HistoryEntry> loadRecent(int limit) throws IOException {
        if (limit < 0) {
            throw new IllegalArgumentException("History limit cannot be negative");
        }
        if (memberEmail.isEmpty()) {
            return new ArrayList<>();
        }
        claimLegacyRecords();
        List<HistoryEntry> entries = new ArrayList<>();
        if (!historyFile.isFile() || historyFile.length() == 0) {
            return entries;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line = reader.readLine();
            if (line == null) {
                return entries;
            }
            if (line.startsWith("\uFEFF")) {
                line = line.substring(1);
            }
            boolean legacyFormat = LEGACY_HEADER.equals(line);
            if (!HEADER.equals(line) && !legacyFormat) {
                throw new IOException("รูปแบบ searchhistory.csv ไม่ถูกต้อง");
            }
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    List<String> columns = parseCsv(line);
                    if (columns.size() != (legacyFormat ? 4 : 5)) {
                        throw new IllegalArgumentException("Unexpected number of CSV columns");
                    }
                    String rowEmail = legacyFormat ? "" : normalizeEmail(columns.get(4));
                    if (!memberEmail.equals(rowEmail)) {
                        continue;
                    }
                    entries.add(new HistoryEntry(
                            LocalDateTime.parse(columns.get(0)),
                            columns.get(1),
                            columns.get(2),
                            Double.parseDouble(columns.get(3))));
                } catch (RuntimeException e) {
                    throw new IOException("ข้อมูล searchhistory.csv ไม่ถูกต้องที่บรรทัด "
                            + lineNumber, e);
                }
            }
        }
        Collections.reverse(entries);
        if (entries.size() > limit) {
            return new ArrayList<>(entries.subList(0, limit));
        }
        return entries;
    }

    /** ลบระเบียนประวัติที่ตรงกับรายการที่เลือกเพียงหนึ่งรายการ */
    public boolean deleteSelection(HistoryEntry entry) throws IOException {
        requireMemberEmail();
        if (entry == null) {
            throw new IllegalArgumentException("History entry is required");
        }
        claimLegacyRecords();
        if (!historyFile.isFile() || historyFile.length() == 0) {
            return false;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            return false;
        }
        String header = lines.get(0).replace("\uFEFF", "");
        if (!HEADER.equals(header)) {
            throw new IOException("รูปแบบส่วนหัว searchhistory.csv ไม่ถูกต้อง");
        }

        boolean removed = false;
        List<String> updatedLines = new ArrayList<>(lines.size());
        updatedLines.add(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> columns = parseCsv(line);
            if (columns.size() != 5) {
                throw new IOException("ข้อมูล searchhistory.csv ไม่ถูกต้องที่บรรทัด "
                        + (i + 1));
            }
            boolean matches = !removed
                    && memberEmail.equals(normalizeEmail(columns.get(4)))
                    && entry.matches(columns);
            if (matches) {
                removed = true;
            } else {
                updatedLines.add(line);
            }
        }
        if (!removed) {
            return false;
        }

        File parent = historyFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile(historyFile.getName(), ".tmp", parent);
        File backupFile = new File(historyFile.getAbsolutePath() + ".bak");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile))) {
                for (String line : updatedLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceFileWithBackup(temporaryFile, backupFile);
        } finally {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath());
            }
        }
        return true;
    }

    /** แปลงระเบียน/ชื่อไฟล์ประวัติรุ่นเก่าและเพิ่มเจ้าของที่ระบุได้ */
    private void migrateLegacyFileIfNeeded() throws IOException {
        migrateLegacyFileNameIfNeeded();
        if (!historyFile.isFile() || historyFile.length() == 0) {
            return;
        }
        boolean assignUnownedRows = new MemberRepository()
                .isOnlyRegisteredAccount(memberEmail);
        List<String> migratedLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            header = header.replace("\uFEFF", "");
            boolean legacyFormat = LEGACY_HEADER.equals(header);
            if (!legacyFormat && !HEADER.equals(header)) {
                throw new IOException("รูปแบบส่วนหัว searchhistory.csv ไม่ถูกต้อง");
            }
            migratedLines.add(HEADER);
            String line;
            int lineNumber = 1;
            boolean changed = legacyFormat;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                List<String> values = parseCsv(line);
                if (values.size() != (legacyFormat ? 4 : 5)) {
                    throw new IOException("ข้อมูล searchhistory.csv ไม่ถูกต้องที่บรรทัด "
                            + lineNumber);
                }
                if (legacyFormat) {
                    values.add(assignUnownedRows ? memberEmail : "");
                } else if (values.get(4).trim().isEmpty() && assignUnownedRows) {
                    values.set(4, memberEmail);
                    changed = true;
                }
                migratedLines.add(toCsv(values.toArray(new String[0])));
            }
            if (!changed) {
                return;
            }
        }

        File parent = historyFile.getAbsoluteFile().getParentFile();
        File temporaryFile = File.createTempFile(historyFile.getName(), ".tmp", parent);
        File backupFile = new File(historyFile.getAbsolutePath() + ".bak");
        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile))) {
                for (String line : migratedLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            replaceFileWithBackup(temporaryFile, backupFile);
        } finally {
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                throw new IOException("ไม่สามารถลบไฟล์ชั่วคราวได้: "
                        + temporaryFile.getAbsolutePath());
            }
        }
    }

    /** เปลี่ยนชื่อไฟล์ประวัติเก่าหากยังไม่มีไฟล์ชื่อปัจจุบัน */
    private void migrateLegacyFileNameIfNeeded() throws IOException {
        if (!FILE_NAME.equals(historyFile.getName())) {
            return;
        }
        File parent = historyFile.getAbsoluteFile().getParentFile();
        File legacyFile = new File(parent, LEGACY_FILE_NAME);
        if (!legacyFile.exists()) {
            return;
        }
        if (historyFile.exists()) {
            throw new IOException("พบทั้ง searchhistory.csv และ foodhistory.csv "
                    + "กรุณารวมข้อมูลก่อนใช้งานต่อ");
        }
        if (!legacyFile.renameTo(historyFile)) {
            throw new IOException("ไม่สามารถเปลี่ยนชื่อไฟล์ foodhistory.csv ได้");
        }
    }

    /** สลับไฟล์ประวัติฉบับใหม่โดยสำรองไฟล์เดิมและกู้คืนเมื่อผิดพลาด */
    private void replaceFileWithBackup(File temporaryFile, File backupFile) throws IOException {
        if (backupFile.exists() && !backupFile.delete()) {
            throw new IOException("ไม่สามารถลบไฟล์สำรอง searchhistory.csv เดิมได้");
        }
        if (!historyFile.renameTo(backupFile)) {
            throw new IOException("ไม่สามารถสำรองไฟล์ searchhistory.csv ได้");
        }
        if (!temporaryFile.renameTo(historyFile)) {
            if (!backupFile.renameTo(historyFile)) {
                throw new IOException("ไม่สามารถกู้คืน searchhistory.csv หลังปรับปรุงได้");
            }
            throw new IOException("ไม่สามารถบันทึกไฟล์ searchhistory.csv ที่ปรับปรุงแล้วได้");
        }
        if (!backupFile.delete()) {
            throw new IOException("ปรับปรุง searchhistory.csv แล้ว แต่ลบไฟล์สำรองไม่สำเร็จ");
        }
    }

    /** แปลงฟิลด์ให้เป็นแถว CSV ที่ escape อักขระตามรูปแบบ */
    private static String toCsv(String[] values) {
        return CsvManager.formatLine(values);
    }

    /** แยกแถว CSV และแปลงข้อผิดพลาด I/O เป็นข้อผิดพลาดข้อมูล */
    private static List<String> parseCsv(String line) {
        try {
            return CsvManager.parseLine(line);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /** ข้อมูลรายการอาหารหนึ่งรายการที่ผู้ใช้เคยเลือก พร้อมเวลาและแคลอรี่ */
    public static final class HistoryEntry {
        private final LocalDateTime selectedAt;
        private final String foodItemId;
        private final String foodName;
        private final double calories;

        /** เก็บเวลาที่เลือก รหัสและชื่ออาหาร รวมถึงแคลอรี่ในประวัติหนึ่งรายการ */
        private HistoryEntry(LocalDateTime selectedAt, String foodItemId,
                String foodName, double calories) {
            this.selectedAt = selectedAt;
            this.foodItemId = foodItemId;
            this.foodName = foodName;
            this.calories = calories;
        }

        public LocalDateTime getSelectedAt() {
            return selectedAt;
        }

        public String getFoodItemId() {
            return foodItemId;
        }

        public String getFoodName() {
            return foodName;
        }

        public double getCalories() {
            return calories;
        }

        /** ตรวจว่าคอลัมน์ CSV เป็นรายการเดียวกับประวัตินี้หรือไม่ */
        private boolean matches(List<String> columns) {
            try {
                return selectedAt.equals(LocalDateTime.parse(columns.get(0)))
                        && Objects.equals(foodItemId, columns.get(1))
                        && Objects.equals(foodName, columns.get(2))
                        && Double.compare(calories, Double.parseDouble(columns.get(3))) == 0;
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid food history entry", e);
            }
        }
    }
}
