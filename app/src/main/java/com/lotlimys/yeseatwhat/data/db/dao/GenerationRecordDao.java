package com.lotlimys.yeseatwhat.data.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import static androidx.room.OnConflictStrategy.REPLACE;
import androidx.room.Query;
import androidx.room.Update;

import com.lotlimys.yeseatwhat.data.db.entity.GenerationRecord;

import java.util.List;

@Dao
public interface GenerationRecordDao {
    @Insert(onConflict = REPLACE)
    void insert(GenerationRecord record);

    @Update
    void update(GenerationRecord record);

    @Query("SELECT * FROM generation_record ORDER BY created_at DESC")
    List<GenerationRecord> getAllByTimeDesc();

    @Query("SELECT * FROM generation_record WHERE id = :id")
    GenerationRecord getById(long id);

    @Query("UPDATE generation_record SET dish_count = :count, completed_at = :time, success = :success WHERE id = :id")
    void updateResult(long id, int count, long time, boolean success);
}
