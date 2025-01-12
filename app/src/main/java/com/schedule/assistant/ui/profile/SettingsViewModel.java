package com.schedule.assistant.ui.profile;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.UserSettingsDao;
import com.schedule.assistant.data.entity.UserSettings;

/**
 * 设置页面的ViewModel
 * 用于管理设置相关的数据和业务逻辑
 */
public class SettingsViewModel extends AndroidViewModel {
    private final UserSettingsDao userSettingsDao;
    private final LiveData<UserSettings> userSettings;

    public SettingsViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getDatabase(application);
        userSettingsDao = database.userSettingsDao();
        userSettings = userSettingsDao.getUserSettingsLiveData();
    }

    /**
     * 获取用户设置的LiveData
     */
    public LiveData<UserSettings> getUserSettings() {
        return userSettings;
    }

    /**
     * 更新通知设置
     */
    public void updateNotificationSettings(boolean enabled) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateNotificationEnabled(settings.getId(), enabled);
            }
        });
    }

    /**
     * 更新通知提前时间
     */
    public void updateNotificationTime(int minutes) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateNotificationAdvanceTime(settings.getId(), minutes);
            }
        });
    }

    /**
     * 更新主题设置
     */
    public void updateThemeMode(int themeMode) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                settings.setThemeMode(themeMode);
                userSettingsDao.update(settings);
            }
        });
    }

    /**
     * 更新语言设置
     */
    public void updateLanguageMode(int languageMode) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateLanguageMode(settings.getId(), languageMode);
            }
        });
    }

    /**
     * 更新系统闹钟同步设置
     */
    public void updateSyncSystemAlarm(boolean enabled) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateSyncSystemAlarm(settings.getId(), enabled);
            }
        });
    }
}