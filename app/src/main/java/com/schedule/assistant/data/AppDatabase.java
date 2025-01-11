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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 应用数据库类
 * 使用单例模式管理数据库实例
 */
@Database(entities = { Shift.class, ShiftTemplate.class, ShiftTypeEntity.class, AlarmEntity.class,
        UserProfile.class, UserSettings.class }, version = 102, exportSchema = false)
@TypeConverters({ DateConverter.class, ShiftTypeConverter.class })
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // 创建固定大小的线程池用于数据库写入操作
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // 数据访问对象
    public abstract ShiftDao shiftDao();

    public abstract ShiftTemplateDao shiftTemplateDao();

    public abstract ShiftTypeDao shiftTypeDao();

    public abstract AlarmDao alarmDao();

    public abstract UserProfileDao userProfileDao();

    public abstract UserSettingsDao userSettingsDao();

    /**
     * 获取数据库实例
     * 使用双重检查锁定确保线程安全
     */
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

    /**
     * 关闭数据库
     * 在应用退出时调用，确保资源正确释放
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            databaseWriteExecutor.shutdown();
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}