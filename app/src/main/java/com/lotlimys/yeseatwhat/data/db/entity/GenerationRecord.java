package com.lotlimys.yeseatwhat.data.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "generation_record")
public class GenerationRecord {
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "ingredients")
    private String ingredients;

    @ColumnInfo(name = "cooking_methods")
    private String cookingMethods;

    @ColumnInfo(name = "cuisines")
    private String cuisines;

    @ColumnInfo(name = "meal_types")
    private String mealTypes;

    @ColumnInfo(name = "preferences")
    private String preferences;

    @ColumnInfo(name = "prompt_text")
    private String promptText;

    @ColumnInfo(name = "dish_count")
    private int dishCount;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "completed_at")
    private long completedAt;

    @ColumnInfo(name = "success")
    private boolean success;

    public GenerationRecord(long id, String ingredients, String cookingMethods,
                            String cuisines, String mealTypes, String preferences,
                            String promptText, int dishCount, long createdAt,
                            long completedAt, boolean success) {
        this.id = id;
        this.ingredients = ingredients;
        this.cookingMethods = cookingMethods;
        this.cuisines = cuisines;
        this.mealTypes = mealTypes;
        this.preferences = preferences;
        this.promptText = promptText;
        this.dishCount = dishCount;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.success = success;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public String getCookingMethods() { return cookingMethods; }
    public void setCookingMethods(String cookingMethods) { this.cookingMethods = cookingMethods; }
    public String getCuisines() { return cuisines; }
    public void setCuisines(String cuisines) { this.cuisines = cuisines; }
    public String getMealTypes() { return mealTypes; }
    public void setMealTypes(String mealTypes) { this.mealTypes = mealTypes; }
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }
    public int getDishCount() { return dishCount; }
    public void setDishCount(int dishCount) { this.dishCount = dishCount; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
