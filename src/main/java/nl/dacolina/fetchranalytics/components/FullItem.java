package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;
import java.util.List;

public class FullItem extends Item{

    private List<Category> categories;
    private String displayName;

    public FullItem(String minecraftItemId, String component) {
        super(minecraftItemId, component);
        this.categories = new ArrayList<>();
        this.displayName = craftDisplayName(minecraftItemId);
    }

    public void addCategoryToItem(String categoryName, int categoryWeight) {
        categories.add(new Category(categoryName, categoryWeight));
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    private String craftDisplayName(String minecraftItemId) {
        String cleanString = removeMinecraftFromName(minecraftItemId);

        // Replace underscore with a space
        cleanString = cleanString.replace("_", " ");

        // Capitalize first letter
        cleanString = cleanString.substring(0,1).toUpperCase() + cleanString.substring(1);

        return cleanString;
    }

    private String removeMinecraftFromName(String minecraftItemId) {
        return minecraftItemId.replaceFirst("minecraft:", "");
    }

}
