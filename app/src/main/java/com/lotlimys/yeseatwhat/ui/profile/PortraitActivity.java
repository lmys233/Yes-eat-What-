package com.lotlimys.yeseatwhat.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.ai.AIService;
import com.lotlimys.yeseatwhat.ai.DashScopeService;
import com.lotlimys.yeseatwhat.ai.OpenAIService;
import com.lotlimys.yeseatwhat.ai.PortraitRequest;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.entity.HistoryWithRecipe;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PortraitActivity extends AppCompatActivity {

    private AppPreferences appPrefs;
    private AIService aiService;

    private TextView tvContent, tvTime, tvRegenHint, tvError;
    private LinearLayout llLoading, llError;
    private Button btnRetry, btnRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portrait);

        appPrefs = AppPreferences.getInstance(this);

        // Create AI service based on provider
        String provider = appPrefs.getAiProvider();
        if (Constants.PROVIDER_QWEN.equals(provider)) {
            aiService = new DashScopeService(appPrefs);
        } else {
            aiService = new OpenAIService(appPrefs);
        }

        // Toolbar
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // Views
        tvContent = findViewById(R.id.tv_portrait_content);
        tvTime = findViewById(R.id.tv_portrait_time);
        tvRegenHint = findViewById(R.id.tv_regen_hint);
        tvError = findViewById(R.id.tv_error);
        llLoading = findViewById(R.id.ll_loading);
        llError = findViewById(R.id.ll_error);
        btnRetry = findViewById(R.id.btn_retry);
        btnRefresh = findViewById(R.id.btn_refresh);

        btnRetry.setOnClickListener(v -> generatePortrait());
        btnRefresh.setOnClickListener(v -> generatePortrait());

        // Check if we should regenerate or show cached
        long lastGenerated = appPrefs.getPortraitGeneratedAt();
        String cachedContent = appPrefs.getPortraitContent();

        if (lastGenerated > 0 && !cachedContent.isEmpty()
                && (System.currentTimeMillis() - lastGenerated) < Constants.PORTRAIT_REGEN_INTERVAL_MS) {
            // Within 3 days, show cached
            showPortrait(cachedContent, lastGenerated);
        } else {
            // Expired or first time, generate
            generatePortrait();
        }
    }

    private void generatePortrait() {
        llLoading.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        tvContent.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
        tvRegenHint.setVisibility(View.GONE);

        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            try {
                long threeDaysAgo = System.currentTimeMillis() - Constants.PORTRAIT_REGEN_INTERVAL_MS;

                // Get recent history with recipe details
                List<HistoryWithRecipe> recentHistory =
                        AppDatabase.getInstance(this).historyDao().getRecentHistory(threeDaysAgo);
                List<String> historyNames = new ArrayList<>();
                for (HistoryWithRecipe hwr : recentHistory) {
                    if (hwr.recipe != null && hwr.recipe.getName() != null) {
                        historyNames.add(hwr.recipe.getName());
                    }
                }

                // Get favorites with recipe details
                List<HistoryWithRecipe> favorites =
                        AppDatabase.getInstance(this).historyDao().getFavorites();
                List<String> favoriteNames = new ArrayList<>();
                for (HistoryWithRecipe hwr : favorites) {
                    if (hwr.recipe != null && hwr.recipe.getName() != null) {
                        favoriteNames.add(hwr.recipe.getName());
                    }
                }

                // Get self-description and previous portrait
                String selfDescription = appPrefs.getSelfDescription();
                String previousPortrait = appPrefs.getPortraitContent();

                // Build portrait request
                PortraitRequest request = new PortraitRequest(
                        new ArrayList<>(), new ArrayList<>(), selfDescription,
                        historyNames, favoriteNames, previousPortrait
                );

                // Call AI
                aiService.generatePortrait(request, new AIService.Callback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            long now = System.currentTimeMillis();
                            appPrefs.setPortraitContent(result);
                            appPrefs.setPortraitGeneratedAt(now);
                            showPortrait(result, now);
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            llLoading.setVisibility(View.GONE);
                            llError.setVisibility(View.VISIBLE);
                            tvError.setText(errorMessage);
                        });
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    llLoading.setVisibility(View.GONE);
                    llError.setVisibility(View.VISIBLE);
                    tvError.setText("数据获取失败: " + e.getMessage());
                });
            }
        });
    }

    private void showPortrait(String content, long generatedAt) {
        llLoading.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
        tvContent.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);

        tvContent.setText(content.replace("**", ""));

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(generatedAt));
        tvTime.setText("上次更新: " + dateStr);

        // Show next regen time
        String nextDateStr = new SimpleDateFormat("MM月dd日", Locale.getDefault())
                .format(new Date(generatedAt + Constants.PORTRAIT_REGEN_INTERVAL_MS));
        tvRegenHint.setVisibility(View.VISIBLE);
        tvRegenHint.setText("下一次自动更新: " + nextDateStr);
    }
}
