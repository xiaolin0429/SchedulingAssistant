package com.schedule.assistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.schedule.assistant.data.entity.Shift;

import java.util.List;

@Dao
public interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE date = :date")
    LiveData<Shift> getShiftByDate(String date);

    @Query("SELECT * FROM shifts WHERE date = :date")
    Shift getShiftByDateDirect(String date);

    @Query("SELECT * FROM shifts WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    LiveData<List<Shift>> getShiftsBetween(String startDate, String endDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Shift shift);

    @Update
    void update(Shift shift);

    @Delete
    void delete(Shift shift);

    @Query("UPDATE shifts SET note = :note, update_time = :updateTime WHERE date = :date")
    void updateNote(String date, String note, long updateTime);

    @Query("SELECT * FROM shifts WHERE note IS NOT NULL ORDER BY update_time DESC LIMIT :limit")
    List<Shift> getShiftsWithNotes(int limit);
} 