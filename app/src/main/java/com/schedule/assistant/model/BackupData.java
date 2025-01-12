package com.schedule.assistant.model;

import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.entity.UserSettings;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;

import java.util.List;

/**
 * 备份数据模型
 * 包含需要备份的所有数据
 */
public class BackupData {
    private UserProfile userProfile;
    private UserSettings userSettings;
    private List<AlarmEntity> alarms;
    private List<Shift> shifts;
    private List<ShiftTemplate> shiftTemplates;
    private List<ShiftTypeEntity> shiftTypes;
    private long backupTime;
    private String appVersion;

    public BackupData() {
        this.backupTime = System.currentTimeMillis();
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    public List<AlarmEntity> getAlarms() {
        return alarms;
    }

    public void setAlarms(List<AlarmEntity> alarms) {
        this.alarms = alarms;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    public List<ShiftTemplate> getShiftTemplates() {
        return shiftTemplates;
    }

    public void setShiftTemplates(List<ShiftTemplate> shiftTemplates) {
        this.shiftTemplates = shiftTemplates;
    }

    public List<ShiftTypeEntity> getShiftTypes() {
        return shiftTypes;
    }

    public void setShiftTypes(List<ShiftTypeEntity> shiftTypes) {
        this.shiftTypes = shiftTypes;
    }

    public long getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(long backupTime) {
        this.backupTime = backupTime;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}