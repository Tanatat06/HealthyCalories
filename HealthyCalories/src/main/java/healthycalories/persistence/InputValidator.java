package healthycalories.persistence;

import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s,@]+@[^\\s,@]+\\.[^\\s,@]+$");

    private InputValidator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && !password.trim().isEmpty();
    }

    public static boolean isValidWeight(double weightKg) {
        return Double.isFinite(weightKg) && weightKg > 0 && weightKg <= 500;
    }

    public static boolean isValidServings(double servings) {
        return Double.isFinite(servings) && servings > 0 && servings <= 100;
    }
}
