package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lotlimys.yeseatwhat.data.db.entity.CustomIngredient;
import com.lotlimys.yeseatwhat.data.db.entity.SystemIngredient;

import java.util.List;

@Dao
public interface IngredientDao {
    @Query("SELECT * FROM system_ingredient ORDER BY sort_order ASC")
    List<SystemIngredient> getAllSystem();

    @Query("SELECT * FROM custom_ingredient ORDER BY created_at DESC")
    List<CustomIngredient> getAllCustom();

    @Query("SELECT * FROM system_ingredient WHERE category_id = :categoryId ORDER BY sort_order ASC")
    List<SystemIngredient> getSystemByCategory(int categoryId);

    @Query("SELECT * FROM custom_ingredient WHERE category_id = :categoryId ORDER BY created_at DESC")
    List<CustomIngredient> getCustomByCategory(int categoryId);

    @Insert
    void insertSystem(SystemIngredient... ingredients);

    @Insert
    void insertCustom(CustomIngredient... ingredients);

    @Delete
    void deleteCustom(CustomIngredient ingredient);

    @Query("DELETE FROM custom_ingredient WHERE name = :name")
    void deleteCustomByName(String name);
}
