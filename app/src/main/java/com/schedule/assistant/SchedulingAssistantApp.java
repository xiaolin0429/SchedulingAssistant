package com.schedule.assistant;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.UserSettings;

public class SchedulingAssistantApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        // 预加载主题设置
        initializeTheme();
    }

    private void initializeTheme() {
        // 默认使用跟随系统
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // 在后台线程中加载用户设置
        new Thread(() -> {
            try {
                UserSettings settings = AppDatabase.getDatabase(this).userSettingsDao().getUserSettings();
                if (settings != null) {
                    int themeMode = settings.getThemeMode();
                    if (themeMode == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (themeMode == 2) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}