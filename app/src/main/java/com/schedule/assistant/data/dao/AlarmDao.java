package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.schedule.assistant.data.entity.Alarm;

import java.util.List;

@Dao
public interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hoursBefore DESC, minutesBefore DESC")
    LiveData<List<Alarm>> getAllAlarms();

    @Insert
    void insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    List<Alarm> getEnabledAlarms();
} 