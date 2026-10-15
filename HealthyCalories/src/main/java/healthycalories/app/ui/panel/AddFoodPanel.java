package healthycalories.app.ui.panel;

import java.awt.*;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.*;
import healthycalories.model.enumeration.MealType;
import healthycalories.model.item.FoodItem;
import healthycalories.model.log.FoodLog;
import healthycalories.persistence.InputValidator;

/** แผงเพิ่มอาหาร แสดงชื่ออาหาร จำนวน หน่วย แคลอรี่รวม และปุ่มเพิ่มรายการ */
public class AddFoodPanel extends JPanel {
    private final FoodLog foodLog;
    private final Consumer<FoodLog> onSave;
    private final JLabel caloriesLabel = new JLabel("", SwingConstants.CENTER);
    private final JSpinner servings = new JSpinner(
            new SpinnerNumberModel(1.0, 0.1, 100.0, 0.1));

    public AddFoodPanel(FoodItem food, MealType meal, Consumer<FoodLog> onSave) {
        if (food == null || meal == null || onSave == null) {
            throw new IllegalArgumentException("Food, meal, and save action are required");
        }
        this.foodLog = new FoodLog("", food, meal, 1.0);
        this.onSave = onSave;
        buildUI(food);
    }

    private void buildUI(FoodItem food) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel name = new JLabel(food.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Tahoma", Font.BOLD, 16));
        add(name, BorderLayout.NORTH);

        JPanel details = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        details.add(new JLabel("จำนวน"));
        details.add(servings);
        details.add(new JLabel(food.getServingUnit()));
        details.add(caloriesLabel);
        add(details, BorderLayout.CENTER);

        JButton saveButton = new JButton("เพิ่มรายการ");
        saveButton.addActionListener(event -> onSaveToDiary());
        add(saveButton, BorderLayout.SOUTH);

        servings.addChangeListener(event ->
                setServings(((Number) servings.getValue()).doubleValue()));
        updateCaloriesLabel();
    }

    public void setServings(double amount) {
        if (!InputValidator.isValidServings(amount)) {
            throw new IllegalArgumentException("จำนวนที่รับประทานต้องมากกว่า 0");
        }
        foodLog.setServings(amount);
        if (Double.compare(((Number) servings.getValue()).doubleValue(), amount) != 0) {
            servings.setValue(amount);
        }
        updateCaloriesLabel();
    }

    private void updateCaloriesLabel() {
        caloriesLabel.setText(String.format(Locale.ROOT, "%.0f kcal",
                foodLog.getTotalKcal()));
    }

    public FoodLog getFoodLog() {
        return foodLog;
    }

    public void onSaveToDiary() {
        onSave.accept(foodLog);
    }
}
    