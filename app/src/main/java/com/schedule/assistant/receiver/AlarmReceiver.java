package com.schedule.assistant.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.schedule.assistant.R;
import com.schedule.assistant.MainActivity;

/**
 * 闹钟广播接收器
 * 处理闹钟触发事件
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
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
        if (action != null) {
            if (action.equals(ACTION_ALARM_TRIGGERED) || action.equals(ACTION_ALARM_TRIGGER)) {
                long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1);
                String alarmName = intent.getStringExtra(EXTRA_ALARM_NAME);
                showAlarmNotification(context, alarmId, alarmName);
            }
        }
    }

    private void showAlarmNotification(Context context, long alarmId, String alarmName) {
        // 确保通知渠道已创建
        createNotificationChannel(context);

        // 创建打开应用的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_ALARM_ID, alarmId);

        // 创建PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(alarmName)
                .setContentText("闹钟时间到")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[] { 0, 1000, 500, 1000 })
                .setOngoing(true);

        // 显示通知
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "显示闹钟通知失败：缺少通知权限", e);
        }
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