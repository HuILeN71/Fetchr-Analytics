package nl.dacolina.fetchranalytics.components;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.util.ArrayList;
import java.util.List;

public class BingoCard {
    private static final int DEFAULT_ITEM_AMOUNT = 25;

    private List<Item> items;

    public BingoCard(MinecraftServer server) {
        this.items = getItemsFromCard(server);

        debugShowCard(items);
    }

    private static List<Item> getItemsFromCard(MinecraftServer server) {

        List<Item> listOfItems = new ArrayList<>();

        //Load the items storage
        Identifier storageId = Identifier.of("fetchr", "card");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList cardItems = nbt.getList("slots", NbtElement.COMPOUND_TYPE);

        for(int i = 0; i < DEFAULT_ITEM_AMOUNT; i++) {
            NbtCompound cardItem = cardItems.getCompound(i);

            NbtCompound item = cardItem.getCompound("item");

            NbtCompound components = item.getCompound("components");

            String itemId = item.getString("id");

            // Check if item has components

            // Add checks to see if items have these compounds, otherwise delete them. !YET TO ADD
            components.remove("minecraft:custom_name");
            components.remove("minecraft:lore");

            Item newItem = null;

            if (item.contains("components", NbtElement.COMPOUND_TYPE) && !components.isEmpty()) {

                if(cardItem.contains("item_tests", NbtElement.LIST_TYPE)) {
                    NbtList itemTests = cardItem.getList("item_tests", NbtElement.COMPOUND_TYPE);

                    NbtCompound normalizedActiveTests = itemTests.getCompound(0);

                    String checkedComponent = normalizedActiveTests.getString("id");

                    if (components.contains(checkedComponent)) {

                        // Parts of the item checks
                        NbtCompound partsOfItemChecks = normalizedActiveTests.getCompound("predicate");

                        // Check if armor trim pattern is being checked. Manual for now, dont yet know how to implement
                        // a way to make this completely dynamic. For now manual controls will be added to this section.

                        FetchrAnalytics.LOGGER.debug(checkedComponent);

                        String engeString = "{\"" + checkedComponent + "\":" + partsOfItemChecks + "}";

//                        if(Objects.equals(checkedComponent, "minecraft:trim") && !partsOfItemChecks.contains("pattern")) {
//                            // Add any value so that database knows the difference between this item and one where a
//                            // certain trim is required. Also used to determine display name.
//
//                            partsOfItemChecks.putString("pattern", "minecraft:any");
//
//                        }

                        newItem = new Item(itemId, engeString);

                    }

                } else {
                    newItem = new Item(itemId, components.toString());
                }

            } else {
                newItem = new Item(itemId, null);
            }

            listOfItems.add(newItem);
        }
        return listOfItems;
    }

    public void debugShowCard(List<Item> items) {
        for (Item itemFromList : items) {

            StringBuilder s = new StringBuilder();

            s.append(itemFromList.getMinecraftItemName());

            if(itemFromList.getComponent() != null) {
                s.append(itemFromList.getComponent());
            }

            FetchrAnalytics.LOGGER.info(s.toString());

        }
    }

}


