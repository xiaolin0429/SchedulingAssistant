package com.schedule.assistant;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class SchedulingAssistantApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
} 