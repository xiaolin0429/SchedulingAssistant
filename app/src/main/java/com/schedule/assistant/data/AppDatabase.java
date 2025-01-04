package com.schedule.assistant.data;

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

@Database(entities = {Shift.class, Alarm.class}, version = 2, exportSchema = false)
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

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "schedule-db")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 