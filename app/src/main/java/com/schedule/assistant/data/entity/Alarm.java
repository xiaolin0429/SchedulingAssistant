package com.schedule.assistant.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Calendar;

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
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hoursBefore);
        calendar.add(Calendar.MINUTE, minutesBefore);
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }
} 