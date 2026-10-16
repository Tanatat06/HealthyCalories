package main.java.healthycalories.app.ui.dialog;

import java.awt.Component;
import javax.swing.JOptionPane;
import main.java.healthycalories.persistence.InputValidator;

/** กล่องรับค่าน้ำหนัก ตรวจสอบให้อยู่ในช่วงที่ยอมรับได้ก่อนคืนค่า */
public class WeightDialog {
    private final Component parent;

    /** ผูกกล่องป้อนน้ำหนักกับคอมโพเนนต์เจ้าของที่จะแสดงข้อความ */
    public WeightDialog(Component parent) {
        this.parent = parent;
    }

    /** รับน้ำหนักจนกว่าจะป้อนค่าถูกต้อง หรือคืน null เมื่อผู้ใช้ยกเลิก */
    public Double showInput() {
        while (true) {
            String input = JOptionPane.showInputDialog(parent,
                    "กรอกน้ำหนัก (กิโลกรัม)");
            if (input == null) {
                return null;
            }
            try {
                double weight = Double.parseDouble(input.trim());
                if (InputValidator.isValidWeight(weight)) {
                    return weight;
                }
            } catch (NumberFormatException e) {
                // Show the same validation message for non-numeric values.
            }
            JOptionPane.showMessageDialog(parent,
                    "กรุณากรอกน้ำหนักมากกว่า 0 และไม่เกิน 500 กิโลกรัม",
                    "น้ำหนักไม่ถูกต้อง", JOptionPane.ERROR_MESSAGE);
        }
    }
}
