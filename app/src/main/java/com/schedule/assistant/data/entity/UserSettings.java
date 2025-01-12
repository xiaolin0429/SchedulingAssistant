package com.schedule.assistant.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用户设置实体类
 * 用于存储用户的主题、语言等设置信息
 */
@Entity(tableName = "user_settings")
public class UserSettings {
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 主题设置：0-跟随系统，1-浅色主题，2-深色主题
    private int themeMode;

    // 语言设置：0-跟随系统，1-中文，2-英文
    private int languageMode;

    // 通知设置：是否开启通知
    private boolean notificationEnabled;

    // 通知提醒时间（分钟）
    private int notificationAdvanceTime;

    // 是否同步系统闹钟
    private boolean syncSystemAlarm;

    public UserSettings() {
        // 默认设置
        this.themeMode = 0;
        this.languageMode = 0;
        this.notificationEnabled = true;
        this.notificationAdvanceTime = 30;
        this.syncSystemAlarm = false; // 默认开启不系统闹钟同步
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }

    public int getLanguageMode() {
        return languageMode;
    }

    public void setLanguageMode(int languageMode) {
        this.languageMode = languageMode;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public int getNotificationAdvanceTime() {
        return notificationAdvanceTime;
    }

    public void setNotificationAdvanceTime(int notificationAdvanceTime) {
        this.notificationAdvanceTime = notificationAdvanceTime;
    }

    public boolean isSyncSystemAlarm() {
        return syncSystemAlarm;
    }

    public void setSyncSystemAlarm(boolean syncSystemAlarm) {
        this.syncSystemAlarm = syncSystemAlarm;
    }
}