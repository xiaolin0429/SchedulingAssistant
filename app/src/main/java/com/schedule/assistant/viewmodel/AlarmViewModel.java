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
import androidx.lifecycle.MutableLiveData;

/**
 * 闹钟ViewModel
 * 处理闹钟相关的业务逻辑
 */
public class AlarmViewModel extends AndroidViewModel {
    private final AlarmRepository repository;
    private final LiveData<List<AlarmEntity>> allAlarms;
    private final AlarmScheduler alarmScheduler;
    private static final String TAG = "AlarmViewModel";
    private final MutableLiveData<Boolean> alarmUpdated = new MutableLiveData<>();

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
    public void createAlarm(
            int hour,
            int minute,
            String name,
            boolean repeat,
            int repeatDays,
            String soundUri,
            boolean vibrate,
            boolean enabled,
            int snoozeInterval,
            int maxSnoozeCount
    ) {

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
        alarm.setEnabled(enabled);
        alarm.setRepeat(repeat);
        alarm.setRepeatDays(repeatDays);
        alarm.setSoundUri(soundUri);
        alarm.setVibrate(vibrate);
        alarm.setSnoozeInterval(snoozeInterval);
        alarm.setMaxSnoozeCount(maxSnoozeCount);

        // 插入闹钟数据
        long alarmId = repository.insert(alarm);
        if (alarmId > 0) {
            alarm.setId(alarmId);
            // 只有在启用状态时才创建系统闹钟
            if (enabled) {
                alarmScheduler.handleAlarmStateChange(alarm, true);
            }
        }
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
        android.util.Log.d(TAG, "toggleAlarm: id=" + id + ", enabled=" + enabled);

        // 1. 更新数据库中的启用状态
        repository.updateEnabled(id, enabled);

        // 2. 更新内存中的闹钟状态
        List<AlarmEntity> alarms = allAlarms.getValue();
        if (alarms != null) {
            AlarmEntity alarm = alarms.stream()
                    .filter(a -> a.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (alarm != null) {
                android.util.Log.d(TAG, "找到闹钟: " + alarm.getId()
                        + ", 当前状态=" + alarm.isEnabled()
                        + ", 目标状态=" + enabled);

                alarm.setEnabled(enabled);
                // 3. 如果是启用操作，需要请求权限
                if (enabled) {
                    android.util.Log.d(TAG, "请求权限并启用系统闹钟");
                    alarmScheduler.requestPermissions(activity, () -> {
                        android.util.Log.d(TAG, "权限请求完成，开始设置系统闹钟");
                        alarmScheduler.handleAlarmStateChange(alarm, true);
                    });
                } else {
                    android.util.Log.d(TAG, "直接禁用系统闹钟");
                    alarmScheduler.handleAlarmStateChange(alarm, false);
                }
            } else {
                android.util.Log.w(TAG, "未找到闹钟: " + id);
            }
        } else {
            android.util.Log.w(TAG, "闹钟列表为空");
        }
    }

    /**
     * 禁用所有闹钟
     */
    public void disableAllAlarms() {
        // 1. 更新数据库中的启用状态
        repository.disableAllAlarms();

        // 2. 更新内存中的闹钟状态并取消系统闹钟
        List<AlarmEntity> alarms = allAlarms.getValue();
        if (alarms != null) {
            for (AlarmEntity alarm : alarms) {
                alarm.setEnabled(false);
                alarmScheduler.handleAlarmStateChange(alarm, false);
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

    /**
     * 更新闹钟
     * 同时更新应用内闹钟和系统闹钟
     */
    public void updateAlarm(FragmentActivity activity, AlarmEntity alarm) {
        alarmScheduler.requestPermissions(activity, () -> {
            alarm.setUpdateTime(System.currentTimeMillis());
            repository.update(alarm);
            if (alarm.isEnabled()) {
                android.util.Log.w(TAG, "已启用的闹钟不允许编辑: " + alarm.getId());
                return;
            }
            alarmScheduler.scheduleAlarm(alarm); // 重新调度系统闹钟
            alarmUpdated.postValue(true); // 通知UI数据已更新
        });
    }

    public LiveData<Boolean> getAlarmUpdated() {
        return alarmUpdated;
    }

    public void resetAlarmUpdated() {
        alarmUpdated.setValue(false);
    }
}