package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;

public class BingoItem extends Item {

    private int itemID;

    public BingoItem(String minecraftItemId, String component) {
        super(minecraftItemId, component);
       this.itemID = 0;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getItemID () {
        return this.itemID;
    }
}
