package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.lotlimys.yeseatwhat.data.db.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY sort_order ASC")
    List<Category> getAll();

    @Query("SELECT * FROM category WHERE type = :type ORDER BY sort_order ASC")
    List<Category> getByType(String type);

    @Insert
    void insert(Category... categories);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);
}
