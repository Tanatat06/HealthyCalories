package main.java.healthycalories.app.ui.dialog;

import javax.swing.*;
import java.awt.Component;

/** กล่องยืนยันแบบใช่/ไม่ใช่ และคืนผลว่าผู้ใช้กดยืนยันหรือไม่ */
public class ConfirmDialog {
    public boolean show(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent, message, "ยืนยัน", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
}