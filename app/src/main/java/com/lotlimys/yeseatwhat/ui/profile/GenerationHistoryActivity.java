package com.lotlimys.yeseatwhat.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.entity.GenerationRecord;
import com.lotlimys.yeseatwhat.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class GenerationHistoryActivity extends AppCompatActivity {

    private RecyclerView rvGeneration;
    private LinearLayout llEmpty;
    private GenerationAdapter adapter;
    private List<GenerationRecord> recordList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generation_history);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        rvGeneration = findViewById(R.id.rv_generation);
        llEmpty = findViewById(R.id.ll_empty);

        rvGeneration.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenerationAdapter();
        rvGeneration.setAdapter(adapter);

        loadRecords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    private void loadRecords() {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            List<GenerationRecord> records = AppDatabase.getInstance(this)
                    .generationRecordDao().getAllByTimeDesc();
            runOnUiThread(() -> {
                recordList = records;
                if (recordList.isEmpty()) {
                    rvGeneration.setVisibility(View.GONE);
                    llEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvGeneration.setVisibility(View.VISIBLE);
                    llEmpty.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void openDetail(GenerationRecord record) {
        Intent intent = new Intent(this, GenerationDetailActivity.class);
        intent.putExtra("record_id", record.getId());
        startActivity(intent);
    }

    // ===== Adapter =====

    private class GenerationAdapter extends RecyclerView.Adapter<GenerationAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_generation_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GenerationRecord record = recordList.get(position);

            // Ingredients
            String ingredients = record.getIngredients();
            if (ingredients != null && !ingredients.isEmpty()) {
                holder.tvIngredients.setVisibility(View.VISIBLE);
                holder.tvIngredients.setText("食材：" + ingredients);
            } else {
                holder.tvIngredients.setVisibility(View.GONE);
            }

            // Cooking methods
            String methods = record.getCookingMethods();
            if (methods != null && !methods.isEmpty()) {
                holder.tvCookingMethods.setVisibility(View.VISIBLE);
                holder.tvCookingMethods.setText("烹饪方式：" + methods);
            } else {
                holder.tvCookingMethods.setVisibility(View.GONE);
            }

            // Dish count
            if (record.isSuccess() && record.getDishCount() > 0) {
                holder.tvDishCount.setText("生成 " + record.getDishCount() + " 道菜");
                holder.tvDishCount.setVisibility(View.VISIBLE);
            } else if (!record.isSuccess() && record.getCompletedAt() > 0) {
                holder.tvDishCount.setText("生成失败");
                holder.tvDishCount.setVisibility(View.VISIBLE);
            } else {
                holder.tvDishCount.setText("生成中...");
                holder.tvDishCount.setVisibility(View.VISIBLE);
            }

            // Time
            holder.tvCreatedTime.setText(DateUtils.formatDateTime(record.getCreatedAt()));

            holder.itemView.setOnClickListener(v -> openDetail(record));
        }

        @Override
        public int getItemCount() {
            return recordList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvIngredients, tvCookingMethods, tvDishCount, tvCreatedTime;

            ViewHolder(View view) {
                super(view);
                tvIngredients = view.findViewById(R.id.tv_ingredients);
                tvCookingMethods = view.findViewById(R.id.tv_cooking_methods);
                tvDishCount = view.findViewById(R.id.tv_dish_count);
                tvCreatedTime = view.findViewById(R.id.tv_created_time);
            }
        }
    }
}
