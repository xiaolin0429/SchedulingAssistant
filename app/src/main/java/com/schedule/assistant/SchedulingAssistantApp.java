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
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.UserSettings;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Field;

public class SchedulingAssistantApp extends Application {
    private static final String TAG = "SchedulingAssistantApp";
    private static Locale currentLocale;
    private static UserSettings cachedSettings;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isInitialized = false;

    @Override
    protected void attachBaseContext(@NonNull Context base) {
        Context wrappedContext = base;
        try {
            // 在后台线程中加载设置，但使用CountDownLatch等待结果
            CountDownLatch latch = new CountDownLatch(1);
            final UserSettings[] settings = new UserSettings[1];

            executor.execute(() -> {
                try {
                    settings[0] = AppDatabase.getDatabase(base).userSettingsDao().getUserSettings();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading settings in background", e);
                } finally {
                    latch.countDown();
                }
            });

            // 等待最多1秒钟
            if (latch.await(1, TimeUnit.SECONDS)) {
                if (settings[0] != null) {
                    Log.d(TAG, "Initial settings loaded - Theme: " + settings[0].getThemeMode() + ", Language: "
                            + settings[0].getLanguageMode());
                    cachedSettings = settings[0];
                    setLocale(settings[0].getLanguageMode());
                    // 确保主题设置也在这里应用
                    final int themeMode = settings[0].getThemeMode();
                    AppCompatDelegate.setDefaultNightMode(themeMode == 1 ? AppCompatDelegate.MODE_NIGHT_NO
                            : themeMode == 2 ? AppCompatDelegate.MODE_NIGHT_YES
                                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                }
            } else {
                Log.w(TAG, "Timeout waiting for settings, using system locale");
                currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            }

            // 更新Context的Locale
            if (currentLocale != null) {
                wrappedContext = updateBaseContextLocale(base);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in attachBaseContext", e);
            currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        }
        super.attachBaseContext(wrappedContext);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);

        // 只在没有缓存的设置时初始化
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
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Object activities = activitiesField.get(activityThread);
            if (activities instanceof java.util.Map) {
                for (Object activityRecord : ((java.util.Map) activities).values()) {
                    Field activityField = activityRecord.getClass().getDeclaredField("activity");
                    activityField.setAccessible(true);
                    android.app.Activity activity = (android.app.Activity) activityField.get(activityRecord);
                    if (activity != null) {
                        return activity;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current activity", e);
        }
        return null;
    }

    private void setLocale(int languageMode) {
        switch (languageMode) {
            case 1:
                currentLocale = new Locale("zh");
                break;
            case 2:
                currentLocale = new Locale("en");
                break;
            default:
                currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
                break;
        }
        Log.d(TAG, "Setting locale to: " + currentLocale.getLanguage());
        LocaleList.setDefault(new LocaleList(currentLocale));
    }

    private void applyThemeSettings(int themeMode) {
        Log.d(TAG, "Applying theme mode: " + themeMode);
        switch (themeMode) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private Context updateBaseContextLocale(@NonNull Context context) {
        if (currentLocale == null) {
            currentLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
        }
        Log.d(TAG, "Updating base context with locale: " + currentLocale.getLanguage());

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocales(new LocaleList(currentLocale));

        return context.createConfigurationContext(configuration);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentLocale != null && cachedSettings != null) {
            // 确保在配置改变时保持用户设置
            setLocale(cachedSettings.getLanguageMode());
            Context context = updateBaseContextLocale(this);
            Configuration configuration = context.getResources().getConfiguration();
            getApplicationContext().createConfigurationContext(configuration);
            // 通知所有Activity更新配置
            if (getCurrentActivity() != null) {
                getCurrentActivity().applyOverrideConfiguration(configuration);
            }
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    public static UserSettings getCachedSettings() {
        return cachedSettings;
    }

    public void updateSettings(UserSettings settings) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "Updating settings - Theme: " + settings.getThemeMode() + ", Language: "
                        + settings.getLanguageMode());
                // 更新数据库
                AppDatabase.getDatabase(this).userSettingsDao().update(settings);
                // 更新缓存
                cachedSettings = settings;
                // 设置语言
                setLocale(settings.getLanguageMode());
                // 应用主题和重新创建Activity
                runOnUiThread(() -> {
                    applyThemeSettings(settings.getThemeMode());
                    if (getCurrentActivity() != null) {
                        getCurrentActivity().recreate();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating settings", e);
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        android.os.Handler handler = new android.os.Handler(getMainLooper());
        handler.post(runnable);
    }
}