package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;
import java.util.List;

public class FullItem extends Item{

    private List<Category> categories;

    public FullItem(String minecraftItemId, String component) {
        super(minecraftItemId, component);
        this.categories = new ArrayList<>();
    }

    public void addCategoryToItem(String categoryName, int categoryWeight) {
        categories.add(new Category(categoryName, categoryWeight));
    }

    public List<Category> getCategories() {
        return categories;
    }
}
