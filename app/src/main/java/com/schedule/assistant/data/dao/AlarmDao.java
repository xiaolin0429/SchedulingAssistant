package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.schedule.assistant.data.entity.AlarmEntity;
import java.util.List;

@Dao
public interface AlarmDao {
    @Insert
    long insert(AlarmEntity alarm);

    @Update
    void update(AlarmEntity alarm);

    @Delete
    void delete(AlarmEntity alarm);

    @Query("SELECT * FROM alarms ORDER BY time ASC")
    LiveData<List<AlarmEntity>> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE id = :id")
    LiveData<AlarmEntity> getAlarmById(long id);
} 