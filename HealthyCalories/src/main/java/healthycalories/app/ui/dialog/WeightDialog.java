package main.java.healthycalories.app.ui.dialog;

import javax.swing.*;
import java.awt.Component;

public class WeightDialog {
    private final Component parent;

    public WeightDialog(Component parent){
        this.parent = parent;
    }
    public Double showInput(){
        String input = JOptionPane.showInputDialog(parent,"กรอกน้ำหนัก(กิโลกรัม)");
        if(input == null) return null; 
        try {double weight = Double.parseDouble(input.trim());
            if(weight <=0) throw new NumberFormatException();
            return weight;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,"กรุณากรอกน้ำหนักให้ถูกต้อง");
            return null;
            // TODO: handle exception
        }
    }
}
