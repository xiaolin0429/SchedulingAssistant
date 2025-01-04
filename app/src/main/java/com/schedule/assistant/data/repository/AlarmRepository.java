package com.schedule.assistant.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.entity.AlarmEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {
    private final AlarmDao alarmDao;
    private final ExecutorService executorService;

    public AlarmRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        alarmDao = db.alarmDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return alarmDao.getAllAlarms();
    }

    public LiveData<AlarmEntity> getAlarmById(long id) {
        return alarmDao.getAlarmById(id);
    }

    public void insert(AlarmEntity alarm) {
        executorService.execute(() -> alarmDao.insert(alarm));
    }

    public void update(AlarmEntity alarm) {
        executorService.execute(() -> alarmDao.update(alarm));
    }

    public void delete(AlarmEntity alarm) {
        executorService.execute(() -> alarmDao.delete(alarm));
    }
} 