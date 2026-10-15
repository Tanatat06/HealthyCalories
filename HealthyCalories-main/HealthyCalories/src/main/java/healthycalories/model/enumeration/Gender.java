package main.java.healthycalories.model.enumeration;
public enum Gender {
    MALE("ชาย"), FEMALE("หญิง"), OTHER("อื่นๆ");

    private final String thaiName;

    Gender(String thaiName) {
        this.thaiName = thaiName;
    }
    public String getThaiName() {
        return thaiName;
    }
}