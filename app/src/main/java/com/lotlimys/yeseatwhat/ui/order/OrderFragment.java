package com.lotlimys.yeseatwhat.ui.order;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.lotlimys.yeseatwhat.R;
import com.lotlimys.yeseatwhat.adapter.IngredientAdapter;
import com.lotlimys.yeseatwhat.ai.AIService;
import com.lotlimys.yeseatwhat.ai.DashScopeService;
import com.lotlimys.yeseatwhat.ai.OpenAIService;
import com.lotlimys.yeseatwhat.ai.RecipeRequest;
import com.lotlimys.yeseatwhat.ai.RecipeResponse;
import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.entity.Category;
import com.lotlimys.yeseatwhat.data.db.entity.CustomIngredient;
import com.lotlimys.yeseatwhat.data.db.entity.GenerationRecord;
import com.lotlimys.yeseatwhat.data.preference.AppPreferences;
import com.lotlimys.yeseatwhat.data.repository.IngredientRepository;
import com.lotlimys.yeseatwhat.data.repository.RecipeRepository;
import com.lotlimys.yeseatwhat.model.CategoryItem;
import com.lotlimys.yeseatwhat.model.CuisineItem;
import com.lotlimys.yeseatwhat.model.IngredientItem;
import com.lotlimys.yeseatwhat.model.SelectableItem;
import com.lotlimys.yeseatwhat.ui.detail.RecipeDetailActivity;
import com.lotlimys.yeseatwhat.util.Constants;
import com.lotlimys.yeseatwhat.util.IngredientImageHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class OrderFragment extends Fragment {

    private IngredientRepository ingredientRepository;
    private RecipeRepository recipeRepository;
    private IngredientAdapter adapter;
    private RecyclerView rvIngredients;
    private TextView tvSelectedCount;
    private TextView tvOrderTitle;
    private TextView tvModeSubtitle;
    private FloatingActionButton btnGenerate;
    private ViewGroup llCategoryTabs;
    private LinearLayout llSpeedDial;
    private ImageButton btnToggleSpeedDial;

    private boolean isSpeedDialOpen = false;

    private List<CategoryItem> categories = new ArrayList<>();
    private List<IngredientItem> allIngredients = new ArrayList<>();
    private int selectedCategoryId = -1;

    // Tool data
    private List<CuisineItem> cookingMethods = new ArrayList<>();
    private List<CuisineItem> cuisines = new ArrayList<>();
    private List<CuisineItem> mealTypeItems = new ArrayList<>();
    private List<CuisineItem> sceneItems = new ArrayList<>();
    private List<CategoryItem> cuisineCategories = new ArrayList<>();
    private List<String> selectedCookingMethods = new ArrayList<>();
    private List<String> selectedCuisines = new ArrayList<>();
    private List<String> selectedMealTypes = new ArrayList<>();

    // Free-text preferences from tool dialog
    private String preferenceText = "";
    private String allergyText = "";

    // Current generation record ID (for update after AI returns)
    private long currentGenerationRecordId;

    // Mode
    private enum Mode { INGREDIENT, COOKING_METHOD, CUISINE, MEAL_TYPE, SCENE }
    private Mode currentMode = Mode.INGREDIENT;

    // Existing fields for the original preferences dialog
    private String selectedCuisine = "";
    private String selectedMealType = "";
    private List<String> selectedDietary = new ArrayList<>();
    private List<String> selectedAllergies = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        tvOrderTitle = view.findViewById(R.id.tv_order_title);
        tvSelectedCount = view.findViewById(R.id.tv_selected_count);
        tvModeSubtitle = view.findViewById(R.id.tv_mode_subtitle);
        rvIngredients = view.findViewById(R.id.rv_ingredients);
        btnGenerate = view.findViewById(R.id.btn_generate);
        llCategoryTabs = view.findViewById(R.id.ll_category_tabs);
        llSpeedDial = view.findViewById(R.id.ll_speed_dial);
        btnToggleSpeedDial = view.findViewById(R.id.btn_toggle_speed_dial);

        rvIngredients.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new IngredientAdapter();
        adapter.setOnItemClickListener(this::onItemClick);
        rvIngredients.setAdapter(adapter);
        applyThemeToAdapter();

        ingredientRepository = new IngredientRepository(requireContext());
        recipeRepository = new RecipeRepository(requireContext());

        initToolData();
        setupSpeedDial();
        btnToggleSpeedDial.setOnClickListener(v -> toggleSpeedDial());

        loadData();

        btnGenerate.setOnClickListener(v -> showPreferencesDialog());

        setupOverflowMenu(view);

        return view;
    }

    // ===== Data Initialization =====

    private void initToolData() {
        initCookingMethods();
        initCuisines();
        initCuisineCategories();
        initMealTypes();
        initScenes();
    }

    private void initMealTypes() {
        String[] names = {"早餐", "午餐", "晚餐", "宵夜", "下午茶"};
        for (int i = 0; i < names.length; i++) {
            mealTypeItems.add(new CuisineItem(i, names[i], "meal_" + i, -1, false, "meal_type"));
        }
    }

    private void initScenes() {
        String[] names = {"家常便饭", "朋友聚餐", "烛光晚餐", "宴请宾客", "野餐", "减脂餐", "加班简餐", "工作快餐", "观赛搭子"};
        for (int i = 0; i < names.length; i++) {
            sceneItems.add(new CuisineItem(i, names[i], "scene_" + i, -1, false, "scene"));
        }
    }

    private void initCookingMethods() {
        String[] methodNames = {"炒", "炸", "蒸", "煮", "烤", "煎", "炖", "凉拌", "煲汤", "火锅"};
        for (int i = 0; i < methodNames.length; i++) {
            cookingMethods.add(new CuisineItem(i, methodNames[i], "method_" + i, -1, false, "method"));
        }
    }

    private void initCuisineCategories() {
        cuisineCategories.add(new CategoryItem(0, "国内菜系", "", "cuisine_domestic"));
        cuisineCategories.add(new CategoryItem(1, "国外菜系", "", "cuisine_international"));
    }

    private void initCuisines() {
        int id = 0;
        String[][] domestic = {
                {"川菜", "sichuan"}, {"粤菜", "cantonese"}, {"湘菜", "hunan"},
                {"鲁菜", "shandong"}, {"苏菜", "jiangsu"}, {"浙菜", "zhejiang"},
                {"闽菜", "fujian"}, {"徽菜", "anhui"}, {"东北菜", "dongbei"},
                {"西北菜", "xibei"}, {"云南菜", "yunnan"}
        };
        for (String[] c : domestic) {
            cuisines.add(new CuisineItem(id++, c[0], c[1], 0, false, "cuisine"));
        }
        String[][] international = {
                {"美式", "american"}, {"意式", "italian"}, {"法式", "french"},
                {"日式", "japanese"}, {"韩式", "korean"}, {"泰式", "thai"},
                {"印度菜", "indian"}, {"墨西哥菜", "mexican"}, {"中东菜", "middle_eastern"}
        };
        for (String[] c : international) {
            cuisines.add(new CuisineItem(id++, c[0], c[1], 1, false, "cuisine"));
        }
    }

    private void loadData() {
        ingredientRepository.getCategories().observe(getViewLifecycleOwner(), categoryItems -> {
            categories.clear();
            categories.addAll(categoryItems);
            setupCategoryTabs();
        });

        ingredientRepository.getIngredients().observe(getViewLifecycleOwner(), ingredientItems -> {
            allIngredients.clear();
            allIngredients.addAll(ingredientItems);
            filterByCategory(-1);
        });
    }

    private void applyThemeToAdapter() {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        // Create a lighter version for selected background
        int red = (primaryColor >> 16) & 0xFF;
        int green = (primaryColor >> 8) & 0xFF;
        int blue = primaryColor & 0xFF;
        int bgColor = (int) (0xFF << 24) | ((red + (255 - red) * 75 / 100) << 16)
                | ((green + (255 - green) * 75 / 100) << 8)
                | (blue + (255 - blue) * 75 / 100);

        adapter.setThemeColors(bgColor, primaryColor, primaryColor);
    }

    // ===== Speed Dial =====

    private void setupSpeedDial() {
        llSpeedDial.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        addToolButton(inflater, "食材选择", R.drawable.ic_chicken, () -> {
            closeSpeedDial();
            switchToMode(Mode.INGREDIENT);
        });
        addToolButton(inflater, "烹饪方式选择", R.drawable.ic_wok, () -> {
            closeSpeedDial();
            switchToMode(Mode.COOKING_METHOD);
        });
        addToolButton(inflater, "偏好/忌口", R.drawable.ic_exclamation, () -> {
            closeSpeedDial();
            showPreferencesToolDialog();
        });
        addToolButton(inflater, "菜系", R.drawable.ic_share, () -> {
            closeSpeedDial();
            switchToMode(Mode.CUISINE);
        });
        addToolButton(inflater, "餐类选择", R.drawable.ic_plate, () -> {
            closeSpeedDial();
            switchToMode(Mode.MEAL_TYPE);
        });
        addToolButton(inflater, "食用场景", R.drawable.ic_scene, () -> {
            closeSpeedDial();
            switchToMode(Mode.SCENE);
        });
    }

    private void addToolButton(LayoutInflater inflater, String name, int iconRes, Runnable action) {
        View toolView = inflater.inflate(R.layout.item_tool_button, llSpeedDial, false);
        ((TextView) toolView.findViewById(R.id.tv_tool_name)).setText(name);
        ((ImageView) toolView.findViewById(R.id.iv_tool_icon)).setImageResource(iconRes);
        toolView.setOnClickListener(v -> action.run());
        llSpeedDial.addView(toolView);
    }

    private void toggleSpeedDial() {
        isSpeedDialOpen = !isSpeedDialOpen;
        llSpeedDial.setVisibility(isSpeedDialOpen ? View.VISIBLE : View.GONE);
        btnToggleSpeedDial.setRotation(isSpeedDialOpen ? 180f : 0f);
    }

    private void closeSpeedDial() {
        isSpeedDialOpen = false;
        llSpeedDial.setVisibility(View.GONE);
        btnToggleSpeedDial.setRotation(0f);
    }

    private void setGridSpanCount(int count) {
        if (rvIngredients.getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) rvIngredients.getLayoutManager()).setSpanCount(count);
        }
    }

    // ===== Mode Switching =====

    private void switchToMode(Mode mode) {
        currentMode = mode;
        tvModeSubtitle.setVisibility(View.GONE);

        switch (mode) {
            case INGREDIENT:
                tvOrderTitle.setText("选择食材");
                setGridSpanCount(4);
                setupIngredientCategoryTabs();
                filterByCategory(selectedCategoryId);
                updateSelectedCount();
                rvIngredients.setVisibility(View.VISIBLE);
                break;

            case COOKING_METHOD:
                tvOrderTitle.setText("烹饪方式选择");
                setGridSpanCount(4);
                llCategoryTabs.removeAllViews();
                tvSelectedCount.setText("");
                adapter.setItems(new ArrayList<>(cookingMethods));
                rvIngredients.setVisibility(View.VISIBLE);
                break;

            case CUISINE:
                tvOrderTitle.setText("选择菜系");
                setupCuisineCategoryTabs();
                tvSelectedCount.setText("");
                rvIngredients.setVisibility(View.VISIBLE);
                break;

            case MEAL_TYPE:
                tvOrderTitle.setText("餐类选择");
                llCategoryTabs.removeAllViews();
                tvSelectedCount.setText("");
                setGridSpanCount(3);
                adapter.setItems(new ArrayList<>(mealTypeItems));
                rvIngredients.setVisibility(View.VISIBLE);
                break;

            case SCENE:
                tvOrderTitle.setText("食用场景");
                llCategoryTabs.removeAllViews();
                tvSelectedCount.setText("");
                setGridSpanCount(3);
                adapter.setItems(new ArrayList<>(sceneItems));
                rvIngredients.setVisibility(View.VISIBLE);
                break;
        }
    }

    // ===== Category Tabs =====

    private void setupIngredientCategoryTabs() {
        llCategoryTabs.removeAllViews();

        Chip allChip = createChip("全部");
        allChip.setChecked(true);
        allChip.setOnClickListener(v -> {
            selectedCategoryId = -1;
            filterByCategory(-1);
            updateChipStyles(allChip);
        });
        llCategoryTabs.addView(allChip);

        for (CategoryItem cat : categories) {
            Chip chip = createChip(cat.getName());
            chip.setOnClickListener(v -> {
                selectedCategoryId = cat.getId();
                filterByCategory(cat.getId());
                updateChipStyles(chip);
            });
            llCategoryTabs.addView(chip);
        }
    }

    private void setupCuisineCategoryTabs() {
        llCategoryTabs.removeAllViews();

        for (CategoryItem cat : cuisineCategories) {
            Chip chip = createChip(cat.getName());
            chip.setOnClickListener(v -> {
                int catId = cat.getId();
                List<CuisineItem> filtered = new ArrayList<>();
                for (CuisineItem item : cuisines) {
                    if (item.getCategoryId() == catId) {
                        filtered.add(item);
                    }
                }
                adapter.setItems(filtered);
                updateChipStyles(chip);
            });
            llCategoryTabs.addView(chip);
        }

        // Select first category by default
        if (llCategoryTabs.getChildCount() > 0) {
            Chip first = (Chip) llCategoryTabs.getChildAt(0);
            first.setChecked(true);
            List<CuisineItem> filtered = new ArrayList<>();
            for (CuisineItem item : cuisines) {
                if (item.getCategoryId() == 0) {
                    filtered.add(item);
                }
            }
            adapter.setItems(filtered);
        }
    }

    private void setupCategoryTabs() {
        if (currentMode == Mode.INGREDIENT) {
            setupIngredientCategoryTabs();
        } else if (currentMode == Mode.CUISINE) {
            setupCuisineCategoryTabs();
        } else {
            llCategoryTabs.removeAllViews();
        }
    }

    private Chip createChip(String text) {
        Chip chip = (Chip) getLayoutInflater().inflate(
                R.layout.item_category_chip, llCategoryTabs, false);
        chip.setText(text);
        return chip;
    }

    private void updateChipStyles(Chip selectedChip) {
        for (int i = 0; i < llCategoryTabs.getChildCount(); i++) {
            Chip chip = (Chip) llCategoryTabs.getChildAt(i);
            chip.setChecked(chip == selectedChip);
        }
    }

    // ===== Data Filtering =====

    private void filterByCategory(int categoryId) {
        List<IngredientItem> filtered = new ArrayList<>();
        // 第一个位置为加号按钮（id=0）
        filtered.add(new IngredientItem(0, "+", "", -1, "", false, false));
        for (IngredientItem item : allIngredients) {
            if (categoryId == -1 || item.getCategoryId() == categoryId) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    // ===== Item Click =====

    private void onItemClick(SelectableItem item, int position) {
        // 加号按钮点击 -> 添加自定义食材
        if ("ingredient".equals(item.getCategoryType()) && item.getId() == 0) {
            showAddCustomIngredientDialog();
            return;
        }

        switch (currentMode) {
            case INGREDIENT:
                item.setSelected(!item.isSelected());
                adapter.notifyItemChanged(position);
                updateSelectedCount();
                break;

            case COOKING_METHOD:
            case MEAL_TYPE:
            case SCENE: {
                // Multi-select toggle
                item.setSelected(!item.isSelected());
                adapter.notifyItemChanged(position);
                break;
            }

            case CUISINE: {
                // Multi-select toggle for cuisines too
                item.setSelected(!item.isSelected());
                adapter.notifyItemChanged(position);
                break;
            }
        }
    }

    private void updateSelectedCount() {
        int count = 0;
        for (SelectableItem item : adapter.getItems()) {
            if (item.isSelected()) count++;
        }
        tvSelectedCount.setText("已选 " + count);
    }

    // ===== Overflow PopupMenu =====

    private void setupOverflowMenu(View rootView) {
        ImageButton btnOverflow = rootView.findViewById(R.id.btn_overflow);
        btnOverflow.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.setGravity(android.view.Gravity.END);
            popup.getMenuInflater().inflate(R.menu.menu_overflow, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    showDeleteCustomIngredientDialog();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void showDeleteCustomIngredientDialog() {
        List<IngredientItem> customItems = new ArrayList<>();
        for (IngredientItem item : allIngredients) {
            if (item.isCustom()) {
                customItems.add(item);
            }
        }

        if (customItems.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("暂无自定义食材")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        String[] names = new String[customItems.size()];
        for (int i = 0; i < customItems.size(); i++) {
            names[i] = customItems.get(i).getName();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择要删除的食材")
                .setItems(names, (dialog, which) -> {
                    IngredientItem target = customItems.get(which);
                    confirmDeleteCustomIngredient(target);
                })
                .setPositiveButton("取消", null)
                .show();
    }

    private void confirmDeleteCustomIngredient(IngredientItem target) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定要删除自定义食材「" + target.getName() + "」吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 按名称从数据库删除
                    ingredientRepository.deleteIngredientByName(target.getName());
                    // 从内存列表删除
                    allIngredients.remove(target);
                    filterByCategory(selectedCategoryId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ===== Add Custom Ingredient =====

    private void showAddCustomIngredientDialog() {
        List<CategoryItem> catList = new ArrayList<>(categories);
        float density = getResources().getDisplayMetrics().density;
        int p8 = (int) (8 * density);
        int p16 = (int) (16 * density);

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(p16, p8, p16, p8);

        // === Image preview + generate button ===
        LinearLayout imageArea = new LinearLayout(requireContext());
        imageArea.setOrientation(LinearLayout.HORIZONTAL);
        imageArea.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView ivPreview = new ImageView(requireContext());
        LinearLayout.LayoutParams previewLp = new LinearLayout.LayoutParams(
                (int) (100 * density), (int) (100 * density));
        previewLp.setMarginEnd(p16);
        ivPreview.setLayoutParams(previewLp);
        ivPreview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivPreview.setImageResource(R.drawable.apic);
        ivPreview.setBackgroundColor(android.graphics.Color.parseColor("#FFF0F0F0"));
        imageArea.addView(ivPreview);

        Button btnGenerate = new Button(requireContext());
        btnGenerate.setText("生成图片");
        btnGenerate.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageArea.addView(btnGenerate);

        layout.addView(imageArea);

        // Spacer
        View spacer1 = new View(requireContext());
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, p16));
        layout.addView(spacer1);

        // 分类标签
        TextView tvCatLabel = new TextView(requireContext());
        tvCatLabel.setText("分类：");
        tvCatLabel.setTextSize(14);
        layout.addView(tvCatLabel);

        // 分类下拉
        Spinner spinner = new Spinner(requireContext());
        List<String> spinnerItems = new ArrayList<>();
        for (CategoryItem cat : catList) {
            spinnerItems.add(cat.getName());
        }
        spinnerItems.add("+ 新增分类");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, spinnerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        layout.addView(spinner);

        // Spacer
        View spacer2 = new View(requireContext());
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, p16));
        layout.addView(spacer2);

        // 食材名字标签
        TextView tvNameLabel = new TextView(requireContext());
        tvNameLabel.setText("食材名字：");
        tvNameLabel.setTextSize(14);
        layout.addView(tvNameLabel);

        // 食材名字输入
        EditText etName = new EditText(requireContext());
        etName.setHint("输入食材名称");
        etName.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(etName);

        // 处理 "+ 新增分类" 选择
        final boolean[] initGuard = {true};
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (initGuard[0]) {
                    initGuard[0] = false;
                    return;
                }
                if (pos == spinnerItems.size() - 1) {
                    showNewCategoryDialog(catList, spinnerItems, spinnerAdapter, spinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // === Track generated image path ===
        final String[] generatedImagePath = {null};

        // === Generate image button handler ===
        btnGenerate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            int pos = spinner.getSelectedItemPosition();
            if (pos < 0 || pos >= catList.size()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提示")
                        .setMessage("请先选择分类")
                        .setPositiveButton("确定", null)
                        .show();
                return;
            }
            if (name.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提示")
                        .setMessage("请先输入食材名称")
                        .setPositiveButton("确定", null)
                        .show();
                return;
            }

            CategoryItem selectedCat = catList.get(pos);
            AppPreferences prefs = AppPreferences.getInstance(requireContext());
            String apiKey = prefs.getApiKey();
            String aiProvider = prefs.getAiProvider();
            if (apiKey.isEmpty() || aiProvider.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("提示")
                        .setMessage("请先在个人页-设置中配置 AI 供应商和 API Key")
                        .setPositiveButton("确定", null)
                        .show();
                return;
            }

            AlertDialog loadingDialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("正在生成图片")
                    .setMessage("AI 正在为「" + name + "」生成食材图标...")
                    .setCancelable(false)
                    .show();

            Log.d("IngredientImg", "AI供应商: " + aiProvider + ", API Key前8位: "
                    + (apiKey.length() > 8 ? apiKey.substring(0, 8) : apiKey) + "...");
            Log.d("IngredientImg", "生成食材图标: name=" + name + ", category=" + selectedCat.getName());

            AIService aiService = Constants.PROVIDER_QWEN.equals(aiProvider)
                    ? new DashScopeService(prefs)
                    : new OpenAIService(prefs);

            aiService.generateIngredientImage(name, selectedCat.getName(),
                    new AIService.Callback<String>() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            Log.d("IngredientImg", "图片生成成功, URL: " + imageUrl);
                            if (!isAdded()) return;
                            // onSuccess 运行在后台线程，在这里进行网络下载
                            IngredientImageHelper imageHelper =
                                    new IngredientImageHelper(requireContext());
                            String localPath =
                                    imageHelper.saveImageFromUrl(imageUrl);
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                if (localPath != null) {
                                    generatedImagePath[0] = localPath;
                                    Glide.with(requireContext())
                                            .load(localPath)
                                            .placeholder(R.drawable.apic)
                                            .into(ivPreview);
                                } else {
                                    ivPreview.setImageResource(R.drawable.apic);
                                    new MaterialAlertDialogBuilder(requireContext())
                                            .setTitle("提示")
                                            .setMessage("图片已生成但保存失败，将使用默认图标")
                                            .setPositiveButton("确定", null)
                                            .show();
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.e("IngredientImg", "图片生成失败: " + errorMessage);
                            requireActivity().runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                new MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("生成失败")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("确定", null)
                                        .show();
                            });
                        }
                    });
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("没有想要的食材，自己添加吧")
                .setView(layout)
                .setPositiveButton("确认添加", (dialog, which) -> {
                    int pos = spinner.getSelectedItemPosition();
                    if (pos < 0 || pos >= catList.size()) return;
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;

                    // 检查食材名称是否已存在
                    for (IngredientItem existing : allIngredients) {
                        if (existing.getName().equals(name)) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("提示")
                                    .setMessage("食材\"" + name + "\"已存在")
                                    .setPositiveButton("确定", null)
                                    .show();
                            return;
                        }
                    }

                    CategoryItem selectedCat = catList.get(pos);
                    int catId = selectedCat.getId();
                    String catName = selectedCat.getName();

                    // 如果分类是新创建的（负 ID），持久化到 DB
                    if (catId < 0) {
                        catId = 100 + Math.abs(catId);
                        Category category = new Category(catId, catName, "", "CUSTOM", 100);
                        ingredientRepository.addCategory(category);
                    }

                    // 持久化食材到 DB（包含图片路径）
                    long now = System.currentTimeMillis();
                    String iconPath = generatedImagePath[0] != null
                            ? generatedImagePath[0] : "";
                    CustomIngredient customIngredient =
                            new CustomIngredient(name, iconPath, catId, now);
                    ingredientRepository.addIngredient(customIngredient);

                    // 立即更新内存列表
                    int newId = 1000 + allIngredients.size();
                    IngredientItem customItem = new IngredientItem(
                            newId, name, "",
                            catId, catName, false, true);
                    customItem.setImagePath(generatedImagePath[0]);
                    allIngredients.add(customItem);
                    filterByCategory(selectedCategoryId);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showNewCategoryDialog(List<CategoryItem> catList, List<String> spinnerItems,
                                        ArrayAdapter<String> adapter, Spinner spinner) {
        EditText input = new EditText(requireContext());
        input.setHint("输入分类名称");
        float density = getResources().getDisplayMetrics().density;
        input.setPadding((int) (16 * density), (int) (8 * density), (int) (16 * density), 0);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("新增分类")
                .setView(input)
                .setPositiveButton("确认", (d, w) -> {
                    String catName = input.getText().toString().trim();
                    if (catName.isEmpty()) return;
                    // 检查分类名称是否已存在
                    for (CategoryItem existing : catList) {
                        if (existing.getName().equals(catName)) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("提示")
                                    .setMessage("分类\"" + catName + "\"已存在")
                                    .setPositiveButton("确定", null)
                                    .show();
                            return;
                        }
                    }
                    int newCatId = -(catList.size() + 1);
                    catList.add(new CategoryItem(newCatId, catName, "", ""));
                    spinnerItems.add(spinnerItems.size() - 1, catName);
                    adapter.notifyDataSetChanged();
                    spinner.setSelection(spinnerItems.size() - 2);
                })
                .setNegativeButton("取消", (d, w) -> spinner.setSelection(0))
                .setOnCancelListener(d -> spinner.setSelection(0))
                .show();
    }

    // ===== Preferences Tool Dialog (free-text) =====

    private void showPreferencesToolDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padPx = (int) (getResources().getDisplayMetrics().density * 24);
        layout.setPadding(padPx, (int) (getResources().getDisplayMetrics().density * 12),
                padPx, (int) (getResources().getDisplayMetrics().density * 12));

        TextView tvPrefLabel = new TextView(requireContext());
        tvPrefLabel.setText("偏好（可填可不填）");
        tvPrefLabel.setTextSize(14);
        tvPrefLabel.setTextColor(getResources().getColor(R.color.purple_500));
        layout.addView(tvPrefLabel);

        EditText etPreference = new EditText(requireContext());
        etPreference.setHint("例如：喜欢吃辣的、口味偏清淡...");
        etPreference.setText(preferenceText);
        etPreference.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(etPreference);

        View spacer = new View(requireContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density * 16)));
        layout.addView(spacer);

        TextView tvAllergyLabel = new TextView(requireContext());
        tvAllergyLabel.setText("忌口（可填可不填）");
        tvAllergyLabel.setTextSize(14);
        tvAllergyLabel.setTextColor(getResources().getColor(R.color.purple_500));
        layout.addView(tvAllergyLabel);

        EditText etAllergy = new EditText(requireContext());
        etAllergy.setHint("例如：不吃香菜、对花生过敏...");
        etAllergy.setText(allergyText);
        etAllergy.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(etAllergy);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("偏好与忌口设置")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    preferenceText = etPreference.getText().toString().trim();
                    allergyText = etAllergy.getText().toString().trim();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ===== Selection Summary Dialog (before generating) =====

    private List<IngredientItem> getSelectedIngredients() {
        List<IngredientItem> selected = new ArrayList<>();
        for (IngredientItem item : allIngredients) {
            if (item.isSelected()) selected.add(item);
        }
        return selected;
    }

    private void showPreferencesDialog() {
        List<IngredientItem> selected = getSelectedIngredients();
        if (selected.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("请先选择至少一种食材")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        // Collect all selections
        StringBuilder sbIngredients = new StringBuilder();
        for (int i = 0; i < selected.size(); i++) {
            if (i > 0) sbIngredients.append("、");
            sbIngredients.append(selected.get(i).getName());
        }

        StringBuilder sbMethods = new StringBuilder();
        for (CuisineItem m : cookingMethods) {
            if (m.isSelected()) {
                if (sbMethods.length() > 0) sbMethods.append("、");
                sbMethods.append(m.getName());
            }
        }

        StringBuilder sbCuisines = new StringBuilder();
        for (CuisineItem c : cuisines) {
            if (c.isSelected()) {
                if (sbCuisines.length() > 0) sbCuisines.append("、");
                sbCuisines.append(c.getName());
            }
        }

        StringBuilder sbMealTypes = new StringBuilder();
        for (CuisineItem mt : mealTypeItems) {
            if (mt.isSelected()) {
                if (sbMealTypes.length() > 0) sbMealTypes.append("、");
                sbMealTypes.append(mt.getName());
            }
        }

        StringBuilder sbScenes = new StringBuilder();
        for (CuisineItem s : sceneItems) {
            if (s.isSelected()) {
                if (sbScenes.length() > 0) sbScenes.append("、");
                sbScenes.append(s.getName());
            }
        }

        // Build dialog layout
        float density = getResources().getDisplayMetrics().density;
        int pad16 = (int) (16 * density);
        int pad8 = (int) (8 * density);
        int pad4 = (int) (4 * density);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad16, pad8, pad16, pad8);

        // Helper to add a summary row
        BiConsumer<String, String> addRow = (label, value) -> {
            if (value == null || value.isEmpty()) return;
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, pad4, 0, pad4);

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setText(label);
            tvLabel.setTextSize(14);
            tvLabel.setTextColor(getResources().getColor(R.color.purple_700));
            tvLabel.setTypeface(null, Typeface.BOLD);

            TextView tvValue = new TextView(requireContext());
            tvValue.setText(value);
            tvValue.setTextSize(14);
            tvValue.setTextColor(0xFF444444);
            tvValue.setPadding(pad8, 0, 0, 0);

            row.addView(tvLabel);
            row.addView(tvValue);
            root.addView(row);
        };

        addRow.accept("食材：", sbIngredients.toString());
        addRow.accept("烹饪方式：", sbMethods.toString());
        addRow.accept("餐类：", sbMealTypes.toString());
        addRow.accept("菜系：", sbCuisines.toString());
        addRow.accept("场景：", sbScenes.toString());
        addRow.accept("偏好：", preferenceText.isEmpty() ? null : preferenceText);
        addRow.accept("忌口：", allergyText.isEmpty() ? null : allergyText);

        // Wrap in ScrollView
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(root);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (360 * density)));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认信息")
                .setView(scrollView)
                .setPositiveButton("确认生成", (dialog, which) -> generateRecipes())
                .setNegativeButton("取消", null)
                .show();
    }

    // ===== Recipe Generation =====

    private void generateRecipes() {
        // Check API key and provider
        AppPreferences prefs = AppPreferences.getInstance(requireContext());
        String apiKey = prefs.getApiKey();
        String aiProvider = prefs.getAiProvider();

        if (apiKey.isEmpty() || aiProvider.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提示")
                    .setMessage("请先在个人页-设置中，选择 AI 供应商并填写有效的 API Key")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        List<IngredientItem> selected = getSelectedIngredients();
        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (IngredientItem item : selected) {
            ids.add(item.getId());
            names.add(item.getName());
        }

        // Collect cooking method selections
        selectedCookingMethods.clear();
        for (CuisineItem method : cookingMethods) {
            if (method.isSelected()) {
                selectedCookingMethods.add(method.getName());
            }
        }

        // Collect meal type selections
        selectedMealTypes.clear();
        StringBuilder mealTypeBuilder = new StringBuilder();
        for (CuisineItem mt : mealTypeItems) {
            if (mt.isSelected()) {
                selectedMealTypes.add(mt.getName());
                if (mealTypeBuilder.length() > 0) mealTypeBuilder.append("、");
                mealTypeBuilder.append(mt.getName());
            }
        }
        String mealTypeStr = mealTypeBuilder.toString();

        // Collect cuisine selections
        selectedCuisines.clear();
        StringBuilder cuisineBuilder = new StringBuilder();
        for (CuisineItem c : cuisines) {
            if (c.isSelected()) {
                selectedCuisines.add(c.getName());
                if (cuisineBuilder.length() > 0) cuisineBuilder.append("、");
                cuisineBuilder.append(c.getName());
            }
        }
        String cuisineStr = cuisineBuilder.toString();

        // Collect scene selections
        StringBuilder sceneBuilder = new StringBuilder();
        for (CuisineItem s : sceneItems) {
            if (s.isSelected()) {
                if (sceneBuilder.length() > 0) sceneBuilder.append("、");
                sceneBuilder.append(s.getName());
            }
        }
        String sceneStr = sceneBuilder.toString();

        // Build preference text from tools
        StringBuilder prefBuilder = new StringBuilder();
        if (!preferenceText.isEmpty()) {
            prefBuilder.append("偏好：").append(preferenceText);
        }
        if (!allergyText.isEmpty()) {
            if (prefBuilder.length() > 0) prefBuilder.append("；");
            prefBuilder.append("忌口：").append(allergyText);
        }

        RecipeRequest request = new RecipeRequest(
                ids, names,
                cuisineStr, mealTypeStr,
                selectedDietary, selectedAllergies,
                new ArrayList<>(), "",
                selectedCookingMethods,
                prefBuilder.toString(),
                prefs.getDietGoal(),
                sceneStr
        );

        // Save generation record before AI request
        long now = System.currentTimeMillis();
        String ingredientsJoined = String.join("、", names);
        String methodsJoined = String.join("、", selectedCookingMethods);
        String prefText = prefBuilder.toString();

        // Full AI prompt (same as sent to the model)
        String systemPrompt = OpenAIService.buildRecipeSystemPrompt(request);
        String userPrompt = OpenAIService.buildRecipeUserPrompt(request);
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        currentGenerationRecordId = now;
        GenerationRecord genRecord = new GenerationRecord(
                currentGenerationRecordId,
                ingredientsJoined,
                methodsJoined,
                cuisineStr,
                mealTypeStr,
                prefText,
                sceneStr,
                fullPrompt,
                0, // dishCount unknown yet
                now, // createdAt
                0, // completedAt unknown
                false // success unknown yet
        );
        AppDatabase.getDatabaseWriteExecutor().execute(() ->
                AppDatabase.getInstance(requireContext()).generationRecordDao().insert(genRecord)
        );

        btnGenerate.setEnabled(false);
        btnGenerate.setAlpha(0.5f);

        MaterialAlertDialogBuilder loadingBuilder = new MaterialAlertDialogBuilder(requireContext());
        loadingBuilder.setTitle("AI思考中...")
                .setMessage("正在根据您的食材生成菜品创意...")
                .setCancelable(false);
        AlertDialog dialog = loadingBuilder.show();

        long recordId = currentGenerationRecordId;
        recipeRepository.generateRecipes(request, new AIService.Callback<RecipeResponse>() {
            @Override
            public void onSuccess(RecipeResponse result) {
                // Update generation record with results
                int dishCount = result.getDishes() != null ? result.getDishes().size() : 0;
                AppDatabase.getDatabaseWriteExecutor().execute(() ->
                        AppDatabase.getInstance(requireContext()).generationRecordDao()
                                .updateResult(recordId, dishCount, System.currentTimeMillis(), true)
                );

                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    btnGenerate.setEnabled(true);
                    btnGenerate.setAlpha(1f);

                    Intent intent = new Intent(getActivity(),
                            RecipeDetailActivity.class);
                    intent.putExtra("dishes",
                            new Gson().toJson(result.getDishes()));
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Update generation record as failed
                AppDatabase.getDatabaseWriteExecutor().execute(() ->
                        AppDatabase.getInstance(requireContext()).generationRecordDao()
                                .updateResult(recordId, 0, System.currentTimeMillis(), false)
                );

                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    btnGenerate.setEnabled(true);
                    btnGenerate.setAlpha(1f);

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("生成失败")
                            .setMessage(errorMessage)
                            .setPositiveButton("确定", null)
                            .show();
                });
            }
        });
    }
}
