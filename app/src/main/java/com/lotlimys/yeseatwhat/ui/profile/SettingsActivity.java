package com.lotlimys.yeseatwhat.ui.profile;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private AppPreferences appPrefs;

    private static final Map<String, Integer> THEME_COLORS = new LinkedHashMap<>();
    static {
        THEME_COLORS.put(Constants.THEME_PURPLE, R.color.theme_pastel_purple);
        THEME_COLORS.put(Constants.THEME_RED, R.color.theme_pastel_red);
        THEME_COLORS.put(Constants.THEME_BLUE, R.color.theme_pastel_blue);
        THEME_COLORS.put(Constants.THEME_YELLOW, R.color.theme_pastel_yellow);
        THEME_COLORS.put(Constants.THEME_GREEN, R.color.theme_pastel_green);
        THEME_COLORS.put(Constants.THEME_ORANGE, R.color.theme_pastel_orange);
        THEME_COLORS.put(Constants.THEME_GRAY, R.color.theme_pastel_gray);
    }

    private static class ProviderInfo {
        final String displayName;
        final String apiUrl;
        final String model;
        ProviderInfo(String displayName, String apiUrl, String model) {
            this.displayName = displayName;
            this.apiUrl = apiUrl;
            this.model = model;
        }
    }

    private static final Map<String, ProviderInfo> AI_PROVIDERS = new LinkedHashMap<>();
    static {
        AI_PROVIDERS.put(Constants.PROVIDER_QWEN,
                new ProviderInfo("通义千问", Constants.PROVIDER_QWEN_URL, Constants.PROVIDER_QWEN_MODEL));
        AI_PROVIDERS.put(Constants.PROVIDER_MINIMAX,
                new ProviderInfo("MiniMax", Constants.PROVIDER_MINIMAX_URL, Constants.PROVIDER_MINIMAX_MODEL));
        AI_PROVIDERS.put(Constants.PROVIDER_DEEPSEEK,
                new ProviderInfo("DeepSeek", Constants.PROVIDER_DEEPSEEK_URL, Constants.PROVIDER_DEEPSEEK_MODEL));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        appPrefs = AppPreferences.getInstance(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        LinearLayout llThemeColors = findViewById(R.id.ll_theme_colors);
        LinearLayout llAiProviders = findViewById(R.id.ll_ai_providers);
        EditText etApiKey = findViewById(R.id.et_api_key);

        // Load saved API key
        etApiKey.setText(appPrefs.getApiKey());

        // Build theme color circles
        String currentTheme = appPrefs.getThemeColor();
        int circleSizePx = (int) (getResources().getDisplayMetrics().density * 40);
        int marginPx = (int) (getResources().getDisplayMetrics().density * 6);

        for (Map.Entry<String, Integer> entry : THEME_COLORS.entrySet()) {
            String themeKey = entry.getKey();
            int colorRes = entry.getValue();
            int color = getResources().getColor(colorRes);

            View circle = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(circleSizePx, circleSizePx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            circle.setLayoutParams(lp);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);

            if (themeKey.equals(currentTheme)) {
                drawable.setStroke((int) (getResources().getDisplayMetrics().density * 3),
                        getResources().getColor(R.color.black));
            }

            circle.setBackground(drawable);
            circle.setClickable(true);
            circle.setFocusable(true);
            circle.setOnClickListener(v -> {
                if (!themeKey.equals(appPrefs.getThemeColor())) {
                    appPrefs.setThemeColor(themeKey);
                    Toast.makeText(this, "主题已更改", Toast.LENGTH_SHORT).show();
                    recreate();
                }
            });

            llThemeColors.addView(circle);
        }

        // Build AI provider buttons
        String currentProvider = appPrefs.getAiProvider();
        int btnHeightPx = (int) (getResources().getDisplayMetrics().density * 42);

        for (Map.Entry<String, ProviderInfo> entry : AI_PROVIDERS.entrySet()) {
            String providerKey = entry.getKey();
            ProviderInfo info = entry.getValue();

            MaterialButton btn = new MaterialButton(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, btnHeightPx);
            lp.weight = 1;
            lp.setMargins(marginPx, 0, marginPx, 0);
            btn.setLayoutParams(lp);
            btn.setText(info.displayName);
            btn.setTextSize(13);
            btn.setCornerRadius((int) (getResources().getDisplayMetrics().density * 21));
            btn.setElevation(0);

            boolean isSelected = providerKey.equals(currentProvider);
            btn.setBackgroundTintList(ColorStateList.valueOf(
                    isSelected ? getResources().getColor(R.color.theme_pastel_purple) : 0xFFF0F0F0));
            btn.setTextColor(isSelected
                    ? getResources().getColor(android.R.color.white)
                    : 0xFF444444);
            btn.setStrokeColor(ColorStateList.valueOf(0xFFDDDDDD));
            btn.setStrokeWidth(isSelected ? 0 : 1);

            btn.setOnClickListener(v -> {
                appPrefs.setAiProvider(providerKey);
                appPrefs.setApiUrl(info.apiUrl);
                appPrefs.setModel(info.model);

                // Update button styles
                updateProviderButtons(llAiProviders, providerKey);
                Toast.makeText(this, "已切换至 " + info.displayName, Toast.LENGTH_SHORT).show();
            });

            llAiProviders.addView(btn);
        }

        // Save button
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String key = etApiKey.getText().toString().trim();
            if (!key.isEmpty()) {
                appPrefs.setApiKey(key);
                Toast.makeText(this, "API Key 已保存", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void updateProviderButtons(LinearLayout container, String selectedKey) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton btn = (MaterialButton) child;
                // Find which provider this button corresponds to
                int idx = 0;
                for (String key : AI_PROVIDERS.keySet()) {
                    if (idx == i) {
                        boolean selected = key.equals(selectedKey);
                        btn.setBackgroundTintList(ColorStateList.valueOf(
                                selected ? getResources().getColor(R.color.theme_pastel_purple) : 0xFFF0F0F0));
                        btn.setTextColor(selected
                                ? getResources().getColor(android.R.color.white)
                                : 0xFF444444);
                        btn.setStrokeWidth(selected ? 0 : 1);
                        break;
                    }
                    idx++;
                }
            }
        }
    }
}
