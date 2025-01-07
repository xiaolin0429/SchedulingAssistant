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
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.receiver.AlarmReceiver;
import java.util.Calendar;

/**
 * 闹钟调度器
 * 负责设置和取消系统闹钟
 */
public class AlarmScheduler {
    private final Context context;
    private final AlarmManager alarmManager;
    private static final int REQUEST_SET_ALARM_PERMISSION = 2;

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
     * 同时设置应用内闹钟和系统闹钟
     * 如果没有系统闹钟权限，则降级为仅应用内闹钟
     */
    public void scheduleAlarm(AlarmEntity alarm) {
        boolean hasAllPermissions = hasExactAlarmPermission() && hasSetAlarmPermission();
        boolean hasBasicPermissions = hasExactAlarmPermission();

        if (!hasBasicPermissions) {
            // 如果连基本的精确闹钟权限都没有，直接返回
            return;
        }

        // 设置应用内闹钟
        scheduleAppAlarm(alarm);
        
        if (alarm.isEnabled()) {
            if (hasAllPermissions) {
                // 有完整权限，设置系统闹钟
                scheduleSystemAlarm(alarm);
            } else {
                // 降级提示
                showDegradedModeToast();
            }
        } else if (hasAllPermissions) {
            // 只有在有完整权限时才取消系统闹钟
            cancelSystemAlarm(alarm);
        }
    }

    /**
     * 设置应用内闹钟
     */
    private void scheduleAppAlarm(AlarmEntity alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_ALARM_TRIGGER);
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.getId());
        intent.putExtra(AlarmReceiver.EXTRA_ALARM_NAME, alarm.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarm.isEnabled()) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                alarm.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * 设置系统闹钟
     */
    private void scheduleSystemAlarm(AlarmEntity alarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getTimeInMillis());
        
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
            .putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            .putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            .putExtra(AlarmClock.EXTRA_MESSAGE, alarm.getName())
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 取消系统闹钟
     */
    private void cancelSystemAlarm(AlarmEntity alarm) {
        if (!hasSetAlarmPermission()) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarm.getTimeInMillis());
        
        // 静默取消系统闹钟，不显示系统界面
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM)
            .putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_TIME)
            .putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
            .putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
            .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // 先尝试使用ACTION_DISMISS_ALARM
        try {
            context.startActivity(intent);
            showCancelToast();
        } catch (Exception e) {
            // 如果ACTION_DISMISS_ALARM失败，尝试使用ACTION_SET_ALARM将闹钟设置为禁用状态
            try {
                Intent setIntent = new Intent(AlarmClock.ACTION_SET_ALARM)
                    .putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY))
                    .putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE))
                    .putExtra(AlarmClock.EXTRA_MESSAGE, alarm.getName())
                    .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(setIntent);
                showCancelToast();
            } catch (Exception e2) {
                // 如果两种方式都失败，说明系统不支持静默操作闹钟
            }
        }
    }

    /**
     * 显示取消提示
     */
    private void showCancelToast() {
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.alarm_disabled),
            android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * 取消闹钟
     */
    public void cancelAlarm(AlarmEntity alarm) {
        // 取消应用内闹钟
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        
        // 取消系统闹钟
        cancelSystemAlarm(alarm);
    }

    /**
     * 显示降级模式提示
     */
    private void showDegradedModeToast() {
        android.widget.Toast.makeText(
            context,
            context.getString(R.string.alarm_degraded_mode),
            android.widget.Toast.LENGTH_SHORT
        ).show();
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
                    new String[]{"com.android.alarm.permission.SET_ALARM"},
                    REQUEST_SET_ALARM_PERMISSION
                ))
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
                        new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    ))
                    .setNegativeButton(R.string.alarm_cancel, null)
                    .show();
                return;
            }
        }

        onGranted.run();
    }
} 