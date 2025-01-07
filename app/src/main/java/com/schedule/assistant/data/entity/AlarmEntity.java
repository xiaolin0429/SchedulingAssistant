package com.schedule.assistant.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 闹钟实体类
 * 用于存储闹钟的基本信息
 */
@Entity(tableName = "alarms")
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;        // 闹钟名称
    private long timeInMillis;  // 闹钟时间（毫秒）
    private boolean enabled;    // 是否启用
    private boolean repeat;     // 是否重复
    private int repeatDays;    // 重复日期（位图：周日=1，周一=2，周二=4，以此类推）
    private String soundUri;    // 铃声URI
    private boolean vibrate;    // 是否震动
    private long createTime;    // 创建时间
    private long updateTime;    // 更新时间

    public AlarmEntity() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.enabled = true;
        this.vibrate = true;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(int repeatDays) {
        this.repeatDays = repeatDays;
    }

    public String getSoundUri() {
        return soundUri;
    }

    public void setSoundUri(String soundUri) {
        this.soundUri = soundUri;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "AlarmEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", timeInMillis=" + timeInMillis +
                ", enabled=" + enabled +
                ", repeat=" + repeat +
                ", repeatDays=" + repeatDays +
                ", vibrate=" + vibrate +
                '}';
    }
} 