package com.schedule.assistant.data.converter;

import androidx.room.TypeConverter;
import com.schedule.assistant.data.entity.ShiftType;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static ShiftType toShiftType(String value) {
        return value == null ? null : ShiftType.valueOf(value);
    }

    @TypeConverter
    public static String fromShiftType(ShiftType type) {
        return type == null ? null : type.name();
    }
} 