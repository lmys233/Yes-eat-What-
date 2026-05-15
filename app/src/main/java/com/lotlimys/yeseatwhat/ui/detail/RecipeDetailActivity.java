package com.lotlimys.yeseatwhat.ui.detail;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.data.repository.HistoryRepository;
import com.lotlimys.yeseatwhat.model.RecipeItem;

import java.lang.reflect.Type;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private RecyclerView rvRecipeList;
    private List<RecipeItem> dishes;
    private Gson gson = new Gson();
    private HistoryRepository historyRepository;

    // Detail views
    private ImageView ivImage;
    private TextView tvName, tvDifficulty, tvTime, tvCalories;
    private TextView tvMealType, tvCuisine;
    private TextView tvIngredients, tvSteps, tvMatchReason, tvDisclaimer;
    private ImageButton btnFavorite;
    private RecipeItem currentRecipe;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        historyRepository = new HistoryRepository(this);

        // Parse data
        String dishesJson = getIntent().getStringExtra("dishes");
        if (dishesJson != null) {
            Type listType = new TypeToken<List<RecipeItem>>() {}.getType();
            dishes = gson.fromJson(dishesJson, listType);
        }

        viewFlipper = findViewById(R.id.view_flipper);
        rvRecipeList = findViewById(R.id.rv_recipe_list);

        // Toolbar - list
        MaterialToolbar toolbarList = findViewById(R.id.toolbar_list);
        toolbarList.setNavigationOnClickListener(v -> finish());

        // Toolbar - detail
        MaterialToolbar toolbarDetail = findViewById(R.id.toolbar_detail);
        toolbarDetail.setNavigationOnClickListener(v -> viewFlipper.showPrevious());

        // 系统返回键：详情页 → 返回列表；列表页 → 关闭页面
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewFlipper.getDisplayedChild() == 1) {
                    viewFlipper.showPrevious();
                } else {
                    finish();
                }
            }
        });

        // Detail views
        ivImage = findViewById(R.id.iv_recipe_detail_image);
        tvName = findViewById(R.id.tv_detail_name);
        tvDifficulty = findViewById(R.id.tv_detail_difficulty);
        tvTime = findViewById(R.id.tv_detail_time);
        tvCalories = findViewById(R.id.tv_detail_calories);
        tvMealType = findViewById(R.id.tv_detail_meal_type);
        tvCuisine = findViewById(R.id.tv_detail_cuisine);
        tvIngredients = findViewById(R.id.tv_detail_ingredients);
        tvSteps = findViewById(R.id.tv_detail_steps);
        tvMatchReason = findViewById(R.id.tv_detail_match_reason);
        tvDisclaimer = findViewById(R.id.tv_detail_disclaimer);
        btnFavorite = findViewById(R.id.btn_detail_favorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Setup list
        if (dishes != null && !dishes.isEmpty()) {
            rvRecipeList.setLayoutManager(new LinearLayoutManager(this));
            rvRecipeList.setAdapter(new RecipeListAdapter(dishes));

            // 如果来自浏览记录等直接详情入口，跳过列表直接显示详情
            if (getIntent().getBooleanExtra("direct_detail", false) && dishes.size() == 1) {
                showDetail(dishes.get(0));
            }
        }
    }

    private void showDetail(RecipeItem recipe) {
        this.currentRecipe = recipe;
        tvName.setText(recipe.getName());
        tvDifficulty.setText("难度: " + recipe.getDifficulty());
        tvTime.setText("时间: " + recipe.getCookingTime() + "分钟");
        tvCalories.setText("热量: " + recipe.getCalories() + "千卡");
        tvMealType.setText("餐类: " + (recipe.getMealType() != null ? recipe.getMealType() : "未指定"));
        tvCuisine.setText("菜系: " + (recipe.getCuisineType() != null ? recipe.getCuisineType() : "未指定")
                + (recipe.getRegion() != null ? " · " + recipe.getRegion() : ""));

        // Ingredients
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RecipeItem.IngredientAmount ing : recipe.getIngredients()) {
                sb.append("• ").append(ing.getName())
                        .append(" ").append(ing.getAmount() != null ? ing.getAmount() : "适量").append("\n");
            }
            tvIngredients.setText(sb.toString());
        } else {
            tvIngredients.setText("暂无食材信息");
        }

        // Steps
        if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (RecipeItem.CookingStep step : recipe.getSteps()) {
                sb.append(step.getStep()).append(". ").append(step.getContent()).append("\n\n");
            }
            tvSteps.setText(sb.toString());
        } else {
            tvSteps.setText("烹饪方式: " + (recipe.getCookingMethod() != null ? recipe.getCookingMethod() : "未指定"));
        }

        // Match reason (推荐理由)
        if (recipe.getMatchReason() != null && !recipe.getMatchReason().isEmpty()) {
            tvMatchReason.setText("💡 " + recipe.getMatchReason());
            tvMatchReason.setVisibility(View.VISIBLE);
        } else {
            tvMatchReason.setVisibility(View.GONE);
        }

        tvDisclaimer.setText("本食谱由AI生成，仅供参考。烹饪时间与食材用量可能因实际情况而异。");

        // 检查收藏状态
        checkFavoriteStatus(recipe.getRecipeKey());

        // 保存浏览记录
        if (recipe.getRecipeKey() != null && historyRepository != null) {
            historyRepository.addOrUpdate(recipe.getRecipeKey());
        }

        viewFlipper.showNext();
    }

    private void checkFavoriteStatus(String recipeKey) {
        if (recipeKey == null) return;
        historyRepository.checkFavorite(recipeKey, exists -> {
            isFavorite = exists;
            updateFavoriteButtonStyle();
        });
    }

    private void toggleFavorite() {
        if (currentRecipe == null || currentRecipe.getRecipeKey() == null) return;
        historyRepository.toggleFavorite(currentRecipe.getRecipeKey(), newStatus -> {
            isFavorite = newStatus;
            updateFavoriteButtonStyle();
        });
    }

    private void updateFavoriteButtonStyle() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_tintable);
            btnFavorite.setImageTintList(ColorStateList.valueOf(getThemeColor()));
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_outline);
            btnFavorite.setImageTintList(null);
        }
    }

    private int getThemeColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    // ===== Adapter =====

    private class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.ViewHolder> {

        private final List<RecipeItem> items;

        RecipeListAdapter(List<RecipeItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecipeItem item = items.get(position);
            holder.tvName.setText(item.getName());

            // Ingredients summary
            String summary = RecipeItem.toIngredientsSummary(item.getIngredients());
            holder.tvIngredients.setText("食材: " + (summary.isEmpty() ? "暂无" : summary));

            // Cooking method
            holder.tvCookingMethod.setText(item.getCookingMethod() != null ? item.getCookingMethod() : "");

            holder.itemView.setOnClickListener(v -> showDetail(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName, tvIngredients, tvCookingMethod;

            ViewHolder(View view) {
                super(view);
                ivImage = view.findViewById(R.id.iv_recipe_image);
                tvName = view.findViewById(R.id.tv_recipe_name);
                tvIngredients = view.findViewById(R.id.tv_recipe_ingredients);
                tvCookingMethod = view.findViewById(R.id.tv_recipe_cooking_method);
            }
        }
    }
}
