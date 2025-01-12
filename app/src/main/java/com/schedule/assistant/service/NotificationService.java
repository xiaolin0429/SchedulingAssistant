package com.schedule.assistant.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.R;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.UserSettings;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通知服务
 * 用于管理应用的通知功能
 */
public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "schedule_assistant_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int PENDING_INTENT_REQUEST_CODE = 0;
    private static final String MAIN_ACTIVITY_CLASS = "com.schedule.assistant.MainActivity";

    private ExecutorService executor;
    private NotificationManager notificationManager;
    private volatile boolean isRunning = false;
    private final Object lock = new Object();
    private final Set<String> notifiedShiftIds = new HashSet<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startNotificationCheck();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    Log.w(TAG, "Executor did not terminate in the specified time.");
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Error shutting down executor", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_description));
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * 开始检查通知
     */
    private void startNotificationCheck() {
        executor.execute(() -> {
            while (isRunning) {
                try {
                    // 获取用户设置
                    UserSettings settings = AppDatabase.getDatabase(this).userSettingsDao().getUserSettings();
                    if (settings != null && settings.isNotificationEnabled()) {
                        checkAndSendNotifications(settings);
                    }
                    // 使用对象锁等待，避免忙等待
                    synchronized (lock) {
                        lock.wait(60000);
                    }
                } catch (InterruptedException e) {
                    Log.d(TAG, "Notification check interrupted");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error in notification check", e);
                }
            }
        });
    }

    /**
     * 检查并发送通知
     * 
     * @param settings 用户设置
     */
    private void checkAndSendNotifications(UserSettings settings) {
        try {
            Log.d(TAG, "Checking for notifications with advance time: " + settings.getNotificationAdvanceTime());

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            // 计算提前通知的时间范围
            LocalDateTime notifyEnd = now.plusMinutes(settings.getNotificationAdvanceTime());

            // 获取数据库访问对象
            ShiftDao shiftDao = AppDatabase.getDatabase(this).shiftDao();

            // 创建 CountDownLatch 用于等待数据
            CountDownLatch latch = new CountDownLatch(1);
            // 使用AtomicReference来存储结果
            final Object lock = new Object();
            final AtomicReference<List<Shift>> shiftsRef = new AtomicReference<>();

            // 查询时间范围内的班次
            LiveData<List<Shift>> shiftsLiveData = shiftDao.getShiftsBetween(
                    now.format(dateFormatter),
                    notifyEnd.format(dateFormatter));

            // 创建观察者
            androidx.lifecycle.Observer<List<Shift>> observer = shiftList -> {
                synchronized (lock) {
                    shiftsRef.set(shiftList);
                }
                latch.countDown();
            };

            // 在主线程中观察 LiveData
            new android.os.Handler(getMainLooper()).post(() -> shiftsLiveData.observeForever(observer));

            // 等待数据加载完成，最多等待 5 秒
            if (latch.await(5, TimeUnit.SECONDS)) {
                synchronized (lock) {
                    List<Shift> shifts = shiftsRef.get();
                    if (shifts != null) {
                        for (Shift shift : shifts) {
                            // 检查是否在通知时间范围内
                            LocalTime shiftStartTime = LocalTime.parse(shift.getStartTime(), timeFormatter);
                            LocalDate shiftDate = LocalDate.parse(shift.getDate(), dateFormatter);
                            LocalDateTime shiftDateTime = LocalDateTime.of(shiftDate, shiftStartTime);

                            if (shouldNotify(shiftDateTime, now, settings.getNotificationAdvanceTime())) {
                                String shiftId = shift.getDate() + "_" + shift.getStartTime();
                                if (!notifiedShiftIds.contains(shiftId)) {
                                    // 发送班次通知
                                    sendShiftNotification(shift);
                                    // 记录已通知的班次ID
                                    notifiedShiftIds.add(shiftId);
                                    // 清理过期的通知记录
                                    cleanupNotifiedShifts(now);
                                }
                            }
                        }

                        // 在主线程中移除观察者
                        new android.os.Handler(getMainLooper()).post(() -> shiftsLiveData.removeObserver(observer));
                    }
                }
            } else {
                Log.w(TAG, "Timeout waiting for shifts data");
                // 确保在超时情况下也移除观察者
                new android.os.Handler(getMainLooper()).post(() -> shiftsLiveData.removeObserver(observer));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking notifications", e);
        }
    }

    /**
     * 判断是否应该发送通知
     */
    private boolean shouldNotify(LocalDateTime shiftTime, LocalDateTime now, int advanceMinutes) {
        LocalDateTime notifyTime = shiftTime.minusMinutes(advanceMinutes);
        return !now.isBefore(notifyTime) && now.isBefore(shiftTime);
    }

    /**
     * 清理过期的通知记录
     */
    private void cleanupNotifiedShifts(LocalDateTime now) {
        notifiedShiftIds.removeIf(shiftId -> {
            String[] parts = shiftId.split("_");
            if (parts.length != 2)
                return true;
            try {
                LocalDateTime shiftTime = LocalDateTime.parse(parts[0] + "T" + parts[1]);
                return shiftTime.isBefore(now);
            } catch (Exception e) {
                return true;
            }
        });
    }

    /**
     * 发送班次通知
     */
    private void sendShiftNotification(Shift shift) {
        try {
            // 创建打开应用的Intent
            Intent intent = new Intent();
            intent.setClassName(getPackageName(), MAIN_ACTIVITY_CLASS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    PENDING_INTENT_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE);

            // 构建通知内容
            String title = getString(R.string.shift_notification_title);
            String content = getString(R.string.shift_notification_content) +
                    " - " + shift.getStartTime();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Error sending notification", e);
        }
    }
}