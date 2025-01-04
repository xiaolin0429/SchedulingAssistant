package com.schedule.assistant.data.entity;

import com.schedule.assistant.R;

public enum ShiftType {
    DAY_SHIFT(R.string.day_shift, R.color.day_shift),
    NIGHT_SHIFT(R.string.night_shift, R.color.night_shift),
    REST_DAY(R.string.rest_day, R.color.rest_day),
    NO_SHIFT(R.string.no_shift, R.color.no_shift);

    private final int nameResId;
    private final int colorResId;

    ShiftType(int nameResId, int colorResId) {
        this.nameResId = nameResId;
        this.colorResId = colorResId;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getColorResId() {
        return colorResId;
    }
} 