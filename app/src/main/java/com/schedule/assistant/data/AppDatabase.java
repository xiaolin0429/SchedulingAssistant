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
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.converter.DateConverter;
import com.schedule.assistant.data.converter.ShiftTypeConverter;

@Database(
    entities = {Shift.class, ShiftTemplate.class, ShiftTypeEntity.class, AlarmEntity.class},
    version = 5,
    exportSchema = false
)
@TypeConverters({DateConverter.class, ShiftTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ShiftDao shiftDao();
    public abstract ShiftTemplateDao shiftTemplateDao();
    public abstract ShiftTypeDao shiftTypeDao();
    public abstract AlarmDao alarmDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "schedule_database"
                        )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
                "type TEXT)"
            );
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
                "vibrate INTEGER NOT NULL DEFAULT 1)"
            );
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加shiftTypeId字段到shifts表
            database.execSQL("ALTER TABLE shifts ADD COLUMN shiftTypeId INTEGER NOT NULL DEFAULT 0");
        }
    };
} 