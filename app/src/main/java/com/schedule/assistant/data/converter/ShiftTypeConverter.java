package com.schedule.assistant.data.converter;

import androidx.room.TypeConverter;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.entity.ShiftType;

public class ShiftTypeConverter {
    @TypeConverter
    public static ShiftType toShiftType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ShiftType.NO_SHIFT;
        }
        try {
            return ShiftType.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return ShiftType.NO_SHIFT;
        }
    }

    @TypeConverter
    public static String fromShiftType(ShiftType type) {
        return type == null ? ShiftType.NO_SHIFT.name() : type.name();
    }
} 