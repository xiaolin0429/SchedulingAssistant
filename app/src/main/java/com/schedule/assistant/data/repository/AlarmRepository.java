package com.schedule.assistant.data.repository;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.AlarmDao;
import com.schedule.assistant.data.entity.AlarmEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 闹钟数据仓库
 * 负责管理闹钟数据的访问和异步操作
 */
public class AlarmRepository {
    private static final String TAG = "AlarmRepository";
    private final AlarmDao alarmDao;
    private final ExecutorService executorService;

    public AlarmRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        alarmDao = db.alarmDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 获取所有闹钟
     */
    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return alarmDao.getAllAlarms();
    }

    /**
     * 获取已启用的闹钟
     */
    public LiveData<List<AlarmEntity>> getEnabledAlarms() {
        return alarmDao.getEnabledAlarms();
    }

    /**
     * 根据ID获取闹钟
     */
    public LiveData<AlarmEntity> getAlarmById(long id) {
        return alarmDao.getAlarmById(id);
    }

    /**
     * 插入闹钟
     */
    public long insert(AlarmEntity alarm) {
        try {
            return executorService.submit(() -> alarmDao.insert(alarm)).get();
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert alarm: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 更新闹钟
     */
    public void update(AlarmEntity alarm) {
        executorService.execute(() -> alarmDao.update(alarm));
    }

    /**
     * 删除闹钟
     */
    public void delete(AlarmEntity alarm) {
        executorService.execute(() -> alarmDao.delete(alarm));
    }

    /**
     * 更新闹钟启用状态
     */
    public void updateEnabled(long id, boolean enabled) {
        executorService.execute(() -> alarmDao.updateEnabled(id, enabled));
    }

    /**
     * 禁用所有闹钟
     */
    public void disableAllAlarms() {
        executorService.execute(() -> alarmDao.disableAllAlarms());
    }
}