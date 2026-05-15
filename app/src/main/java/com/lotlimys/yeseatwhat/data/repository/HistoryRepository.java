package com.lotlimys.yeseatwhat.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lotlimys.yeseatwhat.data.db.AppDatabase;
import com.lotlimys.yeseatwhat.data.db.dao.HistoryDao;
import com.lotlimys.yeseatwhat.data.db.entity.History;
import com.lotlimys.yeseatwhat.data.db.entity.HistoryWithRecipe;

import java.util.List;

public class HistoryRepository {

    private static final long SEVEN_DAYS = 7L * 86400000L;
    private static final long FOURTEEN_DAYS = 14L * 86400000L;

    private final HistoryDao historyDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public HistoryRepository(Context context) {
        this.historyDao = AppDatabase.getInstance(context).historyDao();
    }

    public interface HistoryCallback {
        void onResult(List<HistoryWithRecipe> history);
    }

    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    /**
     * lazy cleanup: 查询前先清除过期数据（>14天），通过回调返回结果
     */
    public void get7DayHistoryAsync(HistoryCallback callback) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            long fourteenDaysAgo = System.currentTimeMillis() - FOURTEEN_DAYS;
            historyDao.deleteOlderThan(fourteenDaysAgo);

            long sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS;
            List<HistoryWithRecipe> result = historyDao.getRecentHistory(sevenDaysAgo);

            mainHandler.post(() -> callback.onResult(result));
        });
    }

    /**
     * 获取收藏的菜品列表，按收藏时间倒序
     */
    public void getFavoritesAsync(HistoryCallback callback) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            List<HistoryWithRecipe> result = historyDao.getFavorites();
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    /**
     * 添加或更新浏览记录（后台线程执行）
     */
    public void addOrUpdate(String recipeKey) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            long now = System.currentTimeMillis();

            History existing = historyDao.getByRecipeKey(recipeKey);
            if (existing != null) {
                historyDao.updateViewedAt(recipeKey, now);
            } else {
                History history = new History(recipeKey, now);
                historyDao.insert(history);
            }
        });
    }

    /**
     * 切换收藏状态，通过回调返回新状态
     */
    public void toggleFavorite(String recipeKey, FavoriteCallback callback) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            long now = System.currentTimeMillis();
            History existing = historyDao.getByRecipeKey(recipeKey);
            if (existing != null) {
                boolean newStatus = !existing.isFavorite();
                historyDao.updateFavoriteStatus(recipeKey, newStatus, newStatus ? now : 0);
                mainHandler.post(() -> callback.onResult(newStatus));
            } else {
                // 无浏览记录时，新建一条并收藏
                History history = new History(recipeKey, now);
                history.setFavorite(true);
                history.setFavoritedAt(now);
                historyDao.insert(history);
                mainHandler.post(() -> callback.onResult(true));
            }
        });
    }

    /**
     * 检查某菜品是否已收藏
     */
    public void checkFavorite(String recipeKey, FavoriteCallback callback) {
        AppDatabase.getDatabaseWriteExecutor().execute(() -> {
            History existing = historyDao.getByRecipeKey(recipeKey);
            boolean isFav = existing != null && existing.isFavorite();
            mainHandler.post(() -> callback.onResult(isFav));
        });
    }
}
