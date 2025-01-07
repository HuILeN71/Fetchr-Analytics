package nl.dacolina.fetchranalytics.onstartup;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

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

            FetchrAnalytics.LOGGER.info(String.valueOf(i));

            NbtCompound item = activeItem.getCompound("item");

            // Check for trimmed

            String itemTests = null;

            if(activeItem != null && activeItem.contains("active_tests")) {
                NbtCompound item_tests = activeItem.getCompound("active_tests");

                //item_tests = item_tests.getCompound(0);

            }


            NbtList categories = activeItem.getList("categories", NbtElement.COMPOUND_TYPE);

            NbtCompound components = item.getCompound("components");

            String itemId = item.getString("id");


            // Check if item has potion components

            components.remove("minecraft:custom_name");
            components.remove("minecraft:lore");

            if (components != null && item.contains("components", NbtElement.COMPOUND_TYPE) && !components.isEmpty()) {

                if (components.) {

                }
                itemId += " -- " + components;

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


