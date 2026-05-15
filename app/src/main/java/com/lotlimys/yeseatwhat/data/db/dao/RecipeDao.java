package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Query;

import com.lotlimys.yeseatwhat.data.db.entity.GeneratedRecipe;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert(onConflict = REPLACE)
    void insert(GeneratedRecipe recipe);

    @Query("SELECT * FROM generated_recipe WHERE recipe_key = :recipeKey")
    GeneratedRecipe getByKey(String recipeKey);

    @Query("SELECT * FROM generated_recipe ORDER BY created_at DESC")
    List<GeneratedRecipe> getAllByTimeDesc();
}
