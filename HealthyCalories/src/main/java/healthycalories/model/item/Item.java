package main.java.healthycalories.model.item;

public abstract class Item {
     private String itemId;
    private String name;
    private double kcal;
    
    public Item(String itemId, String name , double kcal){
        this.itemId = itemId;
        this.name = name;
        this.kcal = kcal;
    }
    public String getItemId(){
        return itemId;
    }
    public String getName(){
        return name;
    }
    public double getKcal(){
        return kcal;

    }
    public void setKcal(double kcal){
        this.kcal = kcal;
    }
    public boolean isCustom(){
        return true;
    }
    public void information(){
        return;
    }


    
} 