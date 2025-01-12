package com.schedule.assistant.data;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * 数据库构建器工厂类
 * 用于分离开发模式和生产模式的数据库构建策略
 */
public class DatabaseBuilderFactory {
    private static final String DATABASE_NAME = "schedule_assistant.db";

    /**
     * 获取开发模式的数据库构建器
     * 使用fallbackToDestructiveMigration，不执行迁移策略
     */
    public static RoomDatabase.Builder<AppDatabase> getDevelopmentDatabaseBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        // 使用应用程序上下文以避免内存泄漏
        Context applicationContext = context.getApplicationContext();
        if (applicationContext == null) {
            // 如果无法获取应用程序上下文，则使用原始上下文
            applicationContext = context;
        }
        Log.d("DatabaseBuilderFactory", "Creating database builder with context: " + applicationContext);
        return Room.databaseBuilder(applicationContext, AppDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration();
    }

    /**
     * 获取生产模式的数据库构建器
     * 使用迁移策略，保证数据安全
     */
    public static RoomDatabase.Builder<AppDatabase> getProductionDatabaseBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        // 使用应用程序上下文以避免内存泄漏
        Context applicationContext = context.getApplicationContext();
        return Room.databaseBuilder(applicationContext, AppDatabase.class, DATABASE_NAME)
                .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_9_100,
                        MIGRATION_100_101);
    }

    // 数据库迁移策略定义
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE shifts ADD COLUMN updateTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE shift_templates ADD COLUMN updateTime INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
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
            database.execSQL("ALTER TABLE shifts ADD COLUMN shiftTypeId INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS shift_types_temp (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "startTime TEXT, " +
                            "endTime TEXT, " +
                            "color INTEGER NOT NULL, " +
                            "isDefault INTEGER NOT NULL, " +
                            "updateTime INTEGER NOT NULL)");

            database.execSQL(
                    "INSERT INTO shift_types_temp (id, name, startTime, endTime, color, isDefault, updateTime) " +
                            "SELECT id, name, startTime, endTime, color, isDefault, updateTime FROM shift_types");

            database.execSQL("DROP TABLE shift_types");
            database.execSQL("ALTER TABLE shift_types_temp RENAME TO shift_types");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
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

    private static final Migration MIGRATION_100_101 = new Migration(100, 101) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS user_settings (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "themeMode INTEGER NOT NULL DEFAULT 0, " +
                            "languageMode INTEGER NOT NULL DEFAULT 0, " +
                            "notificationEnabled INTEGER NOT NULL DEFAULT 1, " +
                            "notificationAdvanceTime INTEGER NOT NULL DEFAULT 30)");
        }
    };

}