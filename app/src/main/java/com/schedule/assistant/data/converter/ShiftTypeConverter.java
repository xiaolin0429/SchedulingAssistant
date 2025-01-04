package com.schedule.assistant.data.converter;

import androidx.room.TypeConverter;

import com.schedule.assistant.data.entity.ShiftType;

public class ShiftTypeConverter {
    @TypeConverter
    public static ShiftType toShiftType(String value) {
        return value == null ? null : ShiftType.valueOf(value);
    }

    @TypeConverter
    public static String fromShiftType(ShiftType type) {
        return type == null ? null : type.name();
    }
} 