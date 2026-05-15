package com.lotlimys.yeseatwhat.model;

public class CuisineItem implements SelectableItem {
    private int id;
    private String name;
    private String iconName;
    private int categoryId;
    private boolean isSelected;
    private String categoryType; // "domestic" / "international" / "method"

    public CuisineItem(int id, String name, String iconName, int categoryId,
                       boolean isSelected, String categoryType) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.categoryId = categoryId;
        this.isSelected = isSelected;
        this.categoryType = categoryType;
    }

    @Override
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @Override
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    @Override
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    @Override
    public boolean isSelected() { return isSelected; }
    @Override
    public void setSelected(boolean selected) { isSelected = selected; }

    @Override
    public String getCategoryType() { return categoryType; }
    public void setCategoryType(String categoryType) { this.categoryType = categoryType; }
}
