package com.schedule.assistant.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Objects;

@Entity(tableName = "shifts")
public class Shift {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String date;

    @NonNull
    private ShiftType type;

    private long shiftTypeId;

    private String startTime;
    private String endTime;
    private String note;
    private long updateTime;

    @Ignore
    private boolean isNewlyAdded;

    @Ignore
    public Shift(@NonNull String date, @NonNull ShiftType type) {
        this(date, type, null, null);
    }

    public Shift(@NonNull String date, @NonNull ShiftType type, String startTime, String endTime) {
        this.date = date;
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.updateTime = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public ShiftType getType() {
        return type;
    }

    public void setType(@NonNull ShiftType type) {
        this.type = type;
    }

    public long getShiftTypeId() {
        return shiftTypeId;
    }

    public void setShiftTypeId(long shiftTypeId) {
        this.shiftTypeId = shiftTypeId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isNewlyAdded() {
        return isNewlyAdded;
    }

    public void setNewlyAdded(boolean newlyAdded) {
        isNewlyAdded = newlyAdded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return id == shift.id &&
                updateTime == shift.updateTime &&
                shiftTypeId == shift.shiftTypeId &&
                date.equals(shift.date) &&
                type == shift.type &&
                Objects.equals(startTime, shift.startTime) &&
                Objects.equals(endTime, shift.endTime) &&
                Objects.equals(note, shift.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, type, shiftTypeId, startTime, endTime, note, updateTime);
    }
} 