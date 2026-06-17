package com.lotlimys.yeseatwhat.ai;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecipeRequest {
    @SerializedName("ingredient_ids")
    private List<Integer> ingredientIds;

    @SerializedName("ingredient_names")
    private List<String> ingredientNames;

    @SerializedName("cuisine_type")
    private String cuisineType;

    @SerializedName("meal_type")
    private String mealType;

    @SerializedName("dietary_restrictions")
    private List<String> dietaryRestrictions;

    @SerializedName("allergies")
    private List<String> allergies;

    @SerializedName("others")
    private List<String> others;

    @SerializedName("self_description")
    private String selfDescription;

    @SerializedName("cooking_methods")
    private List<String> cookingMethods;

    @SerializedName("preferences")
    private String preferences;

    @SerializedName("diet_goal")
    private String dietGoal;

    public RecipeRequest(List<Integer> ingredientIds, List<String> ingredientNames,
                         String cuisineType, String mealType,
                         List<String> dietaryRestrictions, List<String> allergies,
                         List<String> others, String selfDescription,
                         List<String> cookingMethods, String preferences,
                         String dietGoal) {
        this.ingredientIds = ingredientIds;
        this.ingredientNames = ingredientNames;
        this.cuisineType = cuisineType;
        this.mealType = mealType;
        this.dietaryRestrictions = dietaryRestrictions;
        this.allergies = allergies;
        this.others = others;
        this.selfDescription = selfDescription;
        this.cookingMethods = cookingMethods;
        this.preferences = preferences;
        this.dietGoal = dietGoal;
    }

    public List<String> getCookingMethods() { return cookingMethods; }
    public void setCookingMethods(List<String> cookingMethods) { this.cookingMethods = cookingMethods; }
    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public List<Integer> getIngredientIds() { return ingredientIds; }
    public void setIngredientIds(List<Integer> ingredientIds) { this.ingredientIds = ingredientIds; }
    public List<String> getIngredientNames() { return ingredientNames; }
    public void setIngredientNames(List<String> ingredientNames) { this.ingredientNames = ingredientNames; }
    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    public List<String> getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(List<String> dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }
    public List<String> getAllergies() { return allergies; }
    public void setAllergies(List<String> allergies) { this.allergies = allergies; }
    public List<String> getOthers() { return others; }
    public void setOthers(List<String> others) { this.others = others; }
    public String getSelfDescription() { return selfDescription; }
    public void setSelfDescription(String selfDescription) { this.selfDescription = selfDescription; }
    public String getDietGoal() { return dietGoal; }
    public void setDietGoal(String dietGoal) { this.dietGoal = dietGoal; }
}
