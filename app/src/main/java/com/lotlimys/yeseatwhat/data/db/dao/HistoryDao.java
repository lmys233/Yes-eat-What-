package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Query;
import androidx.room.Transaction;

import com.lotlimys.yeseatwhat.data.db.entity.History;
import com.lotlimys.yeseatwhat.data.db.entity.HistoryWithRecipe;

import java.util.List;

@Dao
public interface HistoryDao {
    @Transaction
    @Query("SELECT * FROM history WHERE viewed_at > :since ORDER BY viewed_at DESC")
    List<HistoryWithRecipe> getRecentHistory(long since);

    @Transaction
    @Query("SELECT * FROM history WHERE is_favorite = 1 ORDER BY favorited_at DESC")
    List<HistoryWithRecipe> getFavorites();

    @Query("SELECT * FROM history ORDER BY viewed_at DESC")
    List<History> getRecentOrderByTimeDesc();

    @Query("SELECT * FROM history ORDER BY viewed_at ASC")
    List<History> getRecentOrderByTimeAsc();

    @Query("SELECT h.* FROM history h INNER JOIN generated_recipe r ON h.recipe_key = r.recipe_key WHERE r.name LIKE '%' || :keyword || '%' ORDER BY h.viewed_at DESC")
    List<History> searchByName(String keyword);

    @Query("SELECT * FROM history WHERE recipe_key = :recipeKey LIMIT 1")
    History getByRecipeKey(String recipeKey);

    @Query("UPDATE history SET viewed_at = :time WHERE recipe_key = :recipeKey")
    void updateViewedAt(String recipeKey, long time);

    @Query("UPDATE history SET is_favorite = :isFavorite, favorited_at = :time WHERE recipe_key = :recipeKey")
    void updateFavoriteStatus(String recipeKey, boolean isFavorite, long time);

    @Insert(onConflict = REPLACE)
    void insert(History history);

    @Query("DELETE FROM history")
    void deleteAll();

    @Query("DELETE FROM history WHERE viewed_at < :beforeTime")
    void deleteOlderThan(long beforeTime);
}
