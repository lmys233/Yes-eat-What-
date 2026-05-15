package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Query;

import com.lotlimys.yeseatwhat.data.db.entity.Favorite;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM favorite ORDER BY created_at DESC")
    List<Favorite> getAllOrderByTimeDesc();

    @Query("SELECT * FROM favorite ORDER BY created_at ASC")
    List<Favorite> getAllOrderByTimeAsc();

    @Query("SELECT f.* FROM favorite f INNER JOIN generated_recipe r ON f.recipe_key = r.recipe_key WHERE r.name LIKE '%' || :keyword || '%' ORDER BY f.created_at DESC")
    List<Favorite> searchByName(String keyword);

    @Insert(onConflict = REPLACE)
    void insert(Favorite favorite);

    @Query("DELETE FROM favorite WHERE recipe_key = :recipeKey")
    void deleteByRecipeKey(String recipeKey);

    @Query("SELECT COUNT(*) > 0 FROM favorite WHERE recipe_key = :recipeKey")
    boolean exists(String recipeKey);
}
