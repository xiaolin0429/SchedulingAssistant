package com.schedule.assistant.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.dao.ShiftTemplateDao;
import com.schedule.assistant.data.dao.ShiftTypeDao;
import com.schedule.assistant.data.dao.UserProfileDao;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.converter.DateConverter;
import com.schedule.assistant.data.converter.ShiftTypeConverter;

@Database(entities = { Shift.class, ShiftTemplate.class, ShiftTypeEntity.class, AlarmEntity.class,
        UserProfile.class }, version = 100, exportSchema = false)
@TypeConverters({ DateConverter.class, ShiftTypeConverter.class })
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ShiftDao shiftDao();

    public abstract ShiftTemplateDao shiftTemplateDao();

    public abstract ShiftTypeDao shiftTypeDao();

    public abstract AlarmDao alarmDao();

    public abstract UserProfileDao userProfileDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "schedule_database")
                            // 如果数据库升级失败，允许重建数据库，会清除所有数据
                            // 仅在开发阶段使用，生产环境应该使用Migration策略
                            .fallbackToDestructiveMigration()
                            // 数据库版本迁移策略
                            // 从版本1到版本100的所有迁移路径
                            .addMigrations(
                                    MIGRATION_1_2, // 添加更新时间字段
                                    MIGRATION_2_3, // 创建班次类型表
                                    MIGRATION_3_4, // 创建闹钟表
                                    MIGRATION_4_5, // 添加班次类型ID字段
                                    MIGRATION_5_6, // 更新班次类型表结构
                                    MIGRATION_6_7, // 更新闹钟表结构
                                    MIGRATION_7_8, // 创建用户配置表
                                    MIGRATION_8_9, // 添加邮箱和电话字段
                                    MIGRATION_9_10, // 移除电话字段
                                    MIGRATION_9_100) // 最终版本迁移
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 之前的迁移逻辑
            database.execSQL("ALTER TABLE shifts ADD COLUMN updateTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE shift_templates ADD COLUMN updateTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建新的班次类型表
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS shift_types (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "startTime TEXT, " +
                            "endTime TEXT, " +
                            "color INTEGER NOT NULL, " +
                            "isDefault INTEGER NOT NULL, " +
                            "updateTime INTEGER NOT NULL, " +
                            "type TEXT)");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS alarms (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT, " +
                            "time INTEGER NOT NULL, " +
                            "enabled INTEGER NOT NULL DEFAULT 1, " +
                            "repeat INTEGER NOT NULL DEFAULT 0, " +
                            "repeatDays INTEGER NOT NULL DEFAULT 0, " +
                            "soundUri TEXT, " +
                            "vibrate INTEGER NOT NULL DEFAULT 1)");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加shiftTypeId字段到shifts表
            database.execSQL("ALTER TABLE shifts ADD COLUMN shiftTypeId INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建临时表
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS shift_types_temp (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "startTime TEXT, " +
                            "endTime TEXT, " +
                            "color INTEGER NOT NULL, " +
                            "isDefault INTEGER NOT NULL, " +
                            "updateTime INTEGER NOT NULL)");

            // 复制数据到临时表
            database.execSQL(
                    "INSERT INTO shift_types_temp (id, name, startTime, endTime, color, isDefault, updateTime) " +
                            "SELECT id, name, startTime, endTime, color, isDefault, updateTime FROM shift_types");

            // 删除旧表
            database.execSQL("DROP TABLE shift_types");

            // 重命名临时表为正式表
            database.execSQL("ALTER TABLE shift_types_temp RENAME TO shift_types");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 更新闹钟表结构
            database.execSQL("ALTER TABLE alarms ADD COLUMN createTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE alarms ADD COLUMN updateTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE alarms RENAME COLUMN time TO timeInMillis");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_profile` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `imageUri` TEXT)");
        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE user_profile ADD COLUMN email TEXT");
            database.execSQL("ALTER TABLE user_profile ADD COLUMN phone TEXT");
        }
    };

    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 移除 phone 字段的迁移逻辑
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_profile_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `imageUri` TEXT, `email` TEXT)");
            database.execSQL(
                    "INSERT INTO user_profile_new (id, name, imageUri, email) SELECT id, name, imageUri, email FROM user_profile");
            database.execSQL("DROP TABLE user_profile");
            database.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile");
        }
    };

    private static final Migration MIGRATION_9_100 = new Migration(9, 100) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 迁移逻辑已在之前的迁移中处理，无需额外操作
        }
    };
}