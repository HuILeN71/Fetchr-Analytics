package nl.dacolina.fetchranalytics.managers;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;
import nl.dacolina.fetchranalytics.components.FullItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemManager {

    private List<FullItem> itemsCurrentlyInGame;
    private List<FullItem> itemsCurrentlyInDatabase;

    public ItemManager(MinecraftServer server) {

        this.itemsCurrentlyInGame = getItemsLoadedInGame(server);

        //FetchrAnalytics.LOGGER.info(this.itemsCurrentlyInGame.toString());





    }

    private static String getNameWithoutMinecraftPrefix(String fullName) {
        String[] temp = fullName.split(":");

        return temp[1];

    }

    private static List<FullItem> getItemsLoadedInGame(MinecraftServer server) {

        List<FullItem> listOfItems = new ArrayList<>();

//        FullItem item = new FullItem("minecraft:bucket", "{poison: \"stront\"}");
//        item.addCategoryToItem("fetchr:buckets", 2);
//
//        listOfItems.add(item);

//        for (FullItem itemFromList : listOfItems) {
//            System.out.println(itemFromList.getMinecraftItemName() + itemFromList.getComponent() + " - " + itemFromList.getCategories().getFirst().getCategoryName() + ", " + itemFromList.getCategories().getFirst().getCategoryWeight());

        //Load the items storage
        Identifier storageId = Identifier.of("fetchr", "items");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList activeItems = nbt.getList("active_items", NbtElement.COMPOUND_TYPE);

        //FetchrAnalytics.LOGGER.info(String.valueOf(activeItems.size()));

        for(int i = 0; i < activeItems.size(); i++) {
            NbtCompound activeItem = activeItems.getCompound(i);

            NbtCompound item = activeItem.getCompound("item");

            NbtList categories = activeItem.getList("categories", NbtElement.COMPOUND_TYPE);

            NbtCompound components = item.getCompound("components");

            String itemId = item.getString("id");

            // Check if item has components

            // Add checks to see if items have these compounds, otherwise delete them. !YET TO ADD
            components.remove("minecraft:custom_name");
            components.remove("minecraft:lore");

            FullItem newItem = null;

            if (components != null && item.contains("components", NbtElement.COMPOUND_TYPE) && !components.isEmpty()) {

                if(activeItem.contains("item_tests", NbtElement.LIST_TYPE)) {
                    NbtList itemTests = activeItem.getList("item_tests", NbtElement.COMPOUND_TYPE);

                    NbtCompound normalizedActiveTests = itemTests.getCompound(0);

                    String checkedComponent = normalizedActiveTests.getString("id");

                    if (components.contains(checkedComponent)) {

                        // Parts of the item checks
                        NbtCompound partsOfItemChecks = normalizedActiveTests.getCompound("predicate");

                        // Check if armor trim pattern is being checked. Manual for now, dont yet know how to implement
                        // a way to make this completely dynamic. For now manual controls will be added to this section.

                        FetchrAnalytics.LOGGER.info(checkedComponent);

                        if(Objects.equals(checkedComponent, "minecraft:trim") && !partsOfItemChecks.contains("pattern")) {
                            // Add any value so that database knows the difference between this item and one where a
                            // certain trim is required. Also used to determine display name.

                            partsOfItemChecks.putString("pattern", "minecraft:any");

                        }

                        newItem = new FullItem(itemId, partsOfItemChecks.toString());

                    }

                } else {
                    newItem = new FullItem(itemId, components.toString());
                }

            } else {
                newItem = new FullItem(itemId, null);
            }

            for (int j = 0; j < categories.size(); j++) {
                NbtCompound currentCategory = categories.getCompound(j);

                String categoryName = currentCategory.getString("id");

                int categoryWeight = currentCategory.getInt("weight");

                newItem.addCategoryToItem(categoryName, categoryWeight);

                FetchrAnalytics.LOGGER.info(itemId + " -- " + categoryName + " -- " + categoryWeight);

            }

        }

        return listOfItems;
    }

}

