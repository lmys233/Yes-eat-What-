package com.lotlimys.yeseatwhat.model;

public interface SelectableItem {
    int getId();
    String getName();
    boolean isSelected();
    void setSelected(boolean selected);
    int getCategoryId();
    String getCategoryType();
}
