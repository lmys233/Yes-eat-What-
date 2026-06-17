# Yes eat What

## Always-On Skills

- change-record — 自动维护 change_record 目录，记录每天的代码修改和文件创建
- alignment-check — 当理解与需求出现偏差时自动引导对齐

## Communication Protocol

当用户描述需求时，如果出现以下情况，必须先反问确认再动手：

1. **理解有歧义** — 用户的描述可能有多种实现方式，列出选项让用户选择
2. **信息不完整** — 缺少关键细节（如数据来源、交互方式、边界条件），追问补齐
3. **方案有取舍** — 多个方案各有利弊（性能 vs 简洁、通用 vs 简单），说明 trade-off 后让用户定
4. **与现有逻辑冲突** — 用户的需求和已有代码的行为不一致，指出矛盾

反问的原则：用 1-2 句话 + 几个明确选项，不要长篇大论。

## 代码风格

> 通用编码规范已定义在 `~/.claude/rules/common/coding-style.md`
> Java 规范在 `~/.claude/rules/java/coding-style.md`
> Android 规范在 `~/.claude/rules/android/coding-style.md`
> 这些规则在所有项目中自动生效。

### 项目特有参考

写新文件之前，先找项目中同类的现有文件读 30 秒对齐风格，不要凭空创造新模式。

| 你要写什么 | 先看什么 |
|-----------|---------|
| 网络请求类 / AI 服务 | `DashScopeService.java` / `OpenAIService.java` |
| 工具类 | `util/` 下现有文件（如 `DateUtils.java`） |
| Activity | 其他 Activity（`RecipeDetailActivity.java`、`HistoryActivity.java`） |
| Fragment | `OrderFragment.java` / `ProfileFragment.java` |
| RecyclerView Adapter | `RecipeDetailActivity` 内部的 `RecipeListAdapter` |
| Layout XML | 同类型 layout（`activity_*.xml`、`item_*.xml`） |
