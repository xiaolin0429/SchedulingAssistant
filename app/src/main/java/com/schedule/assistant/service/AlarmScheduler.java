package com.schedule.assistant.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.AlarmClock;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.app.AlarmManagerCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.data.entity.UserSettings;
import com.schedule.assistant.receiver.AlarmReceiver;
import java.util.Calendar;
import java.util.ArrayList;

/**
 * 闹钟调度器
 * 负责设置和取消系统闹钟
 */
public class AlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;
    private static final int REQUEST_SET_ALARM_PERMISSION = 2;
    private static final String TAG = "AlarmScheduler";

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 检查是否有设置精确闹钟的权限
     */
    public boolean hasExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * 检查是否有设置系统闹钟的权限
     */
    public boolean hasSetAlarmPermission() {
        return ActivityCompat.checkSelfPermission(context,
                "com.android.alarm.permission.SET_ALARM") == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 调度闹钟
     * 只设置应用内闹钟，不涉及系统闹钟
     */
    public void scheduleAlarm(AlarmEntity alarm) {
        if (!hasExactAlarmPermission()) {
            return;
        }

        // 只设置应用内闹钟
        scheduleAppAlarm(alarm);
    }

    /**
     * 处理闹钟状态变更
     * 
     * @param alarm   闹钟实体
     * @param enabled 是否启用
     */
    public void handleAlarmStateChange(AlarmEntity alarm, boolean enabled) {
        android.util.Log.d(TAG, "handleAlarmStateChange: alarmId=" + alarm.getId()
                + ", enabled=" + enabled
                + ", currentEnabled=" + alarm.isEnabled());

        // 1. 更新应用内闹钟状态
        scheduleAppAlarm(alarm);

        // 2. 根据启用状态和系统闹钟同步设置处理系统闹钟
        if (enabled) {
            // 在后台线程中获取用户设置
            new Thread(() -> {
                try {
                    UserSettings settings = AppDatabase.getDatabase(context).userSettingsDao().getUserSettings();
                    if (settings != null && settings.isSyncSystemAlarm()) {
                        // 只有在启用系统闹钟同步时才创建系统闹钟
                        boolean hasExactPermission = hasExactAlarmPermission();
                        boolean hasSetPermission = hasSetAlarmPermission();

                        android.util.Log.d(TAG, "系统闹钟权限状态: hasExactPermission=" + hasExactPermission
                                + ", hasSetPermission=" + hasSetPermission);

                        if (hasExactPermission && hasSetPermission) {
                            android.util.Log.d(TAG, "开始设置系统闹钟");
                            scheduleSystemAlarm(alarm);
                        } else {
                            android.util.Log.w(TAG, "缺少必要权限，无法设置系统闹钟");
                            showDegradedModeToastOnMainThread();
                        }
                    } else {
                        android.util.Log.d(TAG, "系统闹钟同步未启用，跳过系统闹钟设置");
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "获取用户设置失败", e);
                }
            }).start();
        } else {
            // 在后台线程中获取用户设置
            new Thread(() -> {
                try {
                    UserSettings settings = AppDatabase.getDatabase(context).userSettingsDao().getUserSettings();
                    if (settings != null && settings.isSyncSystemAlarm()) {
                        android.util.Log.d(TAG, "取消系统闹钟");
                        cancelSystemAlarm(alarm);
                    } else {
                        android.util.Log.d(TAG, "系统闹钟同步未启用，跳过取消系统闹钟");
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "获取用户设置失败", e);
                }
            }).start();
        }
    }

    /**
     * 在主线程上显示降级模式提示
     */
    private void showDegradedModeToastOnMainThread() {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            showDegradedModeToast();
        });
    }

    /**
     * 设置应用内闹钟
     * 不涉及系统闹钟操作
     */
    private void scheduleAppAlarm(AlarmEntity alarm) {
        android.util.Log.d(TAG, "scheduleAppAlarm: alarmId=" + alarm.getId()
                + ", enabled=" + alarm.isEnabled()
                + ", time=" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()).format(new java.util.Date(alarm.getTimeInMillis())));

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGERED);
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.getId());
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_NAME, alarm.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarm.isEnabled()) {
            android.util.Log.d(TAG, "设置应用内闹钟");
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    alarm.getTimeInMillis(),
                    pendingIntent);
        } else {
            android.util.Log.d(TAG, "取消应用内闹钟");
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * 设置系统闹钟
     * 只在闹钟启用时调用
     */
    private void scheduleSystemAlarm(AlarmEntity alarm) {
        android.util.Log.d(TAG, "scheduleSystemAlarm: alarmId=" + alarm.getId()
                + ", enabled=" + alarm.isEnabled()
                + ", time=" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()).format(new java.util.Date(alarm.getTimeInMillis())));

        if (!alarm.isEnabled()) {
            android.util.Log.w(TAG, "闹钟未启用，不设置系统闹钟");
            return;
        }

        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(alarm.getTimeInMillis());

            // 使用系统闹钟应用设置闹钟
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
            intent.putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE));
            // 添加应用标识符前缀，用于后续取消时匹配
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "app_alarm_" + alarm.getId());
            intent.putExtra(AlarmClock.EXTRA_VIBRATE, alarm.isVibrate());
            if (alarm.isRepeat()) {
                intent.putExtra(AlarmClock.EXTRA_DAYS, getDaysOfWeek(alarm.getRepeatDays()));
            }
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
            android.util.Log.d(TAG, "系统闹钟设置成功");
        } catch (Exception e) {
            android.util.Log.e(TAG, "设置系统闹钟失败", e);
            showDegradedModeToast();
        }
    }

    /**
     * 取消系统闹钟
     */
    private void cancelSystemAlarm(AlarmEntity alarm) {
        android.util.Log.d(TAG, "cancelSystemAlarm: alarmId=" + alarm.getId());

        try {
            // 使用系统闹钟应用取消闹钟
            Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
            intent.putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "app_alarm_" + alarm.getId());
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            context.startActivity(intent);
            android.util.Log.d(TAG, "系统闹钟静默取消成功");
        } catch (Exception e) {
            android.util.Log.e(TAG, "取消系统闹钟失败", e);
        }
    }

    /**
     * 将重复日期位图转换为系统闹钟的星期数组
     */
    private ArrayList<Integer> getDaysOfWeek(int repeatDays) {
        ArrayList<Integer> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if ((repeatDays & (1 << i)) != 0) {
                // 系统闹钟的星期从周一开始为1，到周日为7
                days.add(i == 0 ? 7 : i); // 将周日(0)转换为7
            }
        }
        return days;
    }

    /**
     * 显示取消提示
     */
    private void showCancelToast() {
        android.widget.Toast.makeText(
                context,
                context.getString(R.string.alarm_disabled),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * 取消闹钟
     * 同时取消应用内闹钟和系统闹钟
     */
    public void cancelAlarm(AlarmEntity alarm) {
        // 取消应用内闹钟
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        // 在后台线程中获取用户设置
        new Thread(() -> {
            try {
                UserSettings settings = AppDatabase.getDatabase(context).userSettingsDao().getUserSettings();
                // 只有在启用了系统闹钟同步时才取消系统闹钟
                if (settings != null && settings.isSyncSystemAlarm() && alarm.isEnabled() && hasSetAlarmPermission()) {
                    cancelSystemAlarm(alarm);
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "获取用户设置失败", e);
            }
        }).start();
    }

    /**
     * 显示降级模式提示
     */
    private void showDegradedModeToast() {
        android.widget.Toast.makeText(
                context,
                context.getString(R.string.alarm_degraded_mode),
                android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * 请求精确闹钟和系统闹钟权限
     */
    public void requestPermissions(FragmentActivity activity, Runnable onGranted) {
        // 检查并请求SET_ALARM权限
        if (!hasSetAlarmPermission()) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.system_alarm_permission_title)
                    .setMessage(R.string.system_alarm_permission_message)
                    .setPositiveButton(R.string.grant_permission, (dialog, which) -> ActivityCompat.requestPermissions(
                            activity,
                            new String[] { "com.android.alarm.permission.SET_ALARM" },
                            REQUEST_SET_ALARM_PERMISSION))
                    .setNegativeButton(R.string.use_app_alarm_only, (dialog, which) -> {
                        showDegradedModeToast();
                        checkExactAlarmPermission(activity, onGranted);
                    })
                    .show();
            return;
        }

        checkExactAlarmPermission(activity, onGranted);
    }

    /**
     * 检查精确闹钟权限
     */
    private void checkExactAlarmPermission(FragmentActivity activity, Runnable onGranted) {
        // 检查并请求SCHEDULE_EXACT_ALARM权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(R.string.exact_alarm_permission_title)
                        .setMessage(R.string.exact_alarm_permission_message)
                        .setPositiveButton(R.string.go_to_settings, (dialog, which) -> activity.startActivity(
                                new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return;
            }
        }

        onGranted.run();
    }
}