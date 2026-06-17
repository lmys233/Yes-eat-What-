package com.lotlimys.yeseatwhat.model;

public class IngredientItem implements SelectableItem {
    private int id;
    private String name;
    private String icon;
    private int categoryId;
    private String categoryName;
    private boolean isSelected;
    private boolean isCustom;
    private String imagePath;

    public IngredientItem(int id, String name, String icon, int categoryId,
                          String categoryName, boolean isSelected, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isSelected = isSelected;
        this.isCustom = isCustom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    @Override
    public String getCategoryType() { return "ingredient"; }
}
