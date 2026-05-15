# Yes eat What - 接口契约文档

## 包结构
```
com.lotlimys.yeseatwhat/
├── App.java
├── MainActivity.java
├── data/
│   ├── db/
│   │   ├── AppDatabase.java
│   │   ├── entity/ (Category, SystemIngredient, CustomIngredient, GeneratedRecipe, Favorite, History)
│   │   ├── dao/ (CategoryDao, IngredientDao, RecipeDao, FavoriteDao, HistoryDao)
│   │   └── seed/DataSeeder.java
│   ├── preference/AppPreferences.java
│   └── repository/ (IngredientRepository, RecipeRepository, FavoriteRepository, HistoryRepository)
├── ai/
│   ├── AIService.java
│   ├── OpenAIService.java
│   ├── RecipeRequest.java
│   ├── RecipeResponse.java
│   └── PortraitRequest.java
├── ui/
│   ├── home/HomeFragment.java
│   ├── order/OrderFragment.java
│   ├── profile/ProfileFragment.java
│   ├── detail/RecipeDetailActivity.java
│   ├── favorites/FavoritesActivity.java
│   ├── history/HistoryActivity.java
│   ├── portrait/PortraitActivity.java
│   └── settings/SettingsActivity.java
├── adapter/ (IngredientAdapter, CategoryAdapter, RecipeResultAdapter, HistoryFavoriteAdapter)
├── model/ (CategoryItem, IngredientItem, RecipeItem)
└── util/ (ImageCacheManager, Constants, DateUtils, StringUtils)
```

## 数据层契约

### Entity 定义
```
Category: id(int PK), name(String), icon(String), type(String: SYSTEM/CUSTOM), sortOrder(int)
SystemIngredient: id(int PK), name(String), icon(String), categoryId(int), sortOrder(int)
CustomIngredient: id(int PK), name(String), icon(String), categoryId(int), createdAt(long)
GeneratedRecipe: recipeKey(String PK), name(String), imagePath(String), ingredientsJson(String),
    stepsJson(String), cookingMethod(String), cookingTime(int), difficulty(String), calories(int),
    cuisineType(String), region(String), mealType(String), matchType(String: EXACT/TRY),
    matchReason(String), aiDisclaimer(String), createdAt(long)
Favorite: id(int PK), recipeKey(String), createdAt(long)
History: id(int PK), recipeKey(String), viewedAt(long)
```

### DAO 方法签名
```
CategoryDao:
  List<Category> getAll()
  List<Category> getByType(String type)
  void insert(Category... categories)
  void update(Category category)
  void delete(Category category)

IngredientDao:
  List<SystemIngredient> getAllSystem()
  List<CustomIngredient> getAllCustom()
  List<SystemIngredient> getSystemByCategory(int categoryId)
  List<CustomIngredient> getCustomByCategory(int categoryId)
  void insertSystem(SystemIngredient... ingredients)
  void insertCustom(CustomIngredient... ingredients)
  void deleteCustom(CustomIngredient ingredient)

RecipeDao:
  void insert(GeneratedRecipe recipe)
  GeneratedRecipe getByKey(String recipeKey)
  List<GeneratedRecipe> getAll()
  @Query("SELECT * FROM generated_recipe ORDER BY created_at DESC")
  List<GeneratedRecipe> getAllByTimeDesc()

FavoriteDao:
  List<Favorite> getAllOrderByTimeDesc()
  List<Favorite> getAllOrderByTimeAsc()
  @Query搜name: List<Favorite> searchByName(String keyword, String sortOrder)
  void insert(Favorite favorite)
  void deleteByRecipeKey(String recipeKey)
  boolean exists(String recipeKey)

HistoryDao:
  @Query limit 30天: List<History> getRecentOrderByTimeDesc()
  List<History> getRecentOrderByTimeAsc()
  @Query搜name: List<History> searchByName(String keyword, String sortOrder)
  void insert(History history)
  void deleteAll()
```

## AI 层契约

```
AIService interface:
  void generateRecipes(RecipeRequest request, Callback<RecipeResponse> callback)
  void generateRecipeImage(String dishName, Callback<String> callback) // 返回图片URL
  void generateRegionRecommendation(String region, Callback<List<RecipeItem>> callback)
  void generateRandomDishes(List<Integer> ingredientIds, Callback<List<RecipeItem>> callback)
  void generatePortrait(PortraitRequest request, Callback<String> callback) // 返回json字符串

RecipeRequest:
  List<Integer> ingredientIds, String cuisineType, String mealType,
  List<String> dietaryRestrictions, List<String> allergies, List<String> others,
  String selfDescription

RecipeResponse:
  List<RecipeItem> exactMatches, List<RecipeItem> tryThese
```

## Repository 契约
```
IngredientRepository:
  LiveData<List<Category>> getCategories()
  LiveData<List<IngredientItem>> getIngredients(boolean excludeByPreference)
  void addCategory(Category category)
  void addIngredient(CustomIngredient ingredient)
  void deleteCategory(Category category)
  void deleteIngredient(CustomIngredient ingredient)
  List<Integer> getExcludedIngredientIds()

RecipeRepository:
  void generateRecipes(RecipeRequest request, AIService.Callback<RecipeResponse> callback)
  GeneratedRecipe getRecipeDetail(String recipeKey)
  void generateRandomDishes(AIService.Callback<List<RecipeItem>> callback)
  void generateRegionRecommendation(String region, AIService.Callback<List<RecipeItem>> callback)

FavoriteRepository:
  LiveData<List<RecipeItem>> getFavorites(String keyword, String sortOrder)
  void addFavorite(String recipeKey)
  void removeFavorite(String recipeKey)
  boolean isFavorite(String recipeKey)

HistoryRepository:
  LiveData<List<RecipeItem>> getHistory(String keyword, String sortOrder)
  void recordView(String recipeKey)
  void clearHistory()
```

## UI 契约

### View ID 前缀命名
- ingredient_xxx: 食材相关
- category_xxx: 分类相关
- recipe_xxx: 菜品相关
- btn_xxx: 按钮
- tv_xxx: TextView
- rv_xxx: RecyclerView
- iv_xxx: ImageView
- et_xxx: EditText

### 导航
- MainActivity: 底部三个Tab (首页/点菜/个人) + fragment容器
- 跳转Detail: startActivity(RecipeDetailActivity, recipeKey)
- 跳转Favorites: startActivity(FavoritesActivity)
- 跳转History: startActivity(HistoryActivity)
- 跳转Portrait: startActivity(PortraitActivity)
- 跳转Settings: startActivity(SettingsActivity)

### Model
```
IngredientItem: int id, String name, String icon, int categoryId, String categoryName, boolean isSelected, boolean isCustom
CategoryItem: int id, String name, String icon, String type
RecipeItem: String recipeKey, String name, String imagePath, List<IngredientAmount> ingredients,
    String cookingMethod, int cookingTime, String difficulty, int calories, String cuisineType,
    String region, String mealType, String matchType, String matchReason,
    List<IngredientAmount> missingIngredients, long createdAt, boolean isFavorite
```

## 主题色定义
- pink(默认), white, black, blue, green, yellow, red
- AppPreferences 存储 theme key, MainActivity 读取并 apply
