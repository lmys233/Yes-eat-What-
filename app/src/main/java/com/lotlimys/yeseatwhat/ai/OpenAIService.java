package com.lotlimys.yeseatwhat.ai;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.model.RecipeItem;
import com.lotlimys.yeseatwhat.util.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OpenAIService implements AIService {

    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient client;
    private final Gson gson;
    private final AppPreferences preferences;

    public OpenAIService(AppPreferences preferences) {
        this.preferences = preferences;
        this.gson = new GsonBuilder().create();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Constants.AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(Constants.AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(Constants.AI_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void generateRecipes(RecipeRequest request, Callback<RecipeResponse> callback) {
        String systemPrompt = buildRecipeSystemPrompt(request);
        String userPrompt = buildRecipeUserPrompt(request);
        sendChatCompletion(systemPrompt, userPrompt, callback, RecipeResponse.class);
    }

    @Override
    public void generateRecipeImage(RecipeItem recipe, Callback<String> callback) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", Constants.DEFAULT_IMAGE_MODEL);
                body.addProperty("prompt", "Cartoon style food illustration of " + recipe.getName()
                        + ", vibrant colors, cute style, white background, professional food photography");
                body.addProperty("n", 1);
                body.addProperty("size", "1024x1024");

                String json = gson.toJson(body);
                String apiUrl = preferences.getApiUrl().replace("/chat/completions", "")
                        .replace("/v1", "") + "/v1/images/generations";

                Request httpRequest = buildRequest(apiUrl, json);
                try (Response response = client.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        callback.onError("图片生成失败: " + response.code());
                        return;
                    }
                    String respBody = response.body().string();
                    JsonObject respJson = gson.fromJson(respBody, JsonObject.class);
                    String imageUrl = respJson.getAsJsonArray("data")
                            .get(0).getAsJsonObject()
                            .get("url").getAsString();
                    callback.onSuccess(imageUrl);
                }
            } catch (Exception e) {
                callback.onError("图片生成失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void generateRegionRecommendation(String region, Callback<List<RecipeItem>> callback) {
        String systemPrompt = "你是一个美食推荐专家。根据用户提供的地区，推荐该地区的特色菜品。"
                + "请以JSON数组格式返回，每个菜品包含: name, cooking_method, cooking_time(分钟), "
                + "difficulty(简单/中等/困难), calories, cuisine_type, region, meal_type。"
                + "最多推荐3道菜。";
        String userPrompt = "请推荐" + region + "的特色菜品，共3道。";
        sendChatCompletion(systemPrompt, userPrompt, callback,
                new TypeToken<List<RecipeItem>>(){}.getType());
    }

    @Override
    public void generateRandomDishes(List<Integer> ingredientIds, Callback<List<RecipeItem>> callback) {
        String systemPrompt = "你是一个创意美食专家。根据用户提供的食材ID列表，随机生成3道创意菜品。"
                + "请以JSON数组格式返回，每个菜品包含: name, cooking_method, cooking_time(分钟), "
                + "difficulty(简单/中等/困难), calories, cuisine_type, region, meal_type。";
        String userPrompt = "食材IDs: " + gson.toJson(ingredientIds) + "，请随机生成3道菜品。";
        sendChatCompletion(systemPrompt, userPrompt, callback,
                new TypeToken<List<RecipeItem>>(){}.getType());
    }

    @Override
    public void generatePortrait(PortraitRequest request, Callback<String> callback) {
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("你是一个说话嘴毒但一针见血的用户画像分析专家。")
                .append("根据用户的浏览历史、收藏和自我介绍，分析用户的饮食偏好和特点，")
                .append("生成一段毒舌但有趣的用户画像描述。\n\n")
                .append("要求：\n")
                .append("1. 用词犀利幽默，一针见血地指出用户的饮食偏好和\"吃货性格\"\n")
                .append("2. 不要只罗列用户吃过什么菜，而要分析背后的饮食性格\n")
                .append("3. 尽量结合菜品名称来吐槽，显得有理有据\n")
                .append("4. 字数控制在100-200字之间\n")
                .append("5. 最后总结一句：感觉你是那种喜欢吃……的人，省略号处用一个精辟的词语概括，例如：方便面爱好者、路边摊达人等，总之要一针见血\n")
                .append("6. 如果用户有自我介绍，可以结合进来一起吐槽\n")
                .append("7. 如果提供了上一次的画像，可以对比一下用户有没有\"进步\"\n");

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("近3天浏览的菜品: ")
                .append(request.getHistoryRecipeNames() != null && !request.getHistoryRecipeNames().isEmpty()
                        ? String.join("、", request.getHistoryRecipeNames()) : "暂无")
                .append("\n收藏的菜品: ")
                .append(request.getFavoriteRecipeNames() != null && !request.getFavoriteRecipeNames().isEmpty()
                        ? String.join("、", request.getFavoriteRecipeNames()) : "暂无")
                .append("\n自我介绍: ")
                .append(request.getSelfDescription() != null && !request.getSelfDescription().isEmpty()
                        ? request.getSelfDescription() : "无")
                .append("\n上一次画像: ")
                .append(request.getPreviousPortrait() != null && !request.getPreviousPortrait().isEmpty()
                        ? request.getPreviousPortrait() : "无");

        sendChatCompletionRaw(systemPrompt.toString(), userPrompt.toString(), callback);
    }

    @Override
    public void generateIngredientImage(String ingredientName, String categoryName, Callback<String> callback) {
        new Thread(() -> {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("model", Constants.DEFAULT_IMAGE_MODEL);
                body.addProperty("prompt", "Pixel art game item icon of " + ingredientName
                        + ", category: " + (categoryName != null ? categoryName : "")
                        + ", simple retro sprite style, pure white background (#FFFFFF),"
                        + " absolutely NO shadows, NO gradients, NO black borders or outlines around the ingredient,"
                        + " the ingredient must not have any dark edge outline,"
                        + " everything except the ingredient must be pure white,"
                        + " low resolution pixel art, centered single item, no text or decorations");
                body.addProperty("n", 1);
                body.addProperty("size", "1024x1024");

                String json = gson.toJson(body);
                String apiUrl = preferences.getApiUrl().replace("/chat/completions", "")
                        .replace("/v1", "") + "/v1/images/generations";
                Log.d("OpenAIImg", "请求URL: " + apiUrl);
                Log.d("OpenAIImg", "请求body: " + json);

                Request httpRequest = buildRequest(apiUrl, json);
                try (Response response = client.newCall(httpRequest).execute()) {
                    String respBodyStr = response.body() != null ? response.body().string() : "";
                    Log.d("OpenAIImg", "响应 code=" + response.code() + " body=" + respBodyStr);
                    if (!response.isSuccessful()) {
                        callback.onError("食材图标生成失败: " + response.code() + " " + respBodyStr);
                        return;
                    }
                    JsonObject respJson = gson.fromJson(respBodyStr, JsonObject.class);
                    String imageUrl = respJson.getAsJsonArray("data")
                            .get(0).getAsJsonObject()
                            .get("url").getAsString();
                    callback.onSuccess(imageUrl);
                }
            } catch (Exception e) {
                callback.onError("食材图标生成失败: " + e.getMessage());
            }
        }).start();
    }

    public static String buildRecipeSystemPrompt(RecipeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的食谱推荐AI。根据用户提供的食材，优先推荐当前世界上真实存在的主流菜品，")
                .append("这些菜品需要有公认的食谱和一定的群众基础，能够被商业化销售。")
                .append("注意菜系多样性：如果用户没有指定偏好菜系，请根据食材特点匹配最合适的菜系——")
                .append("例如鸡腿可以推荐美式炸鸡、日式照烧鸡腿等，牛油果可以推荐墨西哥卷饼、考伯沙拉等。")
                .append("不要默认只推荐中式菜品，应该从全球各菜系（中式、日式、韩式、美式、意式、法式、泰式、墨西哥等）中选择最适合食材的菜品。\n\n");
        sb.append("## 输出格式要求\n");
        sb.append("返回严格的JSON对象，包含一个dishes数组，共10道菜品：\n");
        sb.append("{\n");
        sb.append("  \"dishes\": [\n");
        sb.append("    { /* 菜品1 */ },\n");
        sb.append("    { /* 菜品2 */ },\n");
        sb.append("    ...\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("## 排序要求\n");
        sb.append("前5-8道必须是真实存在的知名主流菜品，按知名度从高到低排列。")
                .append("如果用户食材组合无法完全覆盖所有推荐菜品，或者有剩余的食材没有对应的知名菜品，")
                .append("可以在列表末尾适当补充少量创新菜品，但必须排在真实菜品之后。\n\n");
        sb.append("## 每个菜品字段\n");
        sb.append("- recipe_key: 唯一标识(英文连字符)\n");
        sb.append("- name: 菜品中文名\n");
        sb.append("- image_path: 空字符串\n");
        sb.append("- difficulty: 简单/中等/困难\n");
        sb.append("- calories: 预估卡路里(整数)\n");
        sb.append("- ingredients: 食材列表[{name: 食材名, amount: 用量}]\n");
        sb.append("- steps: 步骤列表[{step: 步骤序号, content: 步骤说明}]\n");
        sb.append("- cooking_method: 烹饪方式\n");
        sb.append("- cooking_time: 烹饪时间(分钟,整数)\n");
        sb.append("- cuisine_type: 菜系\n");
        sb.append("- region: 地区\n");
        sb.append("- meal_type: 早餐/午餐/晚餐/宵夜\n");
        sb.append("- match_reason: 推荐这道菜的原因\n\n");

        if (request.getCookingMethods() != null && !request.getCookingMethods().isEmpty()) {
            sb.append("## 偏好的烹饪方式\n").append(String.join("、", request.getCookingMethods())).append("\n\n");
        }
        if (request.getDietaryRestrictions() != null && !request.getDietaryRestrictions().isEmpty()) {
            sb.append("## 饮食限制\n").append(String.join("、", request.getDietaryRestrictions())).append("\n\n");
        }
        if (request.getAllergies() != null && !request.getAllergies().isEmpty()) {
            sb.append("## 过敏原注意\n").append(String.join("、", request.getAllergies())).append("\n\n");
        }
        if (request.getCuisineType() != null && !request.getCuisineType().isEmpty()) {
            sb.append("## 偏好菜系\n").append(request.getCuisineType()).append("\n\n");
        }
        if (request.getMealType() != null && !request.getMealType().isEmpty()) {
            sb.append("## 餐型\n").append(request.getMealType()).append("\n\n");
        }
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            sb.append("## 用户偏好\n").append(request.getPreferences()).append("\n\n");
        }
        sb.append("要求：优先推荐有群众基础的知名菜品。如果用户选材组合确实没有足够的知名菜品，")
                .append("允许末尾补充少量合理的创新搭配，但主体必须是真实存在的菜品。");

        // Diet goal
        if (request.getDietGoal() != null && !request.getDietGoal().isEmpty()) {
            sb.append("\n\n## 用户的饮食改变目标\n")
                    .append(request.getDietGoal())
                    .append("\n请优先推荐符合这个目标的菜品，帮助用户达成饮食改变计划。");
        }

        return sb.toString();
    }

    public static String buildRecipeUserPrompt(RecipeRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("我的食材有: ").append(String.join("、", request.getIngredientNames())).append("。");
        if (request.getCookingMethods() != null && !request.getCookingMethods().isEmpty()) {
            sb.append("烹饪方式偏好: ").append(String.join("、", request.getCookingMethods())).append("。");
        }
        sb.append("推荐10道菜品，优先推荐可以使用这些食材制作的真实主流菜品，知名度和群众基础高的排在前面。");
        return sb.toString();
    }

    private String executeChatRequest(String systemPrompt, String userPrompt) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("model", preferences.getModel());

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        body.add("messages", messages);

        String json = gson.toJson(body);
        String apiUrl = buildApiUrl();

        Request httpRequest = buildRequest(apiUrl, json);
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API请求失败: " + response.code());
            }
            String respBody = response.body().string();
            return extractContent(respBody);
        }
    }

    private <T> void sendChatCompletion(String systemPrompt, String userPrompt,
                                         Callback<T> callback, Type type) {
        new Thread(() -> {
            try {
                String content = executeChatRequest(systemPrompt, userPrompt);
                T result = gson.fromJson(content, type);
                callback.onSuccess(result);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    private void sendChatCompletionRaw(String systemPrompt, String userPrompt,
                                        Callback<String> callback) {
        new Thread(() -> {
            try {
                String content = executeChatRequest(systemPrompt, userPrompt);
                callback.onSuccess(content);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    private String buildApiUrl() {
        String url = preferences.getApiUrl();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith("/chat/completions")) {
            if (!url.contains("/v1")) {
                url = url + "/v1/chat/completions";
            } else {
                url = url + "/chat/completions";
            }
        }
        return url;
    }

    private Request buildRequest(String url, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);
        String apiKey = preferences.getApiKey();
        if (!apiKey.isEmpty()) {
            builder.addHeader("Authorization", "Bearer " + apiKey);
        }
        builder.addHeader("Content-Type", "application/json");
        return builder.build();
    }

    private String extractContent(String responseJson) {
        JsonObject resp = gson.fromJson(responseJson, JsonObject.class);
        return resp.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .get("message").getAsJsonObject()
                .get("content").getAsString();
    }
}
