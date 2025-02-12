package com.schedule.assistant.model;

import com.google.gson.annotations.SerializedName;
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
    private static final int CURRENT_FORMAT_VERSION = 1;
    
    @SerializedName("a")
    private int formatVersion;
    
    @SerializedName("g")
    private UserProfile userProfile;
    
    @SerializedName("b")
    private UserSettings userSettings;
    
    @SerializedName("c")
    private List<AlarmEntity> alarms;
    
    @SerializedName("d")
    private List<Shift> shifts;
    
    @SerializedName("e")
    private List<ShiftTemplate> shiftTemplates;
    
    @SerializedName("f")
    private List<ShiftTypeEntity> shiftTypes;
    
    @SerializedName("h")
    private long backupTime;
    
    @SerializedName("i")
    private String appVersion;

    public BackupData() {
        this.backupTime = System.currentTimeMillis();
        this.formatVersion = CURRENT_FORMAT_VERSION;
    }

    public boolean isCompatibleVersion(String currentVersion) {
        // 处理旧版本备份文件（1.2.1及以前的版本没有版本号）
        if (appVersion == null) {
            return true;  // 允许恢复旧版本的备份
        }
        
        // 解析版本号
        String[] backupVer = appVersion.split("\\.");
        String[] currentVer = currentVersion.split("\\.");
        
        if (backupVer.length < 3 || currentVer.length < 3) {
            return true;  // 如果版本号格式不正确，也允许恢复
        }
        
        try {
            int backupMajor = Integer.parseInt(backupVer[0]);
            int backupMinor = Integer.parseInt(backupVer[1]);
            int currentMajor = Integer.parseInt(currentVer[0]);
            int currentMinor = Integer.parseInt(currentVer[1]);
            
            // 主版本号必须相同，次版本号可以更高
            // 1.2.1 的备份可以在 1.2.2 中恢复
            // 1.3.0 的备份不能在 1.2.2 中恢复
            return backupMajor == currentMajor && backupMinor <= currentMinor;
        } catch (NumberFormatException e) {
            return true;  // 如果版本号解析失败，允许恢复
        }
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

    public int getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(int formatVersion) {
        this.formatVersion = formatVersion;
    }
}