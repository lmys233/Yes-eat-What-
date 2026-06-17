package com.lotlimys.yeseatwhat.util;

public class Constants {
    // AI API
    public static final String DEFAULT_API_URL = "https://api.openai.com/v1";
    public static final String DEFAULT_MODEL = "gpt-4o-mini";
    public static final String DEFAULT_IMAGE_MODEL = "dall-e-3";
    public static final int AI_TIMEOUT_SECONDS = 60;

    // AI Providers
    public static final String PROVIDER_QWEN = "qwen";
    public static final String PROVIDER_MINIMAX = "minimax";
    public static final String PROVIDER_DEEPSEEK = "deepseek";

    public static final String PROVIDER_QWEN_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    public static final String PROVIDER_QWEN_MODEL = "qwen-plus";

    public static final String PROVIDER_MINIMAX_URL = "https://api.minimax.chat/v1";
    public static final String PROVIDER_MINIMAX_MODEL = "MiniMax-Text-01";

    public static final String PROVIDER_DEEPSEEK_URL = "https://api.deepseek.com";
    public static final String PROVIDER_DEEPSEEK_MODEL = "deepseek-chat";

    // Recipe limits
    public static final int MAX_EXACT_MATCHES = 3;
    public static final int MAX_TRY_THESE = 3;
    public static final int MAX_HISTORY_DAYS = 30;

    // Cooldown
    public static final int COOLDOWN_GUEST_MS = 15000;
    public static final int COOLDOWN_LOGGED_IN_MS = 10000;

    // SharedPreferences keys
    public static final String PREF_NAME = "yeseatwhat_prefs";
    public static final String KEY_API_URL = "api_url";
    public static final String KEY_API_KEY = "api_key";
    public static final String KEY_MODEL = "model";
    public static final String KEY_THEME = "theme_color";
    public static final String KEY_AI_PROVIDER = "ai_provider";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_SELF_DESCRIPTION = "self_description";
    public static final String KEY_DEVICE_UUID = "device_uuid";

    // Theme values
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_RED = "red";
    public static final String THEME_BLUE = "blue";
    public static final String THEME_YELLOW = "yellow";
    public static final String THEME_GREEN = "green";
    public static final String THEME_ORANGE = "orange";
    public static final String THEME_GRAY = "gray";

    // Portrait
    public static final String KEY_PORTRAIT_CONTENT = "portrait_content";
    public static final String KEY_PORTRAIT_GENERATED_AT = "portrait_generated_at";
    public static final String KEY_DIET_GOAL = "diet_goal";
    public static final long PORTRAIT_REGEN_INTERVAL_MS = 3L * 86400000L; // 3 days
}
