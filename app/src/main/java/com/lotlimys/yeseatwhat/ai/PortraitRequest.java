package com.lotlimys.yeseatwhat.ai;

import java.util.List;

public class PortraitRequest {
    private List<String> historyRecipeKeys;
    private List<String> favoriteRecipeKeys;
    private String selfDescription;
    private List<String> historyRecipeNames;
    private List<String> favoriteRecipeNames;
    private String previousPortrait;

    public PortraitRequest(List<String> historyRecipeKeys, List<String> favoriteRecipeKeys,
                           String selfDescription) {
        this.historyRecipeKeys = historyRecipeKeys;
        this.favoriteRecipeKeys = favoriteRecipeKeys;
        this.selfDescription = selfDescription;
    }

    public PortraitRequest(List<String> historyRecipeKeys, List<String> favoriteRecipeKeys,
                           String selfDescription, List<String> historyRecipeNames,
                           List<String> favoriteRecipeNames, String previousPortrait) {
        this.historyRecipeKeys = historyRecipeKeys;
        this.favoriteRecipeKeys = favoriteRecipeKeys;
        this.selfDescription = selfDescription;
        this.historyRecipeNames = historyRecipeNames;
        this.favoriteRecipeNames = favoriteRecipeNames;
        this.previousPortrait = previousPortrait;
    }

    public List<String> getHistoryRecipeKeys() { return historyRecipeKeys; }
    public void setHistoryRecipeKeys(List<String> historyRecipeKeys) { this.historyRecipeKeys = historyRecipeKeys; }
    public List<String> getFavoriteRecipeKeys() { return favoriteRecipeKeys; }
    public void setFavoriteRecipeKeys(List<String> favoriteRecipeKeys) { this.favoriteRecipeKeys = favoriteRecipeKeys; }
    public String getSelfDescription() { return selfDescription; }
    public void setSelfDescription(String selfDescription) { this.selfDescription = selfDescription; }
    public List<String> getHistoryRecipeNames() { return historyRecipeNames; }
    public void setHistoryRecipeNames(List<String> historyRecipeNames) { this.historyRecipeNames = historyRecipeNames; }
    public List<String> getFavoriteRecipeNames() { return favoriteRecipeNames; }
    public void setFavoriteRecipeNames(List<String> favoriteRecipeNames) { this.favoriteRecipeNames = favoriteRecipeNames; }
    public String getPreviousPortrait() { return previousPortrait; }
    public void setPreviousPortrait(String previousPortrait) { this.previousPortrait = previousPortrait; }
}
