package com.lotlimys.yeseatwhat.ai;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.model.RecipeItem;

import java.util.Arrays;
import java.util.List;

/**
 * 使用阿里云 DashScope SDK 调用通义千问 AI 服务。
 * 仅在用户选择阿里云 Qwen 供应商时使用。
 */
public class DashScopeService implements AIService {

    private final AppPreferences preferences;
    private final Gson gson;

    public DashScopeService(AppPreferences preferences) {
        this.preferences = preferences;
        this.gson = new GsonBuilder().create();
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
    public void generateRecipeImage(String dishName, Callback<String> callback) {
        callback.onError("DashScope SDK 暂不支持图片生成");
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
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            sb.append("## 用户偏好\n").append(request.getPreferences()).append("\n\n");
        }
        sb.append("要求：优先推荐有群众基础的知名菜品。如果用户选材组合确实没有足够的知名菜品，")
                .append("允许末尾补充少量合理的创新搭配，但主体必须是真实存在的菜品。");
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
}
