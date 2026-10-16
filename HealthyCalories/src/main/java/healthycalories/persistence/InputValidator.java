package main.java.healthycalories.persistence;

import java.util.regex.Pattern;

/** ตัวตรวจสอบรูปแบบและช่วงค่าที่กรอกในแบบฟอร์มของแอป */
public final class InputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s,@]+@[^\\s,@]+\\.[^\\s,@]+$");

    /** ป้องกันการสร้างออบเจ็กต์ยูทิลิตี */
    private InputValidator() {
    }

    /** ตรวจรูปแบบอีเมลอย่างพื้นฐานหลังตัดช่องว่างหัวท้าย */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /** ตรวจว่ารหัสผ่านไม่เป็น null หรือข้อความว่าง */
    public static boolean isValidPassword(String password) {
        return password != null && !password.trim().isEmpty();
    }

    /** ตรวจน้ำหนักให้อยู่ในช่วงมากกว่า 0 ถึง 500 กิโลกรัม */
    public static boolean isValidWeight(double weightKg) {
        return Double.isFinite(weightKg) && weightKg > 0 && weightKg <= 500;
    }

    /** ตรวจจำนวนบริโภคให้อยู่ในช่วงมากกว่า 0 ถึง 100 หน่วย */
    public static boolean isValidServings(double servings) {
        return Double.isFinite(servings) && servings > 0 && servings <= 100;
    }
}
