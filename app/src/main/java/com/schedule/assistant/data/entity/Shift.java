package com.schedule.assistant.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.schedule.assistant.data.entity.ShiftType;

@Entity(tableName = "shifts")
public class Shift {
    @PrimaryKey
    @NonNull
    private String date;
    
    @ColumnInfo(name = "shift_type")
    private ShiftType shiftType;
    
    private String note;

    @ColumnInfo(name = "update_time")
    private long updateTime;

    public Shift(@NonNull String date) {
        this.date = date;
        this.shiftType = ShiftType.NO_SHIFT;
        this.updateTime = System.currentTimeMillis();
    }

    @Ignore
    public Shift(@NonNull String date, ShiftType shiftType) {
        this(date);
        this.shiftType = shiftType;
    }

    @Ignore
    public Shift(@NonNull String date, ShiftType shiftType, String note) {
        this(date, shiftType);
        this.note = note;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        this.updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
} 