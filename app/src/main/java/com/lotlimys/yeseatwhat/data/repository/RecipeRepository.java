package com.lotlimys.yeseatwhat.data.repository;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.ai.AIService;
import com.lotlimys.yeseatwhat.ai.DashScopeService;
import com.lotlimys.yeseatwhat.ai.OpenAIService;
import com.lotlimys.yeseatwhat.ai.RecipeRequest;
import com.lotlimys.yeseatwhat.ai.RecipeResponse;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.dao.RecipeDao;
import com.lotlimys.yeseatwhat.data.db.entity.GeneratedRecipe;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.model.RecipeItem;
import com.lotlimys.yeseatwhat.util.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {
    private final RecipeDao recipeDao;
    private final AIService aiService;
    private final Gson gson;

    public RecipeRepository(Context context) {
        this.recipeDao = AppDatabase.getInstance(context).recipeDao();
        AppPreferences prefs = AppPreferences.getInstance(context);
        // 根据供应商选择 AI 服务实现
        if (Constants.PROVIDER_QWEN.equals(prefs.getAiProvider())) {
            this.aiService = new DashScopeService(prefs);
        } else {
            this.aiService = new OpenAIService(prefs);
        }
        this.gson = new Gson();
    }

    public void generateRecipes(RecipeRequest request, AIService.Callback<RecipeResponse> callback) {
        aiService.generateRecipes(request, new AIService.Callback<RecipeResponse>() {
            @Override
            public void onSuccess(RecipeResponse result) {
                if (result.getDishes() != null) {
                    saveRecipes(result.getDishes());
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public GeneratedRecipe getRecipeDetail(String recipeKey) {
        return recipeDao.getByKey(recipeKey);
    }

    public void generateRandomDishes(AIService.Callback<List<RecipeItem>> callback) {
        aiService.generateRandomDishes(new ArrayList<>(), callback);
    }

    public void generateRegionRecommendation(String region, AIService.Callback<List<RecipeItem>> callback) {
        aiService.generateRegionRecommendation(region, callback);
    }

    private void saveRecipes(List<RecipeItem> items) {
        if (items == null) return;
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            for (RecipeItem item : items) {
                GeneratedRecipe recipe = new GeneratedRecipe(
                        item.getRecipeKey(),
                        item.getName(),
                        item.getImagePath(),
                        gson.toJson(item.getIngredients()),
                        gson.toJson(item.getSteps()), // steps
                        item.getCookingMethod(),
                        item.getCookingTime(),
                        item.getDifficulty(),
                        item.getCalories(),
                        item.getCuisineType(),
                        item.getRegion(),
                        item.getMealType(),
                        item.getMatchType(),
                        item.getMatchReason(),
                        "本食谱由AI生成，仅供参考。烹饪时间与食材用量可能因实际情况而异。",
                        System.currentTimeMillis()
                );
                recipeDao.insert(recipe);
            }
        });
    }
}
