package com.lotlimys.yeseatwhat.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.data.db.entity.GeneratedRecipe;

import java.lang.reflect.Type;
import java.util.List;

public class RecipeItem {
    @SerializedName("recipe_key")
    private String recipeKey;
    private String name;
    @SerializedName("image_path")
    private String imagePath;
    private List<IngredientAmount> ingredients;
    @SerializedName("cooking_method")
    private String cookingMethod;
    @SerializedName("cooking_time")
    private int cookingTime;
    private String difficulty;
    private int calories;
    @SerializedName("cuisine_type")
    private String cuisineType;
    private String region;
    @SerializedName("meal_type")
    private String mealType;
    @SerializedName("steps")
    private List<CookingStep> steps;
    @SerializedName("match_type")
    private String matchType;
    @SerializedName("match_reason")
    private String matchReason;
    @SerializedName("missing_ingredients")
    private List<IngredientAmount> missingIngredients;
    @SerializedName("created_at")
    private long createdAt;
    @SerializedName("is_favorite")
    private boolean isFavorite;

    public static class IngredientAmount {
        private String name;
        private String amount;

        public IngredientAmount() {}

        public IngredientAmount(String name, String amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
    }

    public static class CookingStep {
        private int step;
        private String content;

        public CookingStep() {}

        public CookingStep(int step, String content) {
            this.step = step;
            this.content = content;
        }

        public int getStep() { return step; }
        public void setStep(int step) { this.step = step; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public RecipeItem() {}

    /**
     * 从 GeneratedRecipe（数据库实体）构建 RecipeItem，包含 JSON 字段解析。
     */
    public static RecipeItem fromGeneratedRecipe(GeneratedRecipe recipe, Gson gson) {
        RecipeItem item = new RecipeItem();
        item.setRecipeKey(recipe.getRecipeKey());
        item.setName(recipe.getName());
        item.setImagePath(recipe.getImagePath());
        item.setCookingMethod(recipe.getCookingMethod());
        item.setCookingTime(recipe.getCookingTime());
        item.setDifficulty(recipe.getDifficulty());
        item.setCalories(recipe.getCalories());
        item.setCuisineType(recipe.getCuisineType());
        item.setRegion(recipe.getRegion());
        item.setMealType(recipe.getMealType());
        item.setMatchType(recipe.getMatchType());
        item.setMatchReason(recipe.getMatchReason());

        try {
            Type listType = new TypeToken<List<IngredientAmount>>() {}.getType();
            item.setIngredients(gson.fromJson(recipe.getIngredientsJson(), listType));
        } catch (Exception ignored) {}

        try {
            Type listType = new TypeToken<List<CookingStep>>() {}.getType();
            item.setSteps(gson.fromJson(recipe.getStepsJson(), listType));
        } catch (Exception ignored) {}

        return item;
    }

    /**
     * 提取食材列表的名称摘要（"鸡肉、胡萝卜、土豆"）。
     */
    public static String toIngredientsSummary(List<IngredientAmount> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) sb.append("、");
            sb.append(ingredients.get(i).getName());
        }
        return sb.toString();
    }

    public String getRecipeKey() { return recipeKey; }
    public void setRecipeKey(String recipeKey) { this.recipeKey = recipeKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public List<IngredientAmount> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientAmount> ingredients) { this.ingredients = ingredients; }
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
    public List<CookingStep> getSteps() { return steps; }
    public void setSteps(List<CookingStep> steps) { this.steps = steps; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }
    public List<IngredientAmount> getMissingIngredients() { return missingIngredients; }
    public void setMissingIngredients(List<IngredientAmount> missingIngredients) { this.missingIngredients = missingIngredients; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
