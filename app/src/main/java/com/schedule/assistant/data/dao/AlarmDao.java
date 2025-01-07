package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.schedule.assistant.data.entity.AlarmEntity;
import java.util.List;

/**
 * 闹钟数据访问对象
 * 定义了闹钟相关的数据库操作
 */
@Dao
public interface AlarmDao {
    @Insert
    long insert(AlarmEntity alarm);

    @Update
    void update(AlarmEntity alarm);

    @Delete
    void delete(AlarmEntity alarm);

    @Query("SELECT * FROM alarms ORDER BY timeInMillis ASC")
    LiveData<List<AlarmEntity>> getAllAlarms();

    @Query("SELECT * FROM alarms WHERE id = :id")
    LiveData<AlarmEntity> getAlarmById(long id);

    @Query("SELECT * FROM alarms WHERE enabled = 1 ORDER BY timeInMillis ASC")
    LiveData<List<AlarmEntity>> getEnabledAlarms();

    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :id")
    void updateEnabled(long id, boolean enabled);

    @Query("UPDATE alarms SET enabled = 0")
    void disableAllAlarms();

    @Query("DELETE FROM alarms WHERE id = :id")
    void deleteById(long id);
} 