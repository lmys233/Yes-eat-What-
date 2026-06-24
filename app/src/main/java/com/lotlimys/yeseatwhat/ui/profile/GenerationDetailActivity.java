package com.lotlimys.yeseatwhat.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.entity.GenerationRecord;
import com.lotlimys.yeseatwhat.util.DateUtils;

public class GenerationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generation_detail);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        long recordId = getIntent().getLongExtra("record_id", 0L);
        if (recordId == 0L) {
            finish();
            return;
        }

        loadRecord(recordId);
    }

    private void loadRecord(long recordId) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            GenerationRecord record = AppDatabase.getInstance(this)
                    .generationRecordDao().getById(recordId);
            runOnUiThread(() -> {
                if (record == null) {
                    finish();
                    return;
                }
                bindData(record);
            });
        });
    }

    private void bindData(GenerationRecord record) {
        // === Selection Section ===

        setTextOrGone(R.id.tv_detail_ingredients,
                "食材：" + record.getIngredients());
        setTextOrGone(R.id.tv_detail_cooking_methods,
                record.getCookingMethods(), "烹饪方式：" + record.getCookingMethods());
        setTextOrGone(R.id.tv_detail_cuisines,
                record.getCuisines(), "菜系：" + record.getCuisines());
        setTextOrGone(R.id.tv_detail_meal_types,
                record.getMealTypes(), "餐类：" + record.getMealTypes());
        setTextOrGone(R.id.tv_detail_dining_scene,
                record.getDiningScene(), "场景：" + record.getDiningScene());
        setTextOrGone(R.id.tv_detail_preferences,
                record.getPreferences(), "偏好/忌口：" + record.getPreferences());

        // === Prompt Section ===

        TextView tvPrompt = findViewById(R.id.tv_detail_prompt);
        String prompt = record.getPromptText();
        if (prompt != null && !prompt.isEmpty()) {
            tvPrompt.setText(prompt);
        } else {
            tvPrompt.setText("无");
        }

        // === Result Section ===

        if (record.isSuccess() && record.getCompletedAt() > 0) {
            setTextOrGone(R.id.tv_detail_status, "状态：成功");
            setTextOrGone(R.id.tv_detail_dish_count,
                    "生成菜品：" + record.getDishCount() + " 道");
        } else if (!record.isSuccess() && record.getCompletedAt() > 0) {
            setTextOrGone(R.id.tv_detail_status, "状态：失败");
            setTextOrGone(R.id.tv_detail_dish_count,
                    "生成菜品：0 道");
        } else {
            setTextOrGone(R.id.tv_detail_status, "状态：生成中...");
            findViewById(R.id.tv_detail_dish_count).setVisibility(View.GONE);
        }

        setTextOrGone(R.id.tv_detail_created_at,
                "开始时间：" + DateUtils.formatDateTime(record.getCreatedAt()));

        if (record.getCompletedAt() > 0) {
            setTextOrGone(R.id.tv_detail_completed_at,
                    "完成时间：" + DateUtils.formatDateTime(record.getCompletedAt()));
        } else {
            findViewById(R.id.tv_detail_completed_at).setVisibility(View.GONE);
        }
    }

    private void setTextOrGone(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (text != null && !text.isEmpty()) {
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setTextOrGone(int viewId, String value, String labelWithValue) {
        TextView tv = findViewById(viewId);
        if (value != null && !value.isEmpty()) {
            tv.setText(labelWithValue);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }
}
