package com.schedule.assistant.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.schedule.assistant.data.entity.UserSettings;

/**
 * 用户设置DAO接口
 * 提供用户设置的数据库操作方法
 */
@Dao
public interface UserSettingsDao {
    @Query("SELECT * FROM user_settings LIMIT 1")
    UserSettings getUserSettings();

    @Insert
    void insert(UserSettings settings);

    @Update
    void update(UserSettings settings);

    @Query("UPDATE user_settings SET themeMode = :themeMode WHERE id = :id")
    void updateThemeMode(int id, int themeMode);

    @Query("UPDATE user_settings SET languageMode = :languageMode WHERE id = :id")
    void updateLanguageMode(int id, int languageMode);

    @Query("UPDATE user_settings SET notificationEnabled = :enabled WHERE id = :id")
    void updateNotificationEnabled(int id, boolean enabled);

    @Query("UPDATE user_settings SET notificationAdvanceTime = :minutes WHERE id = :id")
    void updateNotificationAdvanceTime(int id, int minutes);
}