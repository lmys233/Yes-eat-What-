package com.lotlimys.yeseatwhat.ai;

import com.lotlimys.yeseatwhat.model.RecipeItem;

import java.util.List;

public interface AIService {
    void generateRecipes(RecipeRequest request, Callback<RecipeResponse> callback);

    void generateRecipeImage(RecipeItem recipe, Callback<String> callback);

    void generateRegionRecommendation(String region, Callback<List<RecipeItem>> callback);

    void generateRandomDishes(List<Integer> ingredientIds, Callback<List<RecipeItem>> callback);

    void generatePortrait(PortraitRequest request, Callback<String> callback);

    void generateIngredientImage(String ingredientName, String categoryName, Callback<String> callback);

    void chat(List<ChatMessage> history, Callback<String> callback);

    class ChatMessage {
        private final String role;
        private final String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public String getContent() { return content; }
    }

    interface Callback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}
