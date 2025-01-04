package com.schedule.assistant.data.entity;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import com.schedule.assistant.R;

public enum ShiftType {
    NO_SHIFT(R.string.no_shift, R.color.no_shift),
    DAY_SHIFT(R.string.day_shift, R.color.day_shift),
    NIGHT_SHIFT(R.string.night_shift, R.color.night_shift),
    REST(R.string.rest_day, R.color.rest_day);

    private final int nameResId;
    private final int colorResId;

    ShiftType(@StringRes int nameResId, @ColorRes int colorResId) {
        this.nameResId = nameResId;
        this.colorResId = colorResId;
    }

    @StringRes
    public int getNameResId() {
        return nameResId;
    }

    @ColorRes
    public int getColorResId() {
        return colorResId;
    }
} 