package com.lotlimys.yeseatwhat.ui.detail;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lotlimys.yeseatwhat.App;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.ai.AIService;
import com.lotlimys.yeseatwhat.ai.DashScopeService;
import com.lotlimys.yeseatwhat.ai.OpenAIService;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.data.repository.HistoryRepository;
import com.lotlimys.yeseatwhat.model.RecipeItem;
import com.lotlimys.yeseatwhat.util.Constants;
import com.lotlimys.yeseatwhat.util.ImageFileManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RecipeDetailActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private RecyclerView rvRecipeList;
    private List<RecipeItem> dishes;
    private Gson gson = new Gson();
    private HistoryRepository historyRepository;
    private AppPreferences appPrefs;
    private AIService aiService;
    private ExecutorService imageExecutor;
    private RecipeListAdapter adapter;
    private ImageFileManager imageFileManager;

    // Detail views
    private ImageView ivImage;
    private TextView tvName, tvDifficulty, tvTime, tvCalories;
    private TextView tvMealType, tvCuisine;
    private TextView tvIngredients, tvSteps, tvMatchReason, tvDisclaimer;
    private ImageButton btnFavorite;
    private RecipeItem currentRecipe;
    private boolean isFavorite = false;
    private boolean directDetail = false;
    private volatile boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        historyRepository = new HistoryRepository(this);

        // AI service for image generation
        appPrefs = AppPreferences.getInstance(this);
        String provider = appPrefs.getAiProvider();
        if (Constants.PROVIDER_QWEN.equals(provider)) {
            aiService = new DashScopeService(appPrefs);
        } else {
            aiService = new OpenAIService(appPrefs);
        }
        imageExecutor = Executors.newSingleThreadExecutor();
        imageFileManager = new ImageFileManager(this);

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
        toolbarDetail.setNavigationOnClickListener(v -> {
            if (directDetail) {
                finish();
            } else {
                viewFlipper.showPrevious();
            }
        });

        // 记录是否来自浏览记录/收藏等直接详情入口
        directDetail = getIntent().getBooleanExtra("direct_detail", false);

        // 系统返回键：详情页 → 返回列表；列表页 → 关闭页面
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (viewFlipper.getDisplayedChild() == 1) {
                    // directDetail 模式：从详情直接结束页面，不回到列表
                    if (directDetail) {
                        finish();
                    } else {
                        viewFlipper.showPrevious();
                    }
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
            adapter = new RecipeListAdapter(dishes);
            rvRecipeList.setAdapter(adapter);

            // Start parallel image generation for each dish
            generateAllImages();

            // 如果来自浏览记录等直接详情入口，跳过列表直接显示详情
            if (getIntent().getBooleanExtra("direct_detail", false) && dishes.size() == 1) {
                showDetail(dishes.get(0));
            }
        }
    }

    private void showDetail(RecipeItem recipe) {
        this.currentRecipe = recipe;

        // Load image: 优先本地路径，其次 URL，最后占位图
        String imagePath = recipe.getImagePath();
        if (imagePath == null || imagePath.isEmpty()) {
            // 尝试从本地文件加载（DB 中可能只有 URL 或无路径）
            String localPath = imageFileManager.getLocalPath(recipe.getRecipeKey());
            if (localPath != null) {
                imagePath = localPath;
                recipe.setImagePath(localPath);
            }
        }
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.apic)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivImage);
        } else {
            ivImage.setImageResource(R.drawable.apic);
        }

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

    private void generateAllImages() {
        if (dishes == null || aiService == null) return;
        Log.d("RecipeImage", "开始串行生成 " + dishes.size() + " 张菜品图片");
        imageExecutor.execute(() -> {
            for (int i = 0; i < dishes.size(); i++) {
                if (destroyed) {
                    Log.d("RecipeImage", "Activity已销毁，停止图片生成");
                    return;
                }
                final int index = i;
                final RecipeItem recipe = dishes.get(i);
                final CountDownLatch latch = new CountDownLatch(1);
                try {
                    Log.d("RecipeImage", "[" + index + "] 开始生成: " + recipe.getName());
                    aiService.generateRecipeImage(recipe, new AIService.Callback<String>() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            if (destroyed) {
                                latch.countDown();
                                return;
                            }
                            Log.d("RecipeImage", "[" + index + "] 生成成功: " + recipe.getName() + " -> " + imageUrl);
                            // 保存图片到本地存储
                            String localPath = imageFileManager.saveImageFromUrl(imageUrl, recipe.getRecipeKey());
                            if (localPath != null) {
                                recipe.setImagePath(localPath);
                                // 更新数据库中的图片路径（用 applicationContext 避免持有已销毁的 activity）
                                AppDatabase.getDatabaseWriteExecutor().execute(() ->
                                    AppDatabase.getInstance(getApplicationContext())
                                            .recipeDao().updateImagePath(recipe.getRecipeKey(), localPath));
                                Log.d("RecipeImage", "[" + index + "] 已保存到本地: " + localPath);
                            } else {
                                recipe.setImagePath(imageUrl);
                            }
                            runOnUiThread(() -> {
                                if (destroyed) return;
                                RecyclerView.ViewHolder holder = rvRecipeList.findViewHolderForAdapterPosition(index);
                                if (holder instanceof RecipeListAdapter.ViewHolder) {
                                    Glide.with(RecipeDetailActivity.this)
                                            .load(recipe.getImagePath())
                                            .placeholder(R.drawable.apic)
                                            .transition(DrawableTransitionOptions.withCrossFade())
                                            .into(((RecipeListAdapter.ViewHolder) holder).ivImage);
                                } else {
                                    adapter.notifyItemChanged(index);
                                }
                            });
                            latch.countDown();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("RecipeImage", "[" + index + "] 生成失败: " + recipe.getName() + " - " + errorMessage);
                            latch.countDown();
                        }
                    });
                    // 真正等待当前图片生成完成，避免并发触发阿里云限流
                    latch.await(120, TimeUnit.SECONDS);
                    // 每张完成后额外等待，避免每分钟请求数超限
                    if (!destroyed && i < dishes.size() - 1) {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    }
                } catch (Exception e) {
                    if (destroyed) return;
                    Log.e("RecipeImage", "[" + index + "] 异常: " + recipe.getName() + " - " + e.getMessage(), e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        if (imageExecutor != null && !imageExecutor.isShutdown()) {
            imageExecutor.shutdownNow();
        }
        super.onDestroy();
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

            // Load image with Glide
            if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                Glide.with(RecipeDetailActivity.this)
                        .load(item.getImagePath())
                        .placeholder(R.drawable.apic)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.drawable.apic);
            }

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
