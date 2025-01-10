package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.UserSettingsDao;
import com.schedule.assistant.data.entity.UserSettings;

/**
 * 设置页面ViewModel
 * 用于管理设置数据和UI状态
 */
public class SettingsViewModel extends AndroidViewModel {
    private final UserSettingsDao userSettingsDao;
    private final MutableLiveData<UserSettings> userSettings;
    private final MutableLiveData<Boolean> isLoading;

    public SettingsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userSettingsDao = db.userSettingsDao();
        userSettings = new MutableLiveData<>();
        isLoading = new MutableLiveData<>(false);
        loadSettings();
    }

    public LiveData<UserSettings> getUserSettings() {
        return userSettings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    private void loadSettings() {
        isLoading.setValue(true);
        new Thread(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings == null) {
                settings = new UserSettings();
                userSettingsDao.insert(settings);
            }
            userSettings.postValue(settings);
            isLoading.postValue(false);
        }).start();
    }

    public void updateNotificationSettings(boolean enabled) {
        new Thread(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateNotificationEnabled(settings.getId(), enabled);
                settings.setNotificationEnabled(enabled);
                userSettings.postValue(settings);
            }
        }).start();
    }

    public void updateNotificationTime(int minutes) {
        new Thread(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateNotificationAdvanceTime(settings.getId(), minutes);
                settings.setNotificationAdvanceTime(minutes);
                userSettings.postValue(settings);
            }
        }).start();
    }

    public void updateThemeMode(int themeMode) {
        new Thread(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateThemeMode(settings.getId(), themeMode);
                settings.setThemeMode(themeMode);
                userSettings.postValue(settings);
            }
        }).start();
    }

    public void updateLanguageMode(int languageMode) {
        new Thread(() -> {
            UserSettings settings = userSettingsDao.getUserSettings();
            if (settings != null) {
                userSettingsDao.updateLanguageMode(settings.getId(), languageMode);
                settings.setLanguageMode(languageMode);
                userSettings.postValue(settings);
            }
        }).start();
    }
}