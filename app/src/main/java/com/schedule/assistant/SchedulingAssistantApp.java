package com.schedule.assistant;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import com.jakewharton.threetenabp.AndroidThreeTen;
import android.Manifest;
import android.content.pm.PackageManager;
import java.io.File;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.UserSettings;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.os.Environment;
import android.content.Intent;
import android.os.Build;
import com.schedule.assistant.service.DataBackupService;

public class SchedulingAssistantApp extends Application {
    private static final String TAG = "SchedulingAssistantApp";
    private static Locale currentLocale;
    private static UserSettings cachedSettings;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isInitialized = false;
    private android.app.Activity currentActivity;
    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_LAST_BACKUP_CHECK = "last_backup_check";
    private static final long BACKUP_CHECK_INTERVAL = 24 * 60 * 60 * 1000; // 24小时

    @Override
    protected void attachBaseContext(@NonNull Context base) {
        Context wrappedContext = base;
        try {
            // 使用系统默认语言作为初始设置
            currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);

            // 更新Context的Locale
            wrappedContext = updateBaseContextLocale(base);

            // 异步加载设置
            executor.execute(() -> {
                try {
                    UserSettings settings = AppDatabase.getDatabase(base).userSettingsDao().getUserSettings();
                    if (settings != null) {
                        Log.d(TAG, "Settings loaded - Theme: " + settings.getThemeMode() + ", Language: "
                                + settings.getLanguageMode());
                        cachedSettings = settings;
                        // 在主线程中应用设置
                        runOnUiThread(() -> {
                            try {
                                setLocale(settings.getLanguageMode());
                                applyThemeSettings(settings.getThemeMode());
                            } catch (Exception e) {
                                Log.e(TAG, "Error applying settings on UI thread", e);
                            }
                        });
                    } else {
                        Log.d(TAG, "No settings found, will create default settings in onCreate");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading settings", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in attachBaseContext", e);
        }
        super.attachBaseContext(wrappedContext);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        // 在后台线程中初始化设置
        if (cachedSettings == null) {
            executor.execute(() -> {
                try {
                    if (!isInitialized) {
                        initializeSettings();
                        isInitialized = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing settings", e);
                }
            });
        } else {
            isInitialized = true;
        }

        // 在后台线程中检查备份文件
        executor.execute(this::checkForBackupFiles);

        // 注册Activity生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                currentActivity = activity;
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });
    }

    private void initializeSettings() {
        try {
            UserSettings settings = AppDatabase.getDatabase(this).userSettingsDao().getUserSettings();
            if (settings != null) {
                Log.d(TAG, "Loading settings - Theme: " + settings.getThemeMode() + ", Language: "
                        + settings.getLanguageMode());
                cachedSettings = settings;
                // 先设置语言，确保在应用主题之前应用语言设置
                setLocale(settings.getLanguageMode());
                // 在主线程中应用主题设置
                final int themeMode = settings.getThemeMode();
                runOnUiThread(() -> {
                    applyThemeSettings(themeMode);
                    // 重新创建Activity以确保语言设置生效
                    if (getCurrentActivity() != null) {
                        getCurrentActivity().recreate();
                    }
                });
            } else {
                Log.d(TAG, "Creating default settings");
                final UserSettings newSettings = new UserSettings();
                AppDatabase.getDatabase(this).userSettingsDao().insert(newSettings);
                cachedSettings = newSettings;
                setLocale(newSettings.getLanguageMode());
                final int themeMode = newSettings.getThemeMode();
                runOnUiThread(() -> applyThemeSettings(themeMode));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initializeSettings", e);
            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
        }
    }

    private android.app.Activity getCurrentActivity() {
        return currentActivity;
    }

    private void setLocale(int languageMode) {
        try {
            String languageCode = switch (languageMode) {
                case 1 -> "zh";
                case 2 -> "en";
                default ->
                    // 使用系统默认语言
                    Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
            };

            currentLocale = new Locale(languageCode);
            if (currentActivity != null) {
                Resources resources = currentActivity.getResources();
                Configuration configuration = resources.getConfiguration();
                configuration.setLocales(new LocaleList(currentLocale));
                currentActivity.createConfigurationContext(configuration);
                // 使用新的context更新资源
                currentActivity.getBaseContext().getResources().getConfiguration()
                        .setLocales(new LocaleList(currentLocale));
            }
            Log.d(TAG, "Locale set to: " + languageCode);
        } catch (Exception e) {
            Log.e(TAG, "Error setting locale", e);
        }
    }

    private void applyThemeSettings(int themeMode) {
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentLocale != null && cachedSettings != null) {
            // 确保在配置改变时保持用户设置
            setLocale(cachedSettings.getLanguageMode());
            Configuration configuration = updateBaseContextLocale(this).getResources().getConfiguration();
            // 通知所有Activity更新配置
            if (getCurrentActivity() != null) {
                getCurrentActivity().applyOverrideConfiguration(configuration);
            }
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * 获取当前缓存的用户设置
     * 此方法用于其他组件获取用户设置信息
     * 
     * @return 当前缓存的用户设置
     */
    @SuppressWarnings("unused")
    public static UserSettings getCachedSettings() {
        return cachedSettings;
    }

    /**
     * 更新用户设置
     * 此方法用于更新主题和语言等设置
     * 会同时更新数据库和内存中的缓存
     *
     * @param settings 新的用户设置
     */
    public void updateSettings(UserSettings settings) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Updating settings - Theme: " + settings.getThemeMode() + ", Language: "
                        + settings.getLanguageMode());
                // 更新数据库
                AppDatabase.getDatabase(this).userSettingsDao().update(settings);
                // 更新缓存
                cachedSettings = settings;
                // 先设置语言
                setLocale(settings.getLanguageMode());
                // 在主线程中应用主题和重新创建Activity
                runOnUiThread(() -> {
                    try {
                        applyThemeSettings(settings.getThemeMode());
                        Activity currentActivity = getCurrentActivity();
                        if (currentActivity != null) {
                            // 使用新的Configuration重新创建Activity
                            Configuration newConfig = new Configuration(
                                    currentActivity.getResources().getConfiguration());
                            newConfig.setLocales(new LocaleList(currentLocale));
                            currentActivity.createConfigurationContext(newConfig);
                            // 重新创建Activity以应用新的配置
                            currentActivity.recreate();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error applying settings in UI thread", e);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating settings", e);
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        if (currentActivity != null) {
            currentActivity.runOnUiThread(runnable);
        } else {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(runnable);
        }
    }

    private void checkForBackupFiles() {
        // 检查是否需要检查备份文件
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        long lastCheck = prefs.getLong(KEY_LAST_BACKUP_CHECK, 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCheck < BACKUP_CHECK_INTERVAL) {
            return;
        }

        // 更新最后检查时间
        prefs.edit().putLong(KEY_LAST_BACKUP_CHECK, currentTime).apply();

        // 在后台线程中检查备份文件
        new Thread(() -> {
            try {
                // 检查是否有存储权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        return;
                    }
                } else if (checkSelfPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                File latestBackup = null;
                long latestTime = 0;

                // 检查公共目录
                File publicBackupDir = new File(Environment.getExternalStorageDirectory(),
                        DataBackupService.BACKUP_FOLDER);
                if (publicBackupDir.exists()) {
                    File[] publicFiles = publicBackupDir.listFiles((dir, name) -> name.endsWith(".json"));
                    if (publicFiles != null) {
                        for (File file : publicFiles) {
                            if (file.lastModified() > latestTime) {
                                latestTime = file.lastModified();
                                latestBackup = file;
                            }
                        }
                    }
                }

                // 检查私有目录
                File privateBackupDir = new File(getExternalFilesDir(null), DataBackupService.BACKUP_FOLDER);
                if (privateBackupDir.exists()) {
                    File[] privateFiles = privateBackupDir.listFiles((dir, name) -> name.endsWith(".json"));
                    if (privateFiles != null) {
                        for (File file : privateFiles) {
                            if (file.lastModified() > latestTime) {
                                latestTime = file.lastModified();
                                latestBackup = file;
                            }
                        }
                    }
                }

                if (latestBackup != null) {
                    // 发送广播通知MainActivity显示恢复对话框
                    Intent intent = new Intent("com.schedule.assistant.ACTION_SHOW_RESTORE_DIALOG");
                    intent.putExtra("backup_file", latestBackup.getAbsolutePath());
                    sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking backup files", e);
            }
        }).start();
    }

    private Context updateBaseContextLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (currentLocale != null) {
            configuration.setLocales(new LocaleList(currentLocale));
            return context.createConfigurationContext(configuration);
        }
        return context;
    }
}