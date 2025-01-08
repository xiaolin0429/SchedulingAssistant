package com.schedule.assistant.receiver;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.schedule.assistant.R;
import com.schedule.assistant.MainActivity;

/**
 * 闹钟广播接收器
 * 处理闹钟触发事件
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "alarm_channel";
    private static final String CHANNEL_NAME = "闹钟提醒";
    private static final int NOTIFICATION_ID = 1;
    public static final String ACTION_ALARM_TRIGGERED = "com.schedule.assistant.ACTION_ALARM_TRIGGERED";
    public static final String EXTRA_ALARM_ID = "alarm_id";
    public static final String EXTRA_ALARM_NAME = "alarm_name";
    public static final String ACTION_ALARM_TRIGGER = "com.schedule.assistant.ACTION_ALARM_TRIGGER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(ACTION_ALARM_TRIGGERED)) {
            long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1);
            String alarmName = intent.getStringExtra(EXTRA_ALARM_NAME);
            // TODO: 处理闹钟触发事件
            showAlarmNotification(context, alarmId, alarmName);
        }
    }

    private void showAlarmNotification(Context context, long alarmId, String alarmName) {
        // TODO: 显示闹钟通知
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("闹钟提醒通知");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[] { 0, 1000, 500, 1000 });

        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }
}