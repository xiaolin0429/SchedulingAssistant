package com.schedule.assistant.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private int hoursBefore;
    private int minutesBefore;
    private boolean enabled;

    public Alarm(int hoursBefore, int minutesBefore) {
        this.hoursBefore = hoursBefore;
        this.minutesBefore = minutesBefore;
        this.enabled = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHoursBefore() {
        return hoursBefore;
    }

    public void setHoursBefore(int hoursBefore) {
        this.hoursBefore = hoursBefore;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    public void setMinutesBefore(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getHour() {
        return hoursBefore;
    }

    public int getMinute() {
        return minutesBefore;
    }

    public String getTimeString() {
        return String.format("%02d:%02d", hoursBefore, minutesBefore);
    }
} 