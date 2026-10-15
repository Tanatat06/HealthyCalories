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

public class HealthyCalories {
    private final MemberRepository memberRepository = new MemberRepository();
    private Member currentMember;
    private boolean authenticated;
    private String currentUsername = "";
    private LocalDate selectedDiaryDate = LocalDate.now();

    public MemberRepository getMemberRepository() {
        return memberRepository;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        if (!authenticated) {
            currentUsername = "";
            currentMember = null;
            selectedDiaryDate = LocalDate.now();
        }
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public LocalDate getSelectedDiaryDate() {
        return selectedDiaryDate;
    }

    public void setSelectedDiaryDate(LocalDate date) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("วันที่เลือกต้องไม่เป็นวันในอนาคต");
        }
        selectedDiaryDate = date;
    }

    public void setCurrentUsername(String username) {
        currentUsername = username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
        currentMember = null;
    }

    public void loadPersistedCalorieProfile() throws IOException {
        DailyCalorieRepository repository = dailyCalorieRepository();
        repository.migrateLegacyFiles(memberRepository.getRegisteredEmails(),
                memberRepository.isOnlyRegisteredAccount(currentUsername));
        currentMember = repository.loadLatest();
        if (currentMember != null) {
            currentMember.setEmail(currentUsername);
        }
    }

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

    private DailyCalorieRepository dailyCalorieRepository() {
        return new DailyCalorieRepository(currentUsername);
    }

    private void prepareDailyCalorieRepository(DailyCalorieRepository repository)
            throws IOException {
        repository.migrateLegacyFiles(memberRepository.getRegisteredEmails(),
                memberRepository.isOnlyRegisteredAccount(currentUsername));
    }
    public static void main(String[] args) {
        configureThaiFonts();
        SwingUtilities.invokeLater(() -> {
            HealthyCalories app = new HealthyCalories();
            MainFrame frame = new MainFrame(app);
            frame.setVisible(true);
        });
    }

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
