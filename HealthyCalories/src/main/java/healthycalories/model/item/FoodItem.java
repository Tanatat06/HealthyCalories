package main.java.healthycalories.model.item;

import main.java.healthycalories.model.enumeration.FoodCategory;

public class FoodItem extends Item{
    private FoodCategory category;
    private String servingUnit;
    private String iconName;

    public FoodItem(String itemId, String name , double kcal,FoodCategory category, String servingUnit , String iconName){
        super(itemId ,name, kcal);
        this.category = category;
        this.servingUnit = servingUnit;
        this.iconName = iconName;
    }    
    public FoodCategory getCategory(){
        return category;
    }
    public  String getUnit(){
        return servingUnit;
    }
    public String[] toCsvRow(){
        return null;
        
    }
    public void fromCsvRow(String[] row){

    }
    
}