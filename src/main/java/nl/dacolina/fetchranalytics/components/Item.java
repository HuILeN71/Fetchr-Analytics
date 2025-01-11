package nl.dacolina.fetchranalytics.components;

// This class is used for the base of the items, as for the bingo items different properties are required than
// during the startup checks

public class Item {

    private String minecraftItemName;
    private String component;

    public Item(String itemName, String component ) {
        this.minecraftItemName = itemName;
        this.component = component;
    }



}
