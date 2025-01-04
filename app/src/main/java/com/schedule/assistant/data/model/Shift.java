package com.schedule.assistant.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "shifts")
public class Shift {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private Date date;
    private ShiftType type;
    private String note;
    
    public Shift(Date date, ShiftType type) {
        this.date = date;
        this.type = type;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public ShiftType getType() {
        return type;
    }
    
    public void setType(ShiftType type) {
        this.type = type;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
} 