package com.lotlimys.yeseatwhat.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "generated_recipe")
public class GeneratedRecipe {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "recipe_key")
    private String recipeKey;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "ingredients_json")
    private String ingredientsJson;

    @ColumnInfo(name = "steps_json")
    private String stepsJson;

    @ColumnInfo(name = "cooking_method")
    private String cookingMethod;

    @ColumnInfo(name = "cooking_time")
    private int cookingTime;

    @ColumnInfo(name = "difficulty")
    private String difficulty;

    @ColumnInfo(name = "calories")
    private int calories;

    @ColumnInfo(name = "cuisine_type")
    private String cuisineType;

    @ColumnInfo(name = "region")
    private String region;

    @ColumnInfo(name = "meal_type")
    private String mealType;

    @ColumnInfo(name = "match_type")
    private String matchType; // EXACT / TRY

    @ColumnInfo(name = "match_reason")
    private String matchReason;

    @ColumnInfo(name = "ai_disclaimer")
    private String aiDisclaimer;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public GeneratedRecipe(String recipeKey, String name, String imagePath,
                           String ingredientsJson, String stepsJson,
                           String cookingMethod, int cookingTime, String difficulty,
                           int calories, String cuisineType, String region,
                           String mealType, String matchType, String matchReason,
                           String aiDisclaimer, long createdAt) {
        this.recipeKey = recipeKey;
        this.name = name;
        this.imagePath = imagePath;
        this.ingredientsJson = ingredientsJson;
        this.stepsJson = stepsJson;
        this.cookingMethod = cookingMethod;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.calories = calories;
        this.cuisineType = cuisineType;
        this.region = region;
        this.mealType = mealType;
        this.matchType = matchType;
        this.matchReason = matchReason;
        this.aiDisclaimer = aiDisclaimer;
        this.createdAt = createdAt;
    }

    public String getRecipeKey() { return recipeKey; }
    public void setRecipeKey(String recipeKey) { this.recipeKey = recipeKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public String getIngredientsJson() { return ingredientsJson; }
    public void setIngredientsJson(String ingredientsJson) { this.ingredientsJson = ingredientsJson; }
    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }
    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public String getAiDisclaimer() { return aiDisclaimer; }
    public void setAiDisclaimer(String aiDisclaimer) { this.aiDisclaimer = aiDisclaimer; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
