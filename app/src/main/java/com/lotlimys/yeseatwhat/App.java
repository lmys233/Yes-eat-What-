package com.lotlimys.yeseatwhat;

import android.app.Activity;
import android.app.Application;

import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.util.Constants;

import java.util.UUID;

public class App extends Application {
    private static App instance;
    private AppPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferences = AppPreferences.getInstance(this);
        initDeviceUuid();
    }

    public static App getInstance() {
        return instance;
    }

    public static AppPreferences getPreferences() {
        return instance.preferences;
    }

    /**
     * 在 Activity 的 super.onCreate() 之前调用，应用用户选择的主题色。
     */
    public static void applyTheme(Activity activity) {
        String theme = getPreferences().getThemeColor();
        switch (theme) {
            case Constants.THEME_RED:
                activity.setTheme(R.style.Theme_YesEatWhat_Red);
                break;
            case Constants.THEME_BLUE:
                activity.setTheme(R.style.Theme_YesEatWhat_Blue);
                break;
            case Constants.THEME_YELLOW:
                activity.setTheme(R.style.Theme_YesEatWhat_Yellow);
                break;
            case Constants.THEME_GREEN:
                activity.setTheme(R.style.Theme_YesEatWhat_Green);
                break;
            case Constants.THEME_ORANGE:
                activity.setTheme(R.style.Theme_YesEatWhat_Orange);
                break;
            case Constants.THEME_GRAY:
                activity.setTheme(R.style.Theme_YesEatWhat_Gray);
                break;
            default:
                activity.setTheme(R.style.Theme_YesEatWhat);
                break;
        }
    }

    private void initDeviceUuid() {
        if (preferences.getDeviceUuid().isEmpty()) {
            preferences.setDeviceUuid(UUID.randomUUID().toString());
        }
    }
}
