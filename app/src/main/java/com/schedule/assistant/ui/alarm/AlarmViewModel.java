package com.schedule.assistant.ui.alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.entity.Alarm;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmViewModel extends AndroidViewModel {
    private final AlarmDao alarmDao;
    private final ExecutorService executorService;
    private final LiveData<List<Alarm>> allAlarms;

    public AlarmViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        alarmDao = db.alarmDao();
        executorService = Executors.newSingleThreadExecutor();
        allAlarms = alarmDao.getAllAlarms();
    }

    public LiveData<List<Alarm>> getAlarms() {
        return allAlarms;
    }

    public void addAlarm(int hoursBefore, int minutesBefore) {
        executorService.execute(() -> {
            Alarm alarm = new Alarm(hoursBefore, minutesBefore);
            alarmDao.insert(alarm);
        });
    }

    public void toggleAlarm(Alarm alarm) {
        executorService.execute(() -> {
            alarm.setEnabled(!alarm.isEnabled());
            alarmDao.update(alarm);
        });
    }

    public void deleteAlarm(Alarm alarm) {
        executorService.execute(() -> alarmDao.delete(alarm));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 