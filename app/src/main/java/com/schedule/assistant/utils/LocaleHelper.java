package com.schedule.assistant.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import com.schedule.assistant.SchedulingAssistantApp;
import com.schedule.assistant.data.entity.UserSettings;

import java.util.Locale;

public class LocaleHelper {
    private static final String TAG = "LocaleHelper";

    public static Context onAttach(Context context) {
        String lang = getPersistedLocale(context);
        return setLocale(context, lang);
    }

    public static String getPersistedLocale(Context context) {
        UserSettings settings = SchedulingAssistantApp.getCachedSettings();
        int languageSetting = settings != null ? settings.getLanguageMode() : 0;
        String language;
        switch (languageSetting) {
            case 1:
                language = "zh";
                break;
            case 2:
                language = "en";
                break;
            default:
                language = Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
        }
        Log.d(TAG, "getPersistedLocale: setting=" + languageSetting + ", language=" + language);
        return language;
    }

    public static boolean isFollowingSystemLocale(Context context) {
        UserSettings settings = SchedulingAssistantApp.getCachedSettings();
        return settings == null || settings.getLanguageMode() == 0;
    }

    public static Context setLocale(Context context, String language) {
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(newLocale);
        
        Log.d(TAG, "Setting locale to: " + newLocale.getLanguage() + ", from language: " + language);
        
        return context.createConfigurationContext(configuration);
    }

    public static Locale getCurrentLocale(Context context) {
        String lang = getPersistedLocale(context);
        Log.d(TAG, "Getting current locale, persisted language: " + lang);
        
        Locale locale = new Locale(lang);
        Log.d(TAG, "Returning locale: " + locale.getLanguage());
        return locale;
    }
} 