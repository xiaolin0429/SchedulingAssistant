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
    @Query("SELECT * FROM shifts ORDER BY date DESC")
    LiveData<List<Shift>> getAllShifts();

    @Query("SELECT * FROM shifts ORDER BY date DESC")
    List<Shift> getAllShiftsSync();

    @Query("SELECT * FROM shifts WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    LiveData<List<Shift>> getShiftsBetween(String startDate, String endDate);

    @Query("SELECT * FROM shifts WHERE date = :date")
    LiveData<Shift> getShiftByDate(String date);

    @Query("SELECT * FROM shifts WHERE date = :date")
    Shift getShiftByDateDirect(String date);

    @Query("SELECT * FROM shifts WHERE note IS NOT NULL AND note != '' ORDER BY date DESC")
    LiveData<List<Shift>> getShiftsWithNotes();

    @Query("SELECT * FROM shifts WHERE shiftTypeId = :typeId")
    List<Shift> getShiftsByTypeDirect(long typeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Shift shift);

    @Update
    void update(Shift shift);

    @Delete
    void delete(Shift shift);

    @Query("UPDATE shifts SET note = :note WHERE id = :shiftId")
    void updateNote(long shiftId, String note);
}