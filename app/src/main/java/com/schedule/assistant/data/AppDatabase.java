package com.schedule.assistant.data;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.schedule.assistant.data.converter.ShiftTypeConverter;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Alarm;
import com.schedule.assistant.data.entity.Shift;

@Database(entities = {Shift.class, Alarm.class}, version = 3, exportSchema = false)
@TypeConverters({ShiftTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ShiftDao shiftDao();
    public abstract AlarmDao alarmDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加 update_time 列
            database.execSQL("ALTER TABLE shifts ADD COLUMN update_time INTEGER NOT NULL DEFAULT 0");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1. 创建临时表
            database.execSQL("CREATE TABLE shifts_temp (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "startTime TEXT, " +
                    "endTime TEXT, " +
                    "note TEXT, " +
                    "updateTime INTEGER NOT NULL DEFAULT 0)");

            // 2. 复制数据
            database.execSQL("INSERT INTO shifts_temp (date, type, note, updateTime) " +
                    "SELECT date, shift_type, note, update_time FROM shifts");

            // 3. 删除旧表
            database.execSQL("DROP TABLE shifts");

            // 4. 重命名新表
            database.execSQL("ALTER TABLE shifts_temp RENAME TO shifts");

            // 5. 重建闹钟表
            database.execSQL("DROP TABLE IF EXISTS alarms");
            database.execSQL("CREATE TABLE IF NOT EXISTS alarms (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "hoursBefore INTEGER NOT NULL, " +
                    "minutesBefore INTEGER NOT NULL, " +
                    "enabled INTEGER NOT NULL DEFAULT 1)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "schedule-db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static AppDatabase getDatabase(final Application application) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(application,
                            AppDatabase.class, "schedule_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 