package com.schedule.assistant.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shift_templates")
public class ShiftTemplate {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String name; // 班次名称
    private String startTime; // 开始时间
    private String endTime; // 结束时间
    private int color; // 班次颜色
    private boolean isDefault; // 是否为默认班次
    private long updateTime; // 更新时间
    private ShiftType type; // 班次类型

    public ShiftTemplate(@NonNull String name, String startTime, String endTime, int color) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.isDefault = false;
        this.updateTime = System.currentTimeMillis();
        // 根据名称设置默认的班次类型
        this.type = switch (name) {
            case "早班" -> ShiftType.DAY_SHIFT;
            case "晚班" -> ShiftType.NIGHT_SHIFT;
            case "休息" -> ShiftType.REST_DAY;
            default -> ShiftType.CUSTOM;
        };
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public ShiftType getType() {
        return type;
    }

    public void setType(ShiftType type) {
        this.type = type;
    }
}