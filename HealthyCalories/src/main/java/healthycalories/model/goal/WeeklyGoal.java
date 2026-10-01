package main.java.healthycalories.model.goal;

public enum WeeklyGoal {
    VERY_FAST(1.0, "แบบรวดเร็วมาก"), 
    FAST(0.5, "แบบเร็ว"), 
    NORMAL(0.33, "แบบทำธรรมดา(แนะนำ)"), 
    SLOW(0.25, "แบบค่อยๆ");

   
    private final double kgPerWeek;
    private final String thaiName;

    
    WeeklyGoal(double kgPerWeek, String thaiName) {
        this.kgPerWeek = kgPerWeek;
        this.thaiName = thaiName;
    }

  
    public double getKgPerWeek() {
        return kgPerWeek;
    }

    public String getThaiName() {
        return thaiName;
    }

    
    public static WeeklyGoal fromValue(double kg) {
        for (WeeklyGoal goal : values()) {
            if (Double.compare(goal.kgPerWeek, kg) == 0) {
                return goal;
            }
        }
        return null;
    }
}