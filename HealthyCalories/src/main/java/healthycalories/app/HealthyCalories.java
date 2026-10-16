package main.java.healthycalories.app;

import java.awt.Font;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import main.java.healthycalories.app.ui.MainFrame;
import main.java.healthycalories.model.user.Member;
import main.java.healthycalories.persistence.DailyCalorieRepository;
import main.java.healthycalories.persistence.MemberRepository;

/** จุดประสานงานของแอป เก็บสถานะสมาชิกและจัดการการเข้าถึงข้อมูลโปรไฟล์ */
public class HealthyCalories {
    private final MemberRepository memberRepository = new MemberRepository();
    private Member currentMember;
    private boolean authenticated;
    private String currentUsername = "";
    private LocalDate selectedDiaryDate = LocalDate.now();

    /** คืนแหล่งจัดเก็บบัญชีสมาชิกของแอป */
    public MemberRepository getMemberRepository() {
        return memberRepository;
    }

    /** คืนโปรไฟล์สมาชิกที่โหลดสำหรับบัญชีปัจจุบัน */
    public Member getCurrentMember() {
        return currentMember;
    }

    /** ระบุว่ามีบัญชีผู้ใช้ที่ยืนยันตัวตนแล้วหรือไม่ */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /** เปลี่ยนสถานะยืนยันตัวตนและล้างข้อมูลบัญชีเมื่อออกจากระบบ */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (!authenticated) {
            currentUsername = "";
            currentMember = null;
            selectedDiaryDate = LocalDate.now();
        }
    }

    /** คืนชื่อบัญชีปัจจุบันในรูปอีเมลที่ใช้ค้นหาข้อมูล */
    public String getCurrentUsername() {
        return currentUsername;
    }

    /** คืนวันที่เลือกเปิดดูในไดอารี่ */
    public LocalDate getSelectedDiaryDate() {
        return selectedDiaryDate;
    }

    /** เลือกวันที่ไดอารี่ โดยไม่อนุญาตวันที่ในอนาคต */
    public void setSelectedDiaryDate(LocalDate date) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("วันที่เลือกต้องไม่เป็นวันในอนาคต");
        }
        selectedDiaryDate = date;
    }

    /** ปรับชื่อบัญชีให้เป็นรูปแบบมาตรฐานและยกเลิกโปรไฟล์ที่แคชไว้ */
    public void setCurrentUsername(String username) {
        currentUsername = username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
        currentMember = null;
    }

    /** ย้ายข้อมูลเก่าที่เกี่ยวข้องและโหลดโปรไฟล์แคลอรี่ล่าสุดของบัญชี */
    public void loadPersistedCalorieProfile() throws IOException {
        DailyCalorieRepository repository = dailyCalorieRepository();
        repository.migrateLegacyFiles(memberRepository.getRegisteredEmails(),
                memberRepository.isOnlyRegisteredAccount(currentUsername));
        currentMember = repository.loadLatest();
        if (currentMember != null) {
            currentMember.setEmail(currentUsername);
        }
    }

    /** บันทึกโปรไฟล์สมาชิกหลังเตรียมที่เก็บของบัญชีปัจจุบัน */
    public void saveCalorieProfile(Member member) throws IOException {
        if (member == null) {
            throw new IllegalArgumentException("Member profile must not be null");
        }
        member.setEmail(currentUsername);
        DailyCalorieRepository repository = dailyCalorieRepository();
        prepareDailyCalorieRepository(repository);
        repository.save(member);
        currentMember = member;
    }

    /** บันทึกน้ำหนักตามวันที่และโหลดโปรไฟล์ล่าสุดกลับเข้าแอป */
    public void saveWeight(double weightKg, LocalDate date) throws IOException {
        if (currentMember == null) {
            throw new IllegalStateException("ยังไม่มีโปรไฟล์สมาชิก กรุณากรอกข้อมูลเริ่มต้นก่อน");
        }
        DailyCalorieRepository repository = dailyCalorieRepository();
        prepareDailyCalorieRepository(repository);
        repository.saveWeight(currentMember, date, weightKg);
        currentMember = repository.loadLatest();
        if (currentMember != null) {
            currentMember.setEmail(currentUsername);
        }
    }

    /** ลบน้ำหนักของวันที่ระบุและโหลดโปรไฟล์ใหม่เมื่อมีการลบจริง */
    public void removeWeight(LocalDate date) throws IOException {
        if (currentMember == null) {
            throw new IllegalStateException("ยังไม่มีโปรไฟล์สมาชิก");
        }
        DailyCalorieRepository repository = dailyCalorieRepository();
        prepareDailyCalorieRepository(repository);
        if (repository.removeWeight(date)) {
            currentMember = repository.loadLatest();
            if (currentMember != null) {
                currentMember.setEmail(currentUsername);
            }
        }
    }

    /** สร้างที่เก็บข้อมูลแคลอรี่สำหรับชื่อบัญชีที่กำลังใช้งาน */
    private DailyCalorieRepository dailyCalorieRepository() {
        return new DailyCalorieRepository(currentUsername);
    }

    /** เตรียมที่เก็บโดยย้ายไฟล์เก่าตามรายชื่อบัญชีที่ลงทะเบียน */
    private void prepareDailyCalorieRepository(DailyCalorieRepository repository)
            throws IOException {
        repository.migrateLegacyFiles(memberRepository.getRegisteredEmails(),
                memberRepository.isOnlyRegisteredAccount(currentUsername));
    }
    /** เริ่มแอปบน Event Dispatch Thread ของ Swing */
    public static void main(String[] args) {
        configureThaiFonts();
        SwingUtilities.invokeLater(() -> {
            HealthyCalories app = new HealthyCalories();
            MainFrame frame = new MainFrame(app);
            frame.setVisible(true);
        });
    }

    /** ตั้งค่าฟอนต์ของ Swing ให้แสดงอักษรไทยได้ก่อนเปิดหน้าต่าง */
    private static void configureThaiFonts() {
        UIDefaults defaults = UIManager.getDefaults();
        Enumeration<Object> keys = defaults.keys();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
                Font font = (Font) value;
                defaults.put(key, new FontUIResource("Tahoma", font.getStyle(), font.getSize()));
            }
        }
    }
}
