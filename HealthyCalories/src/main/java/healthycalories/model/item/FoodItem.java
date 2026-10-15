package main.java.healthycalories.model.item;

import java.util.Objects;
import main.java.healthycalories.model.contract.CsvConvertible;
import main.java.healthycalories.model.enumeration.FoodCategory;

public class FoodItem extends Item implements CsvConvertible {
    private FoodCategory category;
    private String servingUnit;
    private String iconName;

    public FoodItem() {
        this("", "", 0, null, "", "");
    }

    public FoodItem(String itemId, String name, double kcal, FoodCategory category,
                    String servingUnit, String iconName) {
        super(itemId, name, kcal);
        this.category = category;
        this.servingUnit = clean(servingUnit);
        this.iconName = clean(iconName);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public String getUnit() {
        return servingUnit;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = clean(servingUnit);
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = clean(iconName);
    }

    @Override
    public boolean isCustom() {
        return category == FoodCategory.CUSTOM;
    }

    @Override
    public void information() {
        super.information();
        System.out.println("Category: " + (category == null ? "" : category.getThaiName())
                + "; serving: " + servingUnit);
    }

    @Override
    public String[] toCsvRow() {
        return new String[] {
            getItemId(),
            getName(),
            Double.toString(getKcal()),
            category == null ? "" : category.name(),
            servingUnit,
            iconName
        };
    }

    @Override
    public void fromCsvRow(String[] row) {
        if (row == null || row.length < 6) {
            throw new IllegalArgumentException("Food item CSV row must contain 6 columns");
        }
        double kcal;
        try {
            kcal = Double.parseDouble(row[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid food calorie value: " + row[2], e);
        }
        FoodCategory parsedCategory = parseCategory(row[3]);
        setItemId(row[0]);
        setName(row[1]);
        setKcal(kcal);
        setCategory(parsedCategory);
        setServingUnit(row[4]);
        setIconName(row[5]);
    }

    private static FoodCategory parseCategory(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String categoryName = value.trim().toUpperCase(java.util.Locale.ROOT);
        if ("VEGETTABLE".equals(categoryName)) {
            categoryName = "VEGETABLE";
        } else if ("FRIUT".equals(categoryName)) {
            categoryName = "FRUIT";
        }
        try {
            return FoodCategory.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown food category: " + value, e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FoodItem)) return false;
        FoodItem foodItem = (FoodItem) other;
        return Objects.equals(getItemId(), foodItem.getItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItemId());
    }
}