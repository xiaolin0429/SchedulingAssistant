package com.schedule.assistant.ui.alarm;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.repository.AlarmRepository;
import java.util.List;
import java.util.Calendar;

/**
 * 闹钟ViewModel
 * 处理闹钟相关的业务逻辑
 */
public class AlarmViewModel extends AndroidViewModel {
    private final AlarmRepository repository;
    private final LiveData<List<AlarmEntity>> allAlarms;
    private final LiveData<List<AlarmEntity>> enabledAlarms;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        repository = new AlarmRepository(application);
        allAlarms = repository.getAllAlarms();
        enabledAlarms = repository.getEnabledAlarms();
    }

    /**
     * 获取所有闹钟
     */
    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return allAlarms;
    }

    /**
     * 获取已启用的闹钟
     */
    public LiveData<List<AlarmEntity>> getEnabledAlarms() {
        return enabledAlarms;
    }

    /**
     * 根据ID获取闹钟
     */
    public LiveData<AlarmEntity> getAlarmById(long id) {
        return repository.getAlarmById(id);
    }

    /**
     * 创建新闹钟
     * @param hour 小时（24小时制）
     * @param minute 分钟
     * @param name 闹钟名称
     * @param repeat 是否重复
     * @param repeatDays 重复日期（位图）
     * @param soundUri 铃声URI
     * @param vibrate 是否震动
     */
    public void createAlarm(int hour, int minute, String name, boolean repeat, 
                          int repeatDays, String soundUri, boolean vibrate) {
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
        alarm.setName(name);
        alarm.setRepeat(repeat);
        alarm.setRepeatDays(repeatDays);
        alarm.setSoundUri(soundUri);
        alarm.setVibrate(vibrate);

        repository.insert(alarm);
    }

    /**
     * 更新闹钟
     */
    public void updateAlarm(AlarmEntity alarm) {
        repository.update(alarm);
    }

    /**
     * 删除闹钟
     */
    public void deleteAlarm(AlarmEntity alarm) {
        repository.delete(alarm);
    }

    /**
     * 切换闹钟启用状态
     */
    public void toggleAlarm(long id, boolean enabled) {
        repository.updateEnabled(id, enabled);
    }

    /**
     * 禁用所有闹钟
     */
    public void disableAllAlarms() {
        repository.disableAllAlarms();
    }

    /**
     * 更新闹钟时间
     */
    public void updateAlarmTime(AlarmEntity alarm, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 如果设置的时间早于当前时间，设置为第二天
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarm.setTimeInMillis(calendar.getTimeInMillis());
        repository.update(alarm);
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
} 