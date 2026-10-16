package main.java.healthycalories.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** อ่านและเขียนบัญชีสมาชิกจากไฟล์ CSV ของแอป */
public class MemberRepository {
    private final File membersFile;

    /** สร้างที่เก็บโดยใช้ไฟล์บัญชีสมาชิกเริ่มต้นของแอป */
    public MemberRepository() {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "members.csv"));
    }

    /** สร้างที่เก็บสำหรับไฟล์ที่กำหนด (ใช้ภายในแพ็กเกจเพื่อทดสอบ) */
    MemberRepository(File membersFile) {
        this.membersFile = membersFile;
    }

    /** ลงทะเบียนบัญชีใหม่ และคืน false เมื่อพบอีเมลที่ลงทะเบียนอยู่แล้ว */
    public boolean register(String email, String password) throws IOException {
        String cleanEmail = email.trim();

        if (cleanEmail.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("อีเมลและรหัสผ่านต้องไม่ว่าง");
        }

        if (membersFile.exists()) {
            Scanner scanner = new Scanner(membersFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("email,password")) {
                    continue;
                }
                String[] member = line.split(",", 2);
                if (member.length == 2 && member[0].equalsIgnoreCase(cleanEmail)) {
                    scanner.close();
                    return false;
                }
            }
            scanner.close();
        }

        File parentFolder = membersFile.getParentFile();
        if (parentFolder != null && !parentFolder.exists()) {
            parentFolder.mkdirs();
        }

        boolean writeHeader = !membersFile.exists() || membersFile.length() == 0;
        FileWriter writer = new FileWriter(membersFile, true);
        if (writeHeader) {
            writer.write("email,password");
            writer.write(System.lineSeparator());
        }
        writer.write(cleanEmail + "," + password);
        writer.write(System.lineSeparator());
        writer.close();
        return true;
    }

    /** ตรวจสอบอีเมลและรหัสผ่านกับบัญชีที่จัดเก็บไว้ */
    public boolean authenticate(String email, String password) throws IOException {
        if (!membersFile.exists() || email == null || password == null) {
            return false;
        }

        String cleanEmail = email.trim();
        Scanner scanner = new Scanner(membersFile);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("email,password")) {
                continue;
            }
            String[] member = line.split(",", 2);

            if (member.length == 2
                    && member[0].equalsIgnoreCase(cleanEmail)
                    && member[1].equals(password)) {
                scanner.close();
                return true;
            }
        }
        scanner.close();
        return false;
    }

    /** ตรวจว่ามีบัญชีสมาชิกเพียงบัญชีเดียวและตรงกับอีเมลที่ระบุ */
    public boolean isOnlyRegisteredAccount(String email) throws IOException {
        if (!membersFile.isFile() || email == null || email.trim().isEmpty()) {
            return false;
        }

        int accountCount = 0;
        boolean requestedAccountFound = false;
        String cleanEmail = email.trim();
        try (Scanner scanner = new Scanner(membersFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("email,password")) {
                    continue;
                }
                String[] account = line.split(",", 2);
                if (account.length != 2 || account[0].trim().isEmpty()) {
                    continue;
                }
                accountCount++;
                if (account[0].trim().equalsIgnoreCase(cleanEmail)) {
                    requestedAccountFound = true;
                }
            }
        }
        return accountCount == 1 && requestedAccountFound;
    }

    /** อ่านอีเมลของบัญชีที่ถูกต้องทั้งหมดจากไฟล์สมาชิก */
    public List<String> getRegisteredEmails() throws IOException {
        List<String> emails = new ArrayList<>();
        if (!membersFile.isFile()) {
            return emails;
        }
        try (Scanner scanner = new Scanner(membersFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("email,password")) {
                    continue;
                }
                String[] account = line.split(",", 2);
                if (account.length == 2 && !account[0].trim().isEmpty()) {
                    emails.add(account[0].trim());
                }
            }
        }
        return emails;
    }
}
