package nl.dacolina.fetchranalytics.components;

public class Category {

    private String name;
    private int weight;

    public Category(String categoryName, int categoryWeight) {
        this.name = categoryName;
        this.weight = categoryWeight;
    }

    public String getCategoryName() {
        return this.name;
    }

    public int getCategoryWeight() {
        return this.weight;
    }

}
