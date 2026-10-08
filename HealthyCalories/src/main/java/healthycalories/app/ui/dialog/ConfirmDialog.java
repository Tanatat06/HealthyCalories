package main.java.healthycalories.app.ui.dialog;

import javax.swing.*;
import java.awt.Component;

public class ConfirmDialog {
    public boolean show(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(
                parent, message, "ยืนยัน", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
}