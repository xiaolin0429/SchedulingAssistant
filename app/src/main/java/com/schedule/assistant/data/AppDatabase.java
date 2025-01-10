package com.schedule.assistant.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.dao.ShiftTemplateDao;
import com.schedule.assistant.data.dao.ShiftTypeDao;
import com.schedule.assistant.data.dao.UserProfileDao;
import com.schedule.assistant.data.dao.UserSettingsDao;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.entity.UserSettings;
import com.schedule.assistant.data.converter.DateConverter;
import com.schedule.assistant.data.converter.ShiftTypeConverter;

@Database(entities = { Shift.class, ShiftTemplate.class, ShiftTypeEntity.class, AlarmEntity.class,
        UserProfile.class, UserSettings.class }, version = 101, exportSchema = false)
@TypeConverters({ DateConverter.class, ShiftTypeConverter.class })
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ShiftDao shiftDao();

    public abstract ShiftTemplateDao shiftTemplateDao();

    public abstract ShiftTypeDao shiftTypeDao();

    public abstract AlarmDao alarmDao();

    public abstract UserProfileDao userProfileDao();

    public abstract UserSettingsDao userSettingsDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // 开发模式：使用fallbackToDestructiveMigration
                    INSTANCE = DatabaseBuilderFactory.getDevelopmentDatabaseBuilder(context).build();

                    /*
                     * 生产模式：使用迁移策略
                     * INSTANCE =
                     * DatabaseBuilderFactory.getProductionDatabaseBuilder(context).build();
                     */
                }
            }
        }
        return INSTANCE;
    }
}