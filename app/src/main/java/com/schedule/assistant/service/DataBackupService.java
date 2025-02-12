package com.schedule.assistant.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

    private boolean isBackupDataValid(BackupData backupData) {
        if (backupData == null) {
            Log.e(TAG, "Backup data is null");
            return true;
        }
        
        try {
            // 检查必要的集合是否存在（可以为空列表，但不能为null）
            if (backupData.getShifts() == null) {
                Log.e(TAG, "Shifts list is null");
                return true;
            }
            if (backupData.getShiftTypes() == null) {
                Log.e(TAG, "ShiftTypes list is null");
                return true;
            }
            
            // 检查是否至少有一些有效数据
            boolean hasData = !backupData.getShifts().isEmpty() || 
                            !backupData.getShiftTypes().isEmpty() ||
                            backupData.getUserSettings() != null;
            
            if (!hasData) {
                Log.w(TAG, "Backup contains no data, but format is valid");
            }
            
            // 只要格式正确就返回true，即使数据为空
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating backup data", e);
            return true;
        }
    }

    private BackupData createTempBackup() {
        BackupData tempBackup = new BackupData();
        
        // 备份当前数据
        tempBackup.setUserProfile(database.userProfileDao().getUserProfile());
        tempBackup.setUserSettings(database.userSettingsDao().getUserSettings());
        tempBackup.setAlarms(database.alarmDao().getAllAlarmsSync());
        tempBackup.setShifts(database.shiftDao().getAllShiftsSync());
        tempBackup.setShiftTemplates(database.shiftTemplateDao().getAllTemplatesSync());
        tempBackup.setShiftTypes(database.shiftTypeDao().getAllTypesSync());
        
        return tempBackup;
    }

    private void restoreFromTempBackup(BackupData tempBackup) {
        try {
            // 清除当前数据
            database.clearAllTables();
            
            // 恢复之前的数据
            if (tempBackup.getUserProfile() != null) {
                database.userProfileDao().insert(tempBackup.getUserProfile());
            }
            if (tempBackup.getUserSettings() != null) {
                database.userSettingsDao().insert(tempBackup.getUserSettings());
            }
            if (tempBackup.getAlarms() != null) {
                for (AlarmEntity alarm : tempBackup.getAlarms()) {
                    database.alarmDao().insert(alarm);
                }
            }
            if (tempBackup.getShiftTypes() != null) {
                for (ShiftTypeEntity type : tempBackup.getShiftTypes()) {
                    database.shiftTypeDao().insert(type);
                }
            }
            if (tempBackup.getShiftTemplates() != null) {
                for (ShiftTemplate template : tempBackup.getShiftTemplates()) {
                    database.shiftTemplateDao().insert(template);
                }
            }
            if (tempBackup.getShifts() != null) {
                for (Shift shift : tempBackup.getShifts()) {
                    database.shiftDao().insert(shift);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore from temp backup", e);
            throw e;
        }
    }

    private String getAppVersion() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to get app version", e);
            return "0.0.0";
        }
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
            
            // 设置版本信息
            backupData.setAppVersion(getAppVersion());

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

            // 验证备份数据
            if (isBackupDataValid(backupData)) {
                Log.e(TAG, "Failed to create valid backup data");
                return null;
            }

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
                // 先打印文件内容以便调试
                String content = new String(java.nio.file.Files.readAllBytes(backupFile.toPath()));
                Log.d(TAG, "Backup file content: " + content);
                
                backupData = gson.fromJson(reader, BackupData.class);
            }

            if (backupData == null) {
                Log.e(TAG, "Invalid backup file: backup data is null");
                return false;
            }

            // 检查版本兼容性
            if (!backupData.isCompatibleVersion(getAppVersion())) {
                Log.e(TAG, "Incompatible backup version: " + backupData.getAppVersion() +
                        " (current: " + getAppVersion() + ")");
                return false;
            }

            // 验证备份数据
            if (isBackupDataValid(backupData)) {
                Log.e(TAG, "Invalid or empty backup data");
                return false;
            }

            // 打印备份数据的基本信息
            Log.i(TAG, "Backup file details:");
            Log.i(TAG, "- File path: " + backupFile.getPath());
            Log.i(TAG, "- File size: " + backupFile.length() + " bytes");
            Log.i(TAG, "- App version: " + backupData.getAppVersion());
            Log.i(TAG, "- Format version: " + backupData.getFormatVersion());
            Log.i(TAG, "- User profile present: " + (backupData.getUserProfile() != null));
            Log.i(TAG, "- User settings present: " + (backupData.getUserSettings() != null));
            Log.i(TAG, "- Number of alarms: " + (backupData.getAlarms() != null ? backupData.getAlarms().size() : 0));
            Log.i(TAG, "- Number of shifts: " + (backupData.getShifts() != null ? backupData.getShifts().size() : 0));
            Log.i(TAG, "- Number of shift templates: " + (backupData.getShiftTemplates() != null ? backupData.getShiftTemplates().size() : 0));
            Log.i(TAG, "- Number of shift types: " + (backupData.getShiftTypes() != null ? backupData.getShiftTypes().size() : 0));

            // 开始数据库事务
            database.runInTransaction(() -> {
                try {
                    Log.d(TAG, "Starting database transaction for data restore");

                    // 在清除现有数据之前先备份
                    BackupData tempBackup = createTempBackup();

                    try {
                        // 清除现有数据
                        database.clearAllTables();

                        // 恢复用户配置
                        if (backupData.getUserProfile() != null) {
                            database.userProfileDao().insert(backupData.getUserProfile());
                        }

                        // 恢复用户设置
                        if (backupData.getUserSettings() != null) {
                            database.userSettingsDao().insert(backupData.getUserSettings());
                            // 应用恢复的设置
                            ((SchedulingAssistantApp) context.getApplicationContext()).updateSettings(backupData.getUserSettings());
                        }

                        // 恢复闹钟数据
                        if (backupData.getAlarms() != null) {
                            for (AlarmEntity alarm : backupData.getAlarms()) {
                                database.alarmDao().insert(alarm);
                            }
                        }

                        // 恢复班次类型数据
                        if (backupData.getShiftTypes() != null) {
                            for (ShiftTypeEntity type : backupData.getShiftTypes()) {
                                database.shiftTypeDao().insert(type);
                            }
                        }

                        // 恢复班次模板数据
                        if (backupData.getShiftTemplates() != null) {
                            for (ShiftTemplate template : backupData.getShiftTemplates()) {
                                database.shiftTemplateDao().insert(template);
                            }
                        }

                        // 恢复班次数据
                        if (backupData.getShifts() != null) {
                            for (Shift shift : backupData.getShifts()) {
                                database.shiftDao().insert(shift);
                            }
                        }

                        // 验证恢复的数据
                        verifyRestoredData(backupData);

                    } catch (Exception e) {
                        // 恢复失败，还原之前的数据
                        Log.e(TAG, "Error during restore, rolling back to previous state", e);
                        restoreFromTempBackup(tempBackup);
                        throw e;
                    }
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
            String error = String.format(Locale.getDefault(), "Failed to verify restored %s: count mismatch - expected %d, got %d",
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