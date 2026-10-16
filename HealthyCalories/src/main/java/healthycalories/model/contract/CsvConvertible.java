package main.java.healthycalories.model.contract;

/** สัญญาสำหรับแปลงข้อมูลโมเดลไปและกลับจากคอลัมน์ CSV */
public interface CsvConvertible {
    /** แปลงสถานะปัจจุบันเป็นคอลัมน์สำหรับหนึ่งแถว CSV */
    String[] toCsvRow();
    /** นำค่าจากคอลัมน์ CSV มาแทนที่สถานะของออบเจ็กต์ */
    void fromCsvRow(String[] row);
}