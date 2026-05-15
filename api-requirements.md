# Yes eat What - App 架构设计文档

> 版本：v1.0
> 架构：Android 单体应用（无远程后端）
> 数据层：Room + SharedPreferences
> 网络层：OkHttp（直连 AI API）
> 语言：Java

---

## 目录

1. [整体架构](#1-整体架构)
2. [数据层设计](#2-数据层设计)
3. [AI 接口设计](#3-ai-接口设计)
4. [本地存储设计](#4-本地存储设计)
5. [模块结构](#5-模块结构)

---

## 1. 整体架构

### 1.1 架构分层

```
┌──────────────────────────────────────────┐
│              UI 层 (Activity/Fragment)     │
├──────────────────────────────────────────┤
│           ViewModel / Repository          │
├──────────────────────────────────────────┤
│   Room DB  │  SharedPrefs  │  AI Service  │
├──────────────────────────────────────────┤
│     SQLite  │    XML/JSON  │   OkHttp     │
└──────────────────────────────────────────┘
```

### 1.2 数据流

```
用户操作 → Repository → Room (本地持久化)
                   → AIService (OkHttp → AI API)
                   → 回调更新 UI
```

### 1.3 AI API 直连

App 内置 API Key（个人项目），直接调用 AI 服务的 REST API：

- **文本生成** → 生成菜品列表、详情、用户画像等
- **图片生成** → 生成卡通菜品图片，保存到 app 本地缓存目录

---

## 2. 数据层设计

### 2.1 Room 实体

#### User 表 — 用户信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增主键 |
| username | String | 用户名 |
| nickname | String | 昵称 |
| avatar | String | 头像（本地文件路径） |
| region | String | 选择的地区 |
| selfDescription | String | 用户自述 |
| createdAt | long | 创建时间戳 |

> 因为是单体，用户信息实际只有"当前用户"一条数据，但保留表结构便于扩展。

#### SystemIngredient 表 — 系统默认食材

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增 |
| name | String | 食材名称 |
| icon | String | 图标标识（本地 drawable 资源名） |
| categoryId | int (FK) | 所属分类 ID |
| sortOrder | int | 排序序号 |

#### CustomIngredient 表 — 用户自定义食材

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增 |
| name | String | 食材名称 |
| icon | String | 图标标识 |
| categoryId | int (FK) | 所属分类 ID |
| createdAt | long | 创建时间 |

#### Category 表 — 食材分类

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增 |
| name | String | 分类名称 |
| icon | String | 图标标识 |
| type | String | SYSTEM / CUSTOM |
| sortOrder | int | 排序序号 |

#### GeneratedRecipe 表 — 生成的食谱

| 字段 | 类型 | 说明 |
|------|------|------|
| recipeKey | String (PK) | 唯一标识（`gen_{uuid}`） |
| name | String | 菜品名称 |
| imagePath | String | 本地图片路径 |
| ingredientsJson | String | 食材列表 (JSON) |
| stepsJson | String | 步骤列表 (JSON) |
| cookingMethod | String | 烹饪方式 |
| cookingTime | int | 烹饪时间(分钟) |
| difficulty | String | 难度 |
| calories | int | 热量 |
| cuisineType | String | 菜系 |
| region | String | 地区 |
| mealType | String | 餐型 |
| matchType | String | EXACT / TRY |
| matchReason | String | 匹配原因 |
| aiDisclaimer | String | AI 免责声明 |
| createdAt | long | 创建时间 |

#### Favorite 表 — 收藏

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增 |
| recipeKey | String (FK) | 食谱标识 |
| createdAt | long | 收藏时间 |

#### History 表 — 浏览历史

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int (PK) | 自增 |
| recipeKey | String (FK) | 食谱标识 |
| viewedAt | long | 浏览时间 |

### 2.2 SharedPreferences 存储

| Key | 类型 | 说明 |
|-----|------|------|
| `theme` | String | 主题色标识（pink / white / black / blue / green / yellow / red） |
| `region` | String | 用户选择的地区 |
| `dietary_restrictions` | String | 饮食限制（JSON Array 字符串） |
| `allergies` | String | 过敏源（JSON Array） |
| `preferred_cuisines` | String | 偏好菜系（JSON Array） |
| `preferred_meal_types` | String | 偏好餐型（JSON Array） |
| `excluded_ingredients` | String | 排除食材 ID（JSON Array） |
| `search_history` | String | 本地搜索历史（JSON Array，近 7 天） |
| `ai_api_key` | String | AI API Key |
| `ai_base_url` | String | AI API 地址 |
| `device_uuid` | String | 设备唯一标识 |

### 2.3 本地图片缓存

```
app内部存储目录 /images/
├── gen_{uuid}.jpg    ← AI 生成的菜品图片
├── rec_{uuid}.jpg    ← 推荐菜品图片
└── avatar.jpg        ← 用户头像
```

> 图片命名规则与 `recipeKey` 一致，方便映射。
> 限制：每张图片最大 512KB，生成时通过 API 参数控制图片尺寸（如 512x512）。

---

## 3. AI 接口设计

### 3.1 接口适配层

定义一个抽象的 `AIService` 接口，不绑定具体 AI 厂商，app 默认内置一种配置（如 OpenAI 兼容接口），用户也可在设置中更换 API 地址和 Key。

```java
public interface AIService {
    void generateRecipes(RecipeRequest request, Callback<RecipeResponse> callback);
    void generateRecipeImage(String dishName, Callback<ImageResponse> callback);
    void generateRegionRecommendation(String region, Callback<RecommendationResponse> callback);
    void generatePortrait(PortraitRequest request, Callback<PortraitResponse> callback);
}
```

### 3.2 生成食谱

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| ingredientIds | List\<Integer\> | 是 | 选中食材 ID |
| cuisineType | String | 否 | 菜系 |
| mealType | String | 否 | 餐型 |
| dietaryRestrictions | List\<String\> | 否 | 饮食限制（勾选 + 手动输入） |
| allergies | List\<String\> | 否 | 过敏源 |
| others | List\<String\> | 否 | 其他要求（自由输入列表，如"不要太辣""少油"等） |
| selfDescription | String | 否 | 用户自述（用于 AI 参考） |

**返回结果（解析 AI 文本生成的结构化 JSON）：**

```json
{
  "exactMatches": [
    {
      "name": "胡萝卜炒肉",
      "ingredients": [{"name": "胡萝卜", "amount": "200g"}, ...],
      "steps": [{"step": 1, "description": "..."}],
      "cookingMethod": "炒",
      "cookingTime": 15,
      "difficulty": "简单",
      "calories": 350,
      "cuisineType": "中餐",
      "region": "家常",
      "mealType": "晚餐",
      "matchReason": "已使用您选择的所有食材"
    }
  ],
  "tryThese": [...]
}
```

> 限制：`exactMatches` 最多 3 个，`tryThese` 最多 3 个，总计不超过 6 个菜品。

### 3.3 生成菜品图片

**请求参数：** 菜品名称（String）

**返回：** 图片 URL（从 AI 图片 API 获取）

> App 下载图片后保存到本地 `/images/gen_{uuid}.jpg`，显示时直接加载本地文件。
> 图片尺寸限制：512x512，格式 JPG。
> 在图片下载完成前，UI 显示占位图。

### 3.4 生成地区推荐

**请求参数：** 地区名称（String）

**返回：** 当日推荐菜品列表（同 3.2 格式，简化版，不含步骤）

> App 本地缓存推荐结果，每天首次打开首页时刷新，同一天同一地区不重复请求。

### 3.5 生成用户画像

**请求参数：** 用户的收藏列表 + 历史记录 + 用户自述

**返回：**

```json
{
  "summary": "根据您的记录分析，您偏好...",
  "tags": ["标签1", "标签2"],
  "stats": {
    "totalRecipesViewed": 45,
    "totalFavorites": 12,
    "topCuisineType": "湘菜",
    "topCookingMethod": "炒",
    "avgCookingTime": 25
  }
}
```

> 触发时机：每日凌晨或用户手动点击"刷新画像"时。

### 3.6 频率限制（App 端控制）

| 接口 | 冷却时间 | 说明 |
|------|---------|------|
| 生成食谱 | 无限制 | 按需调用 |
| 生成图片 | 无限制 | 按需调用 |
| 随机推荐"换一换" | **已登录 10s / 未登录 15s** | App 端计时器控制按钮状态 |
| 地区推荐 | 每天 1 次 | 同地区同天不重复请求 |

---

## 4. 本地存储设计

### 4.1 Room 数据库

**数据库名：** `yes_eat_what.db`

**版本管理：** 使用 Room 的 Migration 机制，版本号递增。

**预填充数据：** 首次安装时通过 `RoomDatabase.Callback` 插入系统默认食材和分类（使用 `createAllTables` 后的回调执行 `INSERT`）。

### 4.2 系统默认食材数据

**分类：**

| 分类 | 食材 |
|------|------|
| 蔬菜 | 胡萝卜、白菜、西兰花、菠菜、西红柿、黄瓜、土豆、茄子、青椒、洋葱、豆角、生菜、玉米、南瓜、豆腐 |
| 肉类 | 猪肉、牛肉、鸡肉、排骨、五花肉、鸡胸肉、羊肉、鸭肉、火腿 |
| 水产 | 鱼、虾、蟹、蛤蜊、鱿鱼、带鱼 |
| 蛋奶 | 鸡蛋、鸭蛋、牛奶、酸奶、奶酪 |
| 调味料 | 盐、生抽、老抽、醋、料酒、蚝油、糖、胡椒粉、辣椒、花椒、八角、桂皮、姜、蒜、葱 |
| 主食 | 大米、面条、面粉、糯米、小米、意面、米粉 |
| 干货 | 木耳、香菇、紫菜、海带、红枣、枸杞、花生、芝麻 |
| 水果 | 苹果、香蕉、橙子、柠檬、草莓、蓝莓、芒果 |

> 每个食材对应一个卡通图标（drawable 资源）。

### 4.3 数据初始化流程

```
App 启动 → Room 数据库检查
    ├── 首次安装 → 执行预填充（插入系统食材、分类、默认地区和示例数据）
    └── 已存在 → 正常使用
```

---

## 5. 模块结构

### 5.1 包结构

```
com.lotlimys.yeseatwhat/
├── App.java                          // Application 类，初始化
├── MainActivity.java                 // 主 Activity（底部导航容器）
│
├── data/
│   ├── db/
│   │   ├── AppDatabase.java          // Room 数据库
│   │   ├── entity/                   // 实体类
│   │   │   ├── Category.java
│   │   │   ├── SystemIngredient.java
│   │   │   ├── CustomIngredient.java
│   │   │   ├── GeneratedRecipe.java
│   │   │   ├── Favorite.java
│   │   │   └── History.java
│   │   ├── dao/                      // DAO 接口
│   │   │   ├── CategoryDao.java
│   │   │   ├── IngredientDao.java
│   │   │   ├── RecipeDao.java
│   │   │   ├── FavoriteDao.java
│   │   │   └── HistoryDao.java
│   │   └── seed/
│   │       └── DataSeeder.java       // 预填充数据
│   ├── preference/
│   │   └── AppPreferences.java       // SharedPreferences 封装
│   └── repository/
│       ├── IngredientRepository.java
│       ├── RecipeRepository.java
│       ├── FavoriteRepository.java
│       └── HistoryRepository.java
│
├── ai/
│   ├── AIService.java                // AI 接口抽象
│   ├── OpenAIService.java            // OpenAI 兼容实现
│   ├── RecipeRequest.java
│   ├── RecipeResponse.java
│   ├── ImageResponse.java
│   └── PortraitRequest.java
│
├── ui/
│   ├── home/
│   │   ├── HomeFragment.java         // 首页 Fragment
│   │   └── HomeViewModel.java
│   ├── order/
│   │   ├── OrderFragment.java        // 点菜 Fragment
│   │   └── OrderViewModel.java
│   ├── profile/
│   │   ├── ProfileFragment.java      // 个人中心 Fragment
│   │   └── ProfileViewModel.java
│   ├── detail/
│   │   ├── RecipeDetailActivity.java // 菜品详情页
│   │   └── RecipeDetailViewModel.java
│   ├── favorites/
│   │   ├── FavoritesActivity.java    // 收藏列表页
│   │   └── FavoritesViewModel.java
│   ├── history/
│   │   ├── HistoryActivity.java      // 历史记录页
│   │   └── HistoryViewModel.java
│   ├── portrait/
│   │   ├── PortraitActivity.java     // 用户画像页
│   │   └── PortraitViewModel.java
│   └── settings/
│       ├── SettingsActivity.java     // 设置页
│       └── SettingsViewModel.java
│
├── adapter/
│   ├── IngredientAdapter.java        // 食材网格适配器
│   ├── CategoryAdapter.java          // 分类切换适配器
│   ├── RecipeResultAdapter.java      // 生成结果列表适配器
│   └── HistoryFavoriteAdapter.java   // 收藏/历史列表适配器
│
├── model/
│   ├── IngredientItem.java
│   ├── CategoryItem.java
│   ├── RecipeItem.java
│   └── ...
│
└── util/
    ├── ImageCacheManager.java        // 本地图片缓存管理
    ├── DateUtils.java
    ├── StringUtils.java
    └── Constants.java                // 常量（API 地址、Key 等）
```

### 5.2 页面导航

```
MainActivity
├── BottomNavigationView
│   ├── Tab: 首页 → HomeFragment
│   │   ├── 地区轮播（点击地区修改 → 地区选择弹窗）
│   │   ├── 随机菜品推荐（换一换按钮）
│   │   └── [待定内容]
│   ├── Tab: 点菜 → OrderFragment
│   │   ├── [...] 设置按钮（忌口/偏好/其他输入弹窗）
│   │   ├── 食材分类 + 食材网格
│   │   └── 🍳 炒锅按钮（生成菜品）
│   └── Tab: 个人 → ProfileFragment
│       ├── 头像 / 昵称
│       ├── 收藏记录 → FavoritesActivity
│       ├── 历史记录 → HistoryActivity
│       └── ⚙ 设置 → SettingsActivity
│
RecipeDetailActivity (菜品详情)
├── 卡通图片 / 占位图
├── 菜名 / 菜系 / 地区 / 时间 / 难度 / 热量
├── 完整食材列表
├── 做法步骤（含 AI 免责声明）
└── 收藏按钮

FavoritesActivity (收藏列表)
├── 搜索框
├── 时间升降序切换
└── 菜品列表

HistoryActivity (历史记录)
├── 搜索框
├── 时间升降序切换
└── 菜品列表（近 30 天）

PortraitActivity (用户画像)
├── 用户自述输入框
└── 画像结果展示

SettingsActivity (设置)
├── 主题颜色选择
├── AI API 地址/Key
└── 其他设置
```
