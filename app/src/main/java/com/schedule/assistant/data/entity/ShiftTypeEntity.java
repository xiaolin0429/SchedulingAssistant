package com.schedule.assistant.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.TypeConverters;
import com.google.gson.annotations.SerializedName;
import com.schedule.assistant.data.converter.ShiftTypeConverter;

@Entity(tableName = "shift_types")
@TypeConverters(ShiftTypeConverter.class)
public class ShiftTypeEntity {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("a")
    private long id;

    @NonNull
    @SerializedName("b")
    private String name; // 班次名称
    @SerializedName("c")
    private String startTime; // 默认开始时间
    @SerializedName("d")
    private String endTime; // 默认结束时间
    @SerializedName("e")
    private int color; // 班次颜色
    @SerializedName("f")
    private boolean isDefault; // 是否为默认班次
    @SerializedName("g")
    private long updateTime; // 更新时间
    @SerializedName("h")
    private ShiftType type; // 班次类型

    public ShiftTypeEntity(@NonNull String name, String startTime, String endTime, int color) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.isDefault = false;
        this.updateTime = System.currentTimeMillis();
        this.type = ShiftType.CUSTOM; // 默认为自定义类型
    }

    @Ignore
    public ShiftTypeEntity(@NonNull String name, String startTime, String endTime, int color, ShiftType type) {
        this(name, startTime, endTime, color);
        this.type = type;
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