package com.lotlimys.yeseatwhat.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.db.entity.GeneratedRecipe;
import com.lotlimys.yeseatwhat.data.db.entity.HistoryWithRecipe;
import com.lotlimys.yeseatwhat.data.repository.HistoryRepository;
import com.lotlimys.yeseatwhat.model.RecipeItem;
import com.lotlimys.yeseatwhat.ui.detail.RecipeDetailActivity;
import com.lotlimys.yeseatwhat.util.DateUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private LinearLayout llEmpty;
    private HistoryRepository historyRepository;
    private HistoryAdapter adapter;
    private Gson gson = new Gson();
    private List<HistoryWithRecipe> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRepository = new HistoryRepository(this);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        rvHistory = findViewById(R.id.rv_history);
        llEmpty = findViewById(R.id.ll_empty);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        historyRepository.get7DayHistoryAsync(history -> {
            historyList = history;

            if (historyList.isEmpty()) {
                rvHistory.setVisibility(View.GONE);
                llEmpty.setVisibility(View.VISIBLE);
            } else {
                rvHistory.setVisibility(View.VISIBLE);
                llEmpty.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void openRecipeDetail(HistoryWithRecipe item) {
        GeneratedRecipe recipe = item.recipe;
        if (recipe == null) return;

        RecipeItem recipeItem = RecipeItem.fromGeneratedRecipe(recipe, gson);

        List<RecipeItem> singleItemList = new ArrayList<>();
        singleItemList.add(recipeItem);

        // 更新浏览时间
        historyRepository.addOrUpdate(recipe.getRecipeKey());

        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("dishes", gson.toJson(singleItemList));
        intent.putExtra("direct_detail", true);
        startActivity(intent);
    }

    // ===== Adapter =====

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryWithRecipe hwr = historyList.get(position);
            GeneratedRecipe recipe = hwr.recipe;
            if (recipe == null) return;

            holder.tvName.setText(recipe.getName());

            // Load image with Glide
            String imagePath = recipe.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(HistoryActivity.this)
                        .load(imagePath)
                        .placeholder(R.drawable.apic)
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.drawable.apic);
            }

            // Ingredients summary
            String ingredients = "";
            try {
                Type listType = new TypeToken<List<RecipeItem.IngredientAmount>>() {}.getType();
                List<RecipeItem.IngredientAmount> list = gson.fromJson(recipe.getIngredientsJson(), listType);
                ingredients = RecipeItem.toIngredientsSummary(list);
            } catch (Exception ignored) {}

            holder.tvIngredients.setText("食材: " + (ingredients.isEmpty() ? "暂无" : ingredients));
            holder.tvCookingMethod.setText(recipe.getCookingMethod() != null ? recipe.getCookingMethod() : "");

            // 显示浏览时间
            holder.tvSubTime.setVisibility(View.VISIBLE);
            holder.tvSubTime.setText(DateUtils.formatDateTime(hwr.history.getViewedAt()));

            holder.itemView.setOnClickListener(v -> openRecipeDetail(hwr));
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName, tvIngredients, tvCookingMethod, tvSubTime;

            ViewHolder(View view) {
                super(view);
                ivImage = view.findViewById(R.id.iv_recipe_image);
                tvName = view.findViewById(R.id.tv_recipe_name);
                tvIngredients = view.findViewById(R.id.tv_recipe_ingredients);
                tvCookingMethod = view.findViewById(R.id.tv_recipe_cooking_method);
                tvSubTime = view.findViewById(R.id.tv_sub_time);
            }
        }
    }
}
