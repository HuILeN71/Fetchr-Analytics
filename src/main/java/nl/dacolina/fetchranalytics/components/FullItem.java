package nl.dacolina.fetchranalytics.components;

import java.util.ArrayList;
import java.util.List;

public class FullItem extends Item{

    private List<Category> categories;

    public FullItem(String minecraftItemId, String component) {
        super(minecraftItemId, component);
        this.categories = new ArrayList<>();
    }

    public void addCategoryToItem(Category category) {
        categories.add(category);
    }

    public List<Category> getCategories() {
        return categories;
    }
}
