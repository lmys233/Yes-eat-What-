package com.lotlimys.yeseatwhat.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "recipe_key")
    private String recipeKey;

    @ColumnInfo(name = "viewed_at")
    private long viewedAt;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "favorited_at")
    private long favoritedAt;

    public History(String recipeKey, long viewedAt) {
        this.recipeKey = recipeKey;
        this.viewedAt = viewedAt;
        this.isFavorite = false;
        this.favoritedAt = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRecipeKey() { return recipeKey; }
    public void setRecipeKey(String recipeKey) { this.recipeKey = recipeKey; }
    public long getViewedAt() { return viewedAt; }
    public void setViewedAt(long viewedAt) { this.viewedAt = viewedAt; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public long getFavoritedAt() { return favoritedAt; }
    public void setFavoritedAt(long favoritedAt) { this.favoritedAt = favoritedAt; }
}
