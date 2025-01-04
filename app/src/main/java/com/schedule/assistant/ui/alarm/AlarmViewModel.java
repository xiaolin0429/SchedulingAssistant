package com.schedule.assistant.ui.alarm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.repository.AlarmRepository;
import java.util.List;
import java.util.Calendar;

public class AlarmViewModel extends AndroidViewModel {
    private final AlarmRepository repository;
    private final LiveData<List<AlarmEntity>> allAlarms;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        repository = new AlarmRepository(application);
        allAlarms = repository.getAllAlarms();
    }

    public LiveData<List<AlarmEntity>> getAlarms() {
        return allAlarms;
    }

    public void addAlarm(int hoursBefore, int minutesBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hoursBefore);
        calendar.add(Calendar.MINUTE, minutesBefore);

        AlarmEntity alarm = new AlarmEntity();
        alarm.setTime(calendar.getTimeInMillis());
        alarm.setEnabled(true);
        alarm.setName("闹钟 " + hoursBefore + ":" + String.format("%02d", minutesBefore));
        
        repository.insert(alarm);
    }

    public void toggleAlarm(AlarmEntity alarm) {
        alarm.setEnabled(!alarm.isEnabled());
        repository.update(alarm);
    }

    public void deleteAlarm(AlarmEntity alarm) {
        repository.delete(alarm);
    }
} 