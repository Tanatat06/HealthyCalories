package main.java.healthycalories.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MemberRepository {
    private final File membersFile;

    public MemberRepository() {
        this(new File("ProjectFolders" + File.separator + "csv"
                + File.separator + "members.csv"));
    }

    MemberRepository(File membersFile) {
        this.membersFile = membersFile;
    }

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
}
