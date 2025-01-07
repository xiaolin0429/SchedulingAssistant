package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.repository.AlarmRepository;
import java.util.List;
import java.util.Calendar;
import com.schedule.assistant.service.AlarmScheduler;
import androidx.fragment.app.FragmentActivity;

/**
 * 闹钟ViewModel
 * 处理闹钟相关的业务逻辑
 */
public class AlarmViewModel extends AndroidViewModel {
    private final AlarmRepository repository;
    private final LiveData<List<AlarmEntity>> allAlarms;
    private final AlarmScheduler alarmScheduler;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        repository = new AlarmRepository(application);
        allAlarms = repository.getAllAlarms();
        alarmScheduler = new AlarmScheduler(application);
    }

    /**
     * 获取所有闹钟
     */
    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return allAlarms;
    }

    /**
     * 创建新闹钟
     */
    public void createAlarm(FragmentActivity activity, int hour, int minute, String name, boolean repeat, 
                          int repeatDays, String soundUri, boolean vibrate) {
        alarmScheduler.requestPermissions(activity, () -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 如果设置的时间早于当前时间，设置为第二天
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            AlarmEntity alarm = new AlarmEntity();
            alarm.setTimeInMillis(calendar.getTimeInMillis());
            alarm.setName(name != null ? name : "闹钟");
            alarm.setRepeat(repeat);
            alarm.setRepeatDays(repeatDays);
            alarm.setSoundUri(soundUri != null ? soundUri : "");
            alarm.setVibrate(vibrate);

            long alarmId = repository.insert(alarm);
            alarm.setId(alarmId);
            alarmScheduler.scheduleAlarm(alarm);
        });
    }

    /**
     * 删除闹钟
     */
    public void deleteAlarm(AlarmEntity alarm) {
        repository.delete(alarm);
        alarmScheduler.cancelAlarm(alarm);
    }

    /**
     * 切换闹钟启用状态
     */
    public void toggleAlarm(FragmentActivity activity, long id, boolean enabled) {
        alarmScheduler.requestPermissions(activity, () -> {
            repository.updateEnabled(id, enabled);
            List<AlarmEntity> alarms = allAlarms.getValue();
            if (alarms != null) {
                AlarmEntity alarm = alarms.stream()
                    .filter(a -> a.getId() == id)
                    .findFirst()
                    .orElse(null);
                if (alarm != null) {
                    alarm.setEnabled(enabled);
                    alarmScheduler.scheduleAlarm(alarm);
                }
            }
        });
    }

    /**
     * 禁用所有闹钟
     */
    public void disableAllAlarms() {
        repository.disableAllAlarms();
        List<AlarmEntity> alarms = allAlarms.getValue();
        if (alarms != null) {
            for (AlarmEntity alarm : alarms) {
                alarm.setEnabled(false);
                alarmScheduler.cancelAlarm(alarm);
            }
        }
    }

    /**
     * 检查并更新过期的闹钟时间
     * 对于非重复的闹钟，如果时间已过期，将自动设置为下一个相同时间点
     */
    public void checkAndUpdateExpiredAlarms() {
        List<AlarmEntity> alarms = allAlarms.getValue();
        if (alarms != null) {
            long currentTime = System.currentTimeMillis();
            for (AlarmEntity alarm : alarms) {
                if (alarm.isEnabled() && !alarm.isRepeat() && alarm.getTimeInMillis() < currentTime) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(alarm.getTimeInMillis());
                    // 设置为下一天的相同时间
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    alarm.setTimeInMillis(calendar.getTimeInMillis());
                    repository.update(alarm);
                }
            }
        }
    }

    /**
     * 请求闹钟相关权限
     */
    public void requestAlarmPermissions(FragmentActivity activity, Runnable onGranted) {
        alarmScheduler.requestPermissions(activity, onGranted);
    }
} 