package com.lotlimys.yeseatwhat.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.lotlimys.yeseatwhat.util.Constants;

public class AppPreferences {
    private static AppPreferences instance;
    private final SharedPreferences prefs;

    private AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized AppPreferences getInstance(Context context) {
        if (instance == null) {
            instance = new AppPreferences(context);
        }
        return instance;
    }

    public String getApiUrl() {
        return prefs.getString(Constants.KEY_API_URL, Constants.DEFAULT_API_URL);
    }

    public void setApiUrl(String url) {
        prefs.edit().putString(Constants.KEY_API_URL, url).apply();
    }

    public String getApiKey() {
        return prefs.getString(Constants.KEY_API_KEY, "");
    }

    public void setApiKey(String key) {
        prefs.edit().putString(Constants.KEY_API_KEY, key).apply();
    }

    public String getModel() {
        return prefs.getString(Constants.KEY_MODEL, Constants.DEFAULT_MODEL);
    }

    public void setModel(String model) {
        prefs.edit().putString(Constants.KEY_MODEL, model).apply();
    }

    public String getThemeColor() {
        return prefs.getString(Constants.KEY_THEME, Constants.THEME_PURPLE);
    }

    public void setThemeColor(String theme) {
        prefs.edit().putString(Constants.KEY_THEME, theme).apply();
    }

    public String getNickname() {
        return prefs.getString(Constants.KEY_NICKNAME, "");
    }

    public void setNickname(String nickname) {
        prefs.edit().putString(Constants.KEY_NICKNAME, nickname).apply();
    }

    public String getSelfDescription() {
        return prefs.getString(Constants.KEY_SELF_DESCRIPTION, "");
    }

    public void setSelfDescription(String description) {
        prefs.edit().putString(Constants.KEY_SELF_DESCRIPTION, description).apply();
    }

    public String getDeviceUuid() {
        return prefs.getString(Constants.KEY_DEVICE_UUID, "");
    }

    public void setDeviceUuid(String uuid) {
        prefs.edit().putString(Constants.KEY_DEVICE_UUID, uuid).apply();
    }

    public String getAiProvider() {
        return prefs.getString(Constants.KEY_AI_PROVIDER, "");
    }

    public void setAiProvider(String provider) {
        prefs.edit().putString(Constants.KEY_AI_PROVIDER, provider).apply();
    }

    // ===== Portrait =====

    public String getPortraitContent() {
        return prefs.getString(Constants.KEY_PORTRAIT_CONTENT, "");
    }

    public void setPortraitContent(String content) {
        prefs.edit().putString(Constants.KEY_PORTRAIT_CONTENT, content).apply();
    }

    public long getPortraitGeneratedAt() {
        return prefs.getLong(Constants.KEY_PORTRAIT_GENERATED_AT, 0);
    }

    public void setPortraitGeneratedAt(long time) {
        prefs.edit().putLong(Constants.KEY_PORTRAIT_GENERATED_AT, time).apply();
    }

    public String getDietGoal() {
        return prefs.getString(Constants.KEY_DIET_GOAL, "");
    }

    public void setDietGoal(String goal) {
        prefs.edit().putString(Constants.KEY_DIET_GOAL, goal).apply();
    }
}
