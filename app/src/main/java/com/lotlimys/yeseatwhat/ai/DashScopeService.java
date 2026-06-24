package com.lotlimys.yeseatwhat.ai;

import android.util.Log;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用阿里云 DashScope SDK 调用通义千问 AI 服务。
 * 使用阿里云 DashScope SDK 调用通义千问 AI 服务。
 * 仅在用户选择阿里云 Qwen 供应商时使用。
 */
public class DashScopeService implements AIService {

    private static final MediaType JSON = MediaType.get("application/json");
    private static final String IMAGE_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";
    private static final String IMAGE_QUERY_URL = "https://dashscope.aliyuncs.com/api/v1/tasks";
    private static final String IMAGE_MODEL = "qwen-image-plus";

    private final AppPreferences preferences;
    private final Gson gson;
    private final OkHttpClient imageClient;

    public DashScopeService(AppPreferences preferences) {
        this.preferences = preferences;
        this.gson = new GsonBuilder().create();
        this.imageClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void generateRecipes(RecipeRequest request, Callback<RecipeResponse> callback) {
        new Thread(() -> {
            try {
                String content = callDashScope(buildRecipeSystemPrompt(request),
                        buildRecipeUserPrompt(request));
                RecipeResponse response = gson.fromJson(content, RecipeResponse.class);
                callback.onSuccess(response);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void generateRecipeImage(RecipeItem recipe, Callback<String> callback) {
        new Thread(() -> {
            try {
                String prompt = buildImagePrompt(recipe);
                Log.d("DashScopeImg", "提示词:\n" + prompt);
                String imageUrl = callImageGeneration(prompt);
                Log.d("DashScopeImg", "返回URL: " + imageUrl);
                callback.onSuccess(imageUrl);
            } catch (Exception e) {
                Log.e("DashScopeImg", "生成失败", e);
                callback.onError("图片生成失败: " + e.getMessage());
            }
        }).start();
    }

    private String buildImagePrompt(RecipeItem recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("生成像素风格").append(recipe.getName()).append("图片\n");

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            sb.append("食材：");
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                if (i > 0) sb.append("、");
                RecipeItem.IngredientAmount ing = recipe.getIngredients().get(i);
                sb.append(ing.getName());
                if (ing.getAmount() != null && !ing.getAmount().isEmpty()) {
                    sb.append("(").append(ing.getAmount()).append(")");
                }
            }
            sb.append("\n");
        }

        if (recipe.getCookingMethod() != null && !recipe.getCookingMethod().isEmpty()) {
            sb.append("烹饪方式：").append(recipe.getCookingMethod()).append("\n");
        }

        sb.append("不要高清逼真、拉高像素颗粒感\n")
                .append("大幅降低分辨率、减少总像素点数量（指定像素点 2000 个）\n")
                .append("背景：纯白色背景\n")
                .append("画风要求：泰拉瑞亚物品风格（复古低像素、游戏道具风、极简方块像素，不写实）");

        return sb.toString();
    }

    private String callImageGeneration(String prompt) throws Exception {
        String apiKey = preferences.getApiKey();

        // Step 1: 创建异步任务获取 task_id
        String taskId = createAsyncTask(prompt, apiKey);

        // Step 2: 轮询任务结果
        return pollTaskResult(taskId, apiKey);
    }

    /**
     * 创建异步图片生成任务，返回 task_id。
     * 文档: POST https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis
     */
    private String createAsyncTask(String prompt, String apiKey) throws Exception {
        int maxRetries = 3;
        int retryDelay = 5000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            JsonObject body = new JsonObject();
            body.addProperty("model", IMAGE_MODEL);

            JsonObject input = new JsonObject();
            input.addProperty("prompt", prompt);
            body.add("input", input);

            JsonObject parameters = new JsonObject();
            parameters.addProperty("size", "1024*1024");
            parameters.addProperty("n", 1);
            parameters.addProperty("prompt_extend", false);
            parameters.addProperty("watermark", false);
            body.add("parameters", parameters);

            String json = gson.toJson(body);
            Log.d("DashScopeImg", ">> 请求URL: " + IMAGE_API_URL);
            Log.d("DashScopeImg", ">> 请求body: " + json);

            Request httpRequest = new Request.Builder()
                    .url(IMAGE_API_URL)
                    .post(RequestBody.create(json, JSON))
                    .addHeader("X-DashScope-Async", "enable")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = imageClient.newCall(httpRequest).execute()) {
                Log.d("DashScopeImg", "<< 响应code: " + response.code());
                if (response.code() == 429) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    Log.w("DashScopeImg", "[" + attempt + "/" + maxRetries + "] 429限流: " + errBody);
                    if (attempt < maxRetries) {
                        Log.d("DashScopeImg", "等待 " + retryDelay + "ms 后重试创建任务...");
                        try { Thread.sleep(retryDelay); } catch (InterruptedException ignored) {}
                        retryDelay *= 2;
                        continue;
                    } else {
                        throw new IOException("图片生成限流，已重试" + maxRetries + "次仍失败: " + errBody);
                    }
                }
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    Log.e("DashScopeImg", "HTTP " + response.code() + ": " + errBody);
                    throw new IOException("创建图片任务失败: " + response.code() + " " + errBody);
                }
                String respBody = response.body().string();
                Log.d("DashScopeImg", "创建任务响应: " + respBody);
                JsonObject resp = gson.fromJson(respBody, JsonObject.class);
                JsonObject output = resp.getAsJsonObject("output");
                String taskId = output.get("task_id").getAsString();
                String taskStatus = output.get("task_status").getAsString();
                Log.d("DashScopeImg", "任务已创建: " + taskId + " 状态: " + taskStatus);
                return taskId;
            }
        }
        throw new IOException("图片生成任务创建失败");
    }

    /**
     * 根据 task_id 轮询图片生成结果。
     * 文档: GET https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}
     */
    private String pollTaskResult(String taskId, String apiKey) throws Exception {
        String url = IMAGE_QUERY_URL + "/" + taskId;
        long pollInterval = 2000;
        int maxPolls = 90; // 最长等待 3 分钟

        for (int i = 0; i < maxPolls; i++) {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response response = imageClient.newCall(request).execute()) {
                Log.d("DashScopeImg", "<< 轮询响应 code=" + response.code());
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    Log.e("DashScopeImg", "轮询失败 HTTP " + response.code() + " body=" + errBody);
                    throw new IOException("查询任务状态失败: " + response.code());
                }
                String respBody = response.body().string();
                JsonObject resp = gson.fromJson(respBody, JsonObject.class);
                JsonObject output = resp.getAsJsonObject("output");
                String status = output.get("task_status").getAsString();

                Log.d("DashScopeImg", "轮询 [" + (i + 1) + "] task_id=" + taskId + " status=" + status);

                if ("SUCCEEDED".equals(status)) {
                    if (output.has("results")) {
                        JsonArray results = output.getAsJsonArray("results");
                        if (results.size() > 0) {
                            String imageUrl = results.get(0).getAsJsonObject().get("url").getAsString();
                            Log.d("DashScopeImg", "生成成功 URL: " + imageUrl);
                            return imageUrl;
                        }
                    }
                    throw new IOException("任务成功但未找到图片URL");
                } else if ("FAILED".equals(status)) {
                    String message = output.has("message") ? output.get("message").getAsString() : "未知错误";
                    throw new IOException("图片生成失败: " + message);
                } else if ("CANCELED".equals(status)) {
                    throw new IOException("图片生成任务已取消");
                }
                // PENDING 或 RUNNING - 继续轮询
                Thread.sleep(pollInterval);
            }
        }
        throw new IOException("图片生成超时（3分钟）");
    }

    @Override
    public void generateRegionRecommendation(String region, Callback<List<RecipeItem>> callback) {
        new Thread(() -> {
            try {
                String systemPrompt = "你是一个美食推荐专家。根据用户提供的地区，推荐该地区的特色菜品。"
                        + "请以JSON数组格式返回，每个菜品包含: name, cooking_method, cooking_time(分钟), "
                        + "difficulty(简单/中等/困难), calories, cuisine_type, region, meal_type。"
                        + "最多推荐3道菜。";
                String userPrompt = "请推荐" + region + "的特色菜品，共3道。";
                String content = callDashScope(systemPrompt, userPrompt);
                List<RecipeItem> items = gson.fromJson(content,
                        new TypeToken<List<RecipeItem>>() {}.getType());
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void generateRandomDishes(List<Integer> ingredientIds, Callback<List<RecipeItem>> callback) {
        new Thread(() -> {
            try {
                String systemPrompt = "你是一个创意美食专家。根据用户提供的食材ID列表，随机生成3道创意菜品。"
                        + "请以JSON数组格式返回，每个菜品包含: name, cooking_method, cooking_time(分钟), "
                        + "difficulty(简单/中等/困难), calories, cuisine_type, region, meal_type。";
                String userPrompt = "食材IDs: " + gson.toJson(ingredientIds)
                        + "，请随机生成3道菜品。";
                String content = callDashScope(systemPrompt, userPrompt);
                List<RecipeItem> items = gson.fromJson(content,
                        new TypeToken<List<RecipeItem>>() {}.getType());
                callback.onSuccess(items);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void generatePortrait(PortraitRequest request, Callback<String> callback) {
        new Thread(() -> {
            try {
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

                String content = callDashScope(systemPrompt.toString(), userPrompt.toString());
                callback.onSuccess(content);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void generateIngredientImage(String ingredientName, String categoryName, Callback<String> callback) {
        new Thread(() -> {
            try {
                String prompt = "生成像素风格" + ingredientName + "的单一图标\n"
                        + "分类：" + (categoryName != null ? categoryName : "") + "\n"
                        + "不要高清逼真、拉高像素颗粒感\n"
                        + "大幅降低分辨率、减少总像素点数量\n"
                        + "背景必须是纯白色#FFFFFF，不允许有任何阴影、渐变、黑边或黑框\n"
                        + "食材周围绝对不能有黑色轮廓线或暗色描边\n"
                        + "画风要求：泰拉瑞亚物品风格（复古低像素、游戏道具风、极简方块像素、不写实）\n"
                        + "单个物品图标居中展示，不要多余的装饰文字或图案\n"
                        + "重要：图标边缘和背景之间必须完全平滑过渡，没有任何暗色边框";
                Log.d("DashScopeImg", "食材图标提示词: " + ingredientName + "(" + categoryName + ")");
                String imageUrl = callImageGeneration(prompt);
                callback.onSuccess(imageUrl);
            } catch (Exception e) {
                Log.e("DashScopeImg", "食材图标生成失败: " + ingredientName, e);
                callback.onError("食材图标生成失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void chat(List<ChatMessage> history, Callback<String> callback) {
        new Thread(() -> {
            try {
                com.alibaba.dashscope.common.Message systemMsg = com.alibaba.dashscope.common.Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content("你是一个智能助手，可以回答各种问题。请用中文回复，回答简洁有帮助。")
                        .build();

                List<com.alibaba.dashscope.common.Message> msgList = new java.util.ArrayList<>();
                msgList.add(systemMsg);
                for (ChatMessage msg : history) {
                    msgList.add(com.alibaba.dashscope.common.Message.builder()
                            .role(msg.getRole())
                            .content(msg.getContent())
                            .build());
                }

                Generation gen = new Generation();
                GenerationParam param = GenerationParam.builder()
                        .apiKey(preferences.getApiKey())
                        .model(preferences.getModel())
                        .messages(msgList)
                        .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                        .build();
                GenerationResult result = gen.call(param);
                String reply = result.getOutput().getChoices().get(0).getMessage().getContent();
                callback.onSuccess(reply);
            } catch (Exception e) {
                callback.onError("请求失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 调用 DashScope SDK 发送聊天请求。
     */
    private String callDashScope(String systemPrompt, String userPrompt) throws Exception {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(systemPrompt)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(userPrompt)
                .build();
        GenerationParam param = GenerationParam.builder()
                .apiKey(preferences.getApiKey())
                .model(preferences.getModel())
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        GenerationResult result = gen.call(param);
        return result.getOutput().getChoices().get(0).getMessage().getContent();
    }

    // ===== Prompt Builders =====

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
        if (request.getDiningScene() != null && !request.getDiningScene().isEmpty()) {
            sb.append("## 食用场景\n").append(request.getDiningScene())
                    .append("\n（请根据该场景推荐合适的菜品，比如\"朋友聚餐\"推荐适合分享的大份菜品，\"减脂餐\"推荐低卡健康菜品）\n\n");
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
        if (request.getDiningScene() != null && !request.getDiningScene().isEmpty()) {
            sb.append("食用场景: ").append(request.getDiningScene()).append("。");
        }
        sb.append("推荐10道菜品，优先推荐可以使用这些食材制作的真实主流菜品，知名度和群众基础高的排在前面。");
        return sb.toString();
    }
}
