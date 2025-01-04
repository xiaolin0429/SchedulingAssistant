package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schedule.assistant.data.entity.ShiftTemplate;

import java.util.List;

@Dao
public interface ShiftTemplateDao {
    @Insert
    long insert(ShiftTemplate template);

    @Update
    void update(ShiftTemplate template);

    @Delete
    void delete(ShiftTemplate template);

    @Query("SELECT * FROM shift_templates ORDER BY updateTime DESC")
    LiveData<List<ShiftTemplate>> getAllTemplates();

    @Query("SELECT * FROM shift_templates WHERE isDefault = 1")
    LiveData<List<ShiftTemplate>> getDefaultTemplates();

    @Query("SELECT * FROM shift_templates WHERE id = :id")
    LiveData<ShiftTemplate> getTemplateById(long id);

    @Query("SELECT COUNT(*) FROM shift_templates")
    int getTemplateCount();
} 