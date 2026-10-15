package main.java.healthycalories.model.item;

public abstract class Item {
    private String itemId;
    private String name;
    private double kcal;

    protected Item(String itemId, String name, double kcal) {
        this.itemId = clean(itemId);
        this.name = clean(name);
        setKcal(kcal);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = clean(itemId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = clean(name);
    }

    public double getKcal() {
        return kcal;
    }

    public void setKcal(double kcal) {
        if (!Double.isFinite(kcal) || kcal < 0) {
            throw new IllegalArgumentException("Calories must be a finite non-negative value");
        }
        this.kcal = kcal;
    }

    public boolean isCustom() {
        return false;
    }

    public void information() {
        System.out.println(name + " (" + itemId + "): " + kcal + " kcal");
    }
}