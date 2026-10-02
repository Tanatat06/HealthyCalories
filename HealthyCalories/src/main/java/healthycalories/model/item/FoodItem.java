package main.java.healthycalories.model.item;

import main.java.healthycalories.model.enumeration.FoodCategory;

public class FoodItem extends Item{
    private FoodCategory category;
    private String servvingUnit;
    private String iconName;

    public FoodItem(String itemId, String name , double kcal)(
        super(itemId,name, kcal, null);
        
    )
    public FoodCategory getCategory(){

    }
    public  String getUnit(){

    }
    public String[] toCsvRow(){

    }
    public void fromCsvRow(String[] row){

    }
    
}