package nl.dacolina.fetchranalytics.onstartup;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.util.Objects;

public class Items {

    public static String[] getItemsFromGame(MinecraftServer server) {

        String[] tempArray = {};

        // Load the items storage
        Identifier storageId = Identifier.of("fetchr", "items");

        NbtCompound nbt = server.getDataCommandStorage().get(storageId);

        NbtList activeItems = nbt.getList("active_items", NbtElement.COMPOUND_TYPE);

        FetchrAnalytics.LOGGER.info(String.valueOf(activeItems.size()));

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

                            itemId += partsOfItemChecks;
                        }


                    }

                } else {
                    itemId += " -- " + components;
                }



            }

            // Check if item has a trim

//            if(components != null && components.contains("minecraft:trim")) {
//
//                NbtCompound trimProperties = components.getCompound("minecraft:trim");
//
//                String material = trimProperties.getString("material");
//
//                String pattern = trimProperties.getString("pattern");
//
//                itemId += ".trim." + getNameWithoutMinecraftPrefix(material) + '.' + getNameWithoutMinecraftPrefix(pattern);
//            }

            for (int j = 0; j < categories.size(); j++) {
                NbtCompound firstCategory = categories.getCompound(j);

                String categoryName = firstCategory.getString("id");

                int categoryWeight = firstCategory.getInt("weight");

                FetchrAnalytics.LOGGER.info(itemId + " -- " + categoryName + " -- " + categoryWeight);

            }

        }


        return tempArray;
    }

    private static String getNameWithoutMinecraftPrefix(String fullName) {
        String[] temp = fullName.split(":");

        return temp[1];

    }


}


