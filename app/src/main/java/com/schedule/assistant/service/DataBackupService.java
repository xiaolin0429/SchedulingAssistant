package com.schedule.assistant.service;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.schedule.assistant.SchedulingAssistantApp;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.entity.UserSettings;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.model.BackupData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 数据备份服务
 * 负责应用数据的备份和恢复
 */
public class DataBackupService {
    private static final String TAG = "DataBackupService";
    public static final String BACKUP_FOLDER = "ScheduleAssistant/backup";
    private static final String BACKUP_FILE_PREFIX = "backup_";
    private static final String BACKUP_FILE_EXTENSION = ".json";

    private final Context context;
    private final AppDatabase database;
    private final Gson gson;

    public DataBackupService(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(context);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    private File getBackupDirectory() {
        // 首先尝试使用外部公共目录
        File publicDir = new File(Environment.getExternalStorageDirectory(), BACKUP_FOLDER);
        if (!publicDir.exists() && !publicDir.mkdirs()) {
            // 如果无法创建公共目录，回退到应用私有目录
            return new File(context.getExternalFilesDir(null), BACKUP_FOLDER);
        }
        return publicDir;
    }

    /**
     * 执行数据备份
     * 
     * @return 备份文件的路径，如果备份失败则返回null
     */
    public String backupData() {
        try {
            // 创建备份数据对象
            BackupData backupData = new BackupData();

            // 获取用户配置信息
            UserProfile userProfile = database.userProfileDao().getUserProfile();
            backupData.setUserProfile(userProfile);

            UserSettings userSettings = database.userSettingsDao().getUserSettings();
            backupData.setUserSettings(userSettings);

            // 获取闹钟数据
            List<AlarmEntity> alarms = database.alarmDao().getAllAlarmsSync();
            backupData.setAlarms(alarms);

            // 获取班次数据
            List<Shift> shifts = database.shiftDao().getAllShiftsSync();
            backupData.setShifts(shifts);

            // 获取班次模板数据
            List<ShiftTemplate> templates = database.shiftTemplateDao().getAllTemplatesSync();
            backupData.setShiftTemplates(templates);

            // 获取班次类型数据
            List<ShiftTypeEntity> types = database.shiftTypeDao().getAllTypesSync();
            backupData.setShiftTypes(types);

            // 创建备份文件
            String backupFilePath = createBackupFile(backupData);
            Log.i(TAG, "Data backup successful: " + backupFilePath);
            return backupFilePath;
        } catch (Exception e) {
            Log.e(TAG, "Failed to backup data", e);
            return null;
        }
    }

    /**
     * 从备份文件恢复数据
     * 
     * @param backupFile 备份文件
     * @return 是否恢复成功
     */
    public boolean restoreData(File backupFile) {
        try {
            // 读取备份文件
            BackupData backupData;
            try (FileReader reader = new FileReader(backupFile)) {
                backupData = gson.fromJson(reader, BackupData.class);
            }

            if (backupData == null) {
                Log.e(TAG, "Invalid backup file: backup data is null");
                return false;
            }

            // 打印备份数据的基本信息
            Log.i(TAG, "Backup file details:");
            Log.i(TAG, "- File path: " + backupFile.getPath());
            Log.i(TAG, "- File size: " + backupFile.length() + " bytes");
            Log.i(TAG, "- User profile present: " + (backupData.getUserProfile() != null));
            Log.i(TAG, "- User settings present: " + (backupData.getUserSettings() != null));
            Log.i(TAG, "- Number of alarms: " + (backupData.getAlarms() != null ? backupData.getAlarms().size() : 0));
            Log.i(TAG, "- Number of shifts: " + (backupData.getShifts() != null ? backupData.getShifts().size() : 0));
            Log.i(TAG, "- Number of shift templates: "
                    + (backupData.getShiftTemplates() != null ? backupData.getShiftTemplates().size() : 0));
            Log.i(TAG, "- Number of shift types: "
                    + (backupData.getShiftTypes() != null ? backupData.getShiftTypes().size() : 0));

            // 开始数据库事务
            database.runInTransaction(() -> {
                try {
                    Log.d(TAG, "Starting database transaction for data restore");

                    // 清除现有数据前记录当前数据状态
                    int currentAlarms = database.alarmDao().getAllAlarmsSync().size();
                    int currentShifts = database.shiftDao().getAllShiftsSync().size();
                    int currentTemplates = database.shiftTemplateDao().getAllTemplatesSync().size();
                    int currentTypes = database.shiftTypeDao().getAllTypesSync().size();
                    UserProfile currentProfile = database.userProfileDao().getUserProfile();
                    UserSettings currentSettings = database.userSettingsDao().getUserSettings();

                    Log.d(TAG, "Current database state before clearing:");
                    Log.d(TAG, "- Number of alarms: " + currentAlarms);
                    Log.d(TAG, "- Number of shifts: " + currentShifts);
                    Log.d(TAG, "- Number of shift templates: " + currentTemplates);
                    Log.d(TAG, "- Number of shift types: " + currentTypes);
                    Log.d(TAG, "- User profile exists: " + (currentProfile != null));
                    Log.d(TAG, "- User settings exists: " + (currentSettings != null));

                    // 清除现有数据
                    Log.d(TAG, "Clearing existing data...");
                    database.clearAllTables();
                    Log.d(TAG, "Existing data cleared successfully");

                    // 恢复用户配置
                    UserProfile userProfile = backupData.getUserProfile();
                    if (userProfile != null) {
                        Log.d(TAG, "Restoring user profile:");
                        Log.d(TAG, "- Profile ID: " + userProfile.getId());
                        Log.d(TAG, "- Name: " + userProfile.getName());
                        try {
                            database.userProfileDao().insert(userProfile);
                            Log.d(TAG, "User profile restored successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore user profile: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No user profile data found in backup");
                    }

                    // 恢复用户设置
                    UserSettings userSettings = backupData.getUserSettings();
                    if (userSettings != null) {
                        Log.d(TAG, "Restoring user settings:");
                        Log.d(TAG, "- Settings ID: " + userSettings.getId());
                        Log.d(TAG, "- Theme Mode: " + userSettings.getThemeMode());
                        Log.d(TAG, "- Language Mode: " + userSettings.getLanguageMode());
                        try {
                            database.userSettingsDao().insert(userSettings);
                            Log.d(TAG, "User settings restored successfully");
                            // 应用恢复的设置
                            ((SchedulingAssistantApp) context.getApplicationContext()).updateSettings(userSettings);
                            Log.d(TAG, "User settings applied successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore user settings: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No user settings data found in backup");
                    }

                    // 恢复闹钟数据
                    List<AlarmEntity> alarms = backupData.getAlarms();
                    if (alarms != null && !alarms.isEmpty()) {
                        Log.d(TAG, "Restoring " + alarms.size() + " alarms:");
                        try {
                            for (AlarmEntity alarm : alarms) {
                                Log.d(TAG, "- Restoring alarm: ID=" + alarm.getId() +
                                        ", Time=" + alarm.getTimeInMillis() +
                                        ", Enabled=" + alarm.isEnabled());
                                database.alarmDao().insert(alarm);
                            }
                            Log.d(TAG, "Alarms restored successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore alarms: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No alarm data found in backup");
                    }

                    // 恢复班次类型数据
                    List<ShiftTypeEntity> types = backupData.getShiftTypes();
                    if (types != null && !types.isEmpty()) {
                        Log.d(TAG, "Restoring " + types.size() + " shift types:");
                        try {
                            for (ShiftTypeEntity type : types) {
                                Log.d(TAG, "- Restoring shift type: ID=" + type.getId() +
                                        ", Name=" + type.getName());
                                database.shiftTypeDao().insert(type);
                            }
                            Log.d(TAG, "Shift types restored successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore shift types: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No shift type data found in backup");
                    }

                    // 恢复班次模板数据
                    List<ShiftTemplate> templates = backupData.getShiftTemplates();
                    if (templates != null && !templates.isEmpty()) {
                        Log.d(TAG, "Restoring " + templates.size() + " shift templates:");
                        try {
                            for (ShiftTemplate template : templates) {
                                Log.d(TAG, "- Restoring shift template: ID=" + template.getId() +
                                        ", Name=" + template.getName());
                                database.shiftTemplateDao().insert(template);
                            }
                            Log.d(TAG, "Shift templates restored successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore shift templates: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No shift template data found in backup");
                    }

                    // 恢复班次数据
                    List<Shift> shifts = backupData.getShifts();
                    if (shifts != null && !shifts.isEmpty()) {
                        Log.d(TAG, "Restoring " + shifts.size() + " shifts:");
                        try {
                            for (Shift shift : shifts) {
                                Log.d(TAG, "- Restoring shift: ID=" + shift.getId() +
                                        ", Date=" + shift.getDate() +
                                        ", Type=" + shift.getType());
                                database.shiftDao().insert(shift);
                            }
                            Log.d(TAG, "Shifts restored successfully");
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to restore shifts: " + e.getMessage(), e);
                            throw e;
                        }
                    } else {
                        Log.w(TAG, "No shift data found in backup");
                    }

                    // 验证恢复的数据
                    verifyRestoredData(backupData);

                } catch (Exception e) {
                    Log.e(TAG, "Error during data restore transaction: " + e.getMessage(), e);
                    throw e;
                }
            });

            // 验证最终的数据库状态
            Log.i(TAG, "Final database state after restore:");
            Log.i(TAG, "- Number of alarms: " + database.alarmDao().getAllAlarmsSync().size());
            Log.i(TAG, "- Number of shifts: " + database.shiftDao().getAllShiftsSync().size());
            Log.i(TAG, "- Number of shift templates: " + database.shiftTemplateDao().getAllTemplatesSync().size());
            Log.i(TAG, "- Number of shift types: " + database.shiftTypeDao().getAllTypesSync().size());
            Log.i(TAG, "- User profile exists: " + (database.userProfileDao().getUserProfile() != null));
            Log.i(TAG, "- User settings exists: " + (database.userSettingsDao().getUserSettings() != null));

            Log.i(TAG, "Data restore completed successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore data: " + e.getMessage(), e);
            return false;
        }
    }

    private void verifyRestoredData(BackupData backupData) {
        Log.d(TAG, "Starting data verification...");

        // 验证用户配置
        if (backupData.getUserProfile() != null) {
            UserProfile restoredProfile = database.userProfileDao().getUserProfile();
            if (restoredProfile == null) {
                String error = "Failed to verify restored user profile: profile not found in database";
                Log.e(TAG, error);
                throw new RuntimeException(error);
            }
            Log.d(TAG, "User profile verification successful");
        }

        // 验证用户设置
        if (backupData.getUserSettings() != null) {
            UserSettings restoredSettings = database.userSettingsDao().getUserSettings();
            if (restoredSettings == null) {
                String error = "Failed to verify restored user settings: settings not found in database";
                Log.e(TAG, error);
                throw new RuntimeException(error);
            }
            Log.d(TAG, "User settings verification successful");
        }

        // 验证闹钟数据
        List<AlarmEntity> alarms = backupData.getAlarms();
        if (alarms != null && !alarms.isEmpty()) {
            List<AlarmEntity> restoredAlarms = database.alarmDao().getAllAlarmsSync();
            verifyListSize("alarms", alarms.size(), restoredAlarms.size());
            Log.d(TAG, "Alarms verification successful");
        }

        // 验证班次类型数据
        List<ShiftTypeEntity> types = backupData.getShiftTypes();
        if (types != null && !types.isEmpty()) {
            List<ShiftTypeEntity> restoredTypes = database.shiftTypeDao().getAllTypesSync();
            verifyListSize("shift types", types.size(), restoredTypes.size());
            Log.d(TAG, "Shift types verification successful");
        }

        // 验证班次模板数据
        List<ShiftTemplate> templates = backupData.getShiftTemplates();
        if (templates != null && !templates.isEmpty()) {
            List<ShiftTemplate> restoredTemplates = database.shiftTemplateDao().getAllTemplatesSync();
            verifyListSize("shift templates", templates.size(), restoredTemplates.size());
            Log.d(TAG, "Shift templates verification successful");
        }

        // 验证班次数据
        List<Shift> shifts = backupData.getShifts();
        if (shifts != null && !shifts.isEmpty()) {
            List<Shift> restoredShifts = database.shiftDao().getAllShiftsSync();
            verifyListSize("shifts", shifts.size(), restoredShifts.size());
            Log.d(TAG, "Shifts verification successful");
        }

        Log.d(TAG, "All data verification completed successfully");
    }

    private void verifyListSize(String dataType, int expected, int actual) {
        if (actual != expected) {
            String error = String.format("Failed to verify restored %s: count mismatch - expected %d, got %d",
                    dataType, expected, actual);
            Log.e(TAG, error);
            throw new RuntimeException(error);
        }
    }

    /**
     * 创建备份文件
     * 
     * @param backupData 要备份的数据
     * @return 备份文件的路径
     * @throws IOException 如果创建文件失败
     */
    private String createBackupFile(BackupData backupData) throws IOException {
        // 获取备份目录
        File backupDir = getBackupDirectory();
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            throw new IOException("Failed to create backup directory");
        }

        // 生成备份文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = BACKUP_FILE_PREFIX + timestamp + BACKUP_FILE_EXTENSION;
        File backupFile = new File(backupDir, fileName);

        // 写入数据
        try (FileWriter writer = new FileWriter(backupFile)) {
            gson.toJson(backupData, writer);
        }

        return backupFile.getAbsolutePath();
    }

    /**
     * 清除所有备份文件
     * 
     * @return 是否成功清除所有备份文件
     */
    public boolean clearAllBackups() {
        boolean success = true;

        // 清除公共目录的备份文件
        File publicBackupDir = new File(Environment.getExternalStorageDirectory(), BACKUP_FOLDER);
        if (publicBackupDir.exists()) {
            File[] publicFiles = publicBackupDir.listFiles((dir, name) -> name.endsWith(BACKUP_FILE_EXTENSION));
            if (publicFiles != null) {
                for (File file : publicFiles) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete backup file: " + file.getPath());
                        success = false;
                    }
                }
            }
        }

        // 清除私有目录的备份文件
        File privateBackupDir = new File(context.getExternalFilesDir(null), BACKUP_FOLDER);
        if (privateBackupDir.exists()) {
            File[] privateFiles = privateBackupDir.listFiles((dir, name) -> name.endsWith(BACKUP_FILE_EXTENSION));
            if (privateFiles != null) {
                for (File file : privateFiles) {
                    if (!file.delete()) {
                        Log.e(TAG, "Failed to delete backup file: " + file.getPath());
                        success = false;
                    }
                }
            }
        }

        return success;
    }
}