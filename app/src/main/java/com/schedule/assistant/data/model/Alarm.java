package com.schedule.assistant.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class Alarm {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private int hour;
    private int minute;
    private boolean enabled;
    
    public Alarm(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.enabled = true;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) {
        this.hour = hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }
} 