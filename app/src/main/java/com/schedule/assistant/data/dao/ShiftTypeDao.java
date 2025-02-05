package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import java.util.List;

@Dao
public interface ShiftTypeDao {
    @Insert
    long insert(ShiftTypeEntity shiftType);

    @Update
    void update(ShiftTypeEntity shiftType);

    @Delete
    void delete(ShiftTypeEntity shiftType);

    @Query("SELECT * FROM shift_types ORDER BY updateTime DESC")
    LiveData<List<ShiftTypeEntity>> getAllShiftTypes();

    @Query("SELECT * FROM shift_types ORDER BY updateTime DESC")
    List<ShiftTypeEntity> getAllTypesSync();

    @Query("SELECT * FROM shift_types WHERE isDefault = 1 ORDER BY updateTime DESC")
    LiveData<List<ShiftTypeEntity>> getDefaultShiftTypes();

    @Query("SELECT COUNT(*) FROM shift_types")
    int getShiftTypeCount();

    @Query("SELECT * FROM shift_types WHERE id = :id")
    LiveData<ShiftTypeEntity> getShiftTypeById(long id);

    @Query("SELECT * FROM shift_types WHERE id = :id")
    ShiftTypeEntity getShiftTypeByIdDirect(long id);
}