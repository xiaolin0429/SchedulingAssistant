package com.schedule.assistant.data.repository;

import com.schedule.assistant.data.entity.Shift;

public interface OnShiftLoadedCallback {
    void onShiftLoaded(Shift shift);
    void onDataNotAvailable();
} 