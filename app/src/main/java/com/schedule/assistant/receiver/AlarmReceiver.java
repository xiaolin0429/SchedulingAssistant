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
    public static final String ACTION_ALARM_TRIGGER = "com.schedule.assistant.ACTION_ALARM_TRIGGER";
    public static final String EXTRA_ALARM_ID = "ALARM_ID";
    public static final String EXTRA_ALARM_NAME = "ALARM_NAME";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }

        switch (action) {
            case ACTION_ALARM_TRIGGER:
                handleAlarmTrigger(context, intent);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                // TODO: 重新调度所有启用的闹钟
                break;
        }
    }

    private void handleAlarmTrigger(Context context, Intent intent) {
        long alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1);
        String alarmName = intent.getStringExtra(EXTRA_ALARM_NAME);

        if (alarmId == -1) {
            return;
        }

        // 创建通知渠道
        createNotificationChannel(context);

        // 创建打开应用的Intent
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 获取默认铃声
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(alarmName)
            .setContentText("闹钟时间到")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(new long[]{0, 1000, 500, 1000})
            .setContentIntent(pendingIntent);

        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // 显示通知
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("闹钟提醒通知");
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }
} 