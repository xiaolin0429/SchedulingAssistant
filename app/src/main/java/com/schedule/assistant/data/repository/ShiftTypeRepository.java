package com.schedule.assistant.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.ShiftTypeDao;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftTypeRepository {
    private static final String TAG = "ShiftTypeRepository";
    private final ShiftTypeDao shiftTypeDao;
    private final ExecutorService executorService;

    public ShiftTypeRepository(Application application) {
        Log.d(TAG, "初始化ShiftTypeRepository");
        AppDatabase database = AppDatabase.getDatabase(application);
        shiftTypeDao = database.shiftTypeDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(ShiftTypeEntity shiftType) {
        executorService.execute(() -> {
            try {
                long id = shiftTypeDao.insert(shiftType);
                Log.d(TAG, "班次类型插入成功，ID: " + id);
            } catch (Exception e) {
                Log.e(TAG, "班次类型插入失败", e);
            }
        });
    }

    public void update(ShiftTypeEntity shiftType) {
        executorService.execute(() -> shiftTypeDao.update(shiftType));
    }

    public void delete(ShiftTypeEntity shiftType) {
        executorService.execute(() -> shiftTypeDao.delete(shiftType));
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        Log.d(TAG, "getAllShiftTypes: 获取所有班次类型");
        return shiftTypeDao.getAllShiftTypes();
    }

    public LiveData<ShiftTypeEntity> getShiftTypeById(long id) {
        return shiftTypeDao.getShiftTypeById(id);
    }

    public void initializeDefaultShiftTypes() {
        Log.d(TAG, "initializeDefaultShiftTypes: 开始初始化默认班次类型");
        executorService.execute(() -> {
            try {
                int count = shiftTypeDao.getShiftTypeCount();
                Log.d(TAG, "当前班次类型数量: " + count);
                if (count == 0) {
                    Log.d(TAG, "开始创建默认班次类型");
                    // 创建默认班次类型
                    ShiftTypeEntity dayShift = new ShiftTypeEntity("早班", "08:00", "18:00", 0xFF4CAF50,
                            ShiftType.DAY_SHIFT);
                    dayShift.setDefault(true);
                    dayShift.setUpdateTime(System.currentTimeMillis());

                    ShiftTypeEntity nightShift = new ShiftTypeEntity("夜班", "20:00", "08:00", 0xFF2196F3,
                            ShiftType.NIGHT_SHIFT);
                    nightShift.setDefault(true);
                    nightShift.setUpdateTime(System.currentTimeMillis());

                    ShiftTypeEntity restDay = new ShiftTypeEntity("休息", "-", "-", 0xFFFF9800, ShiftType.REST_DAY);
                    restDay.setDefault(true);
                    restDay.setUpdateTime(System.currentTimeMillis());

                    try {
                        long dayShiftId = shiftTypeDao.insert(dayShift);
                        long nightShiftId = shiftTypeDao.insert(nightShift);
                        long restDayId = shiftTypeDao.insert(restDay);
                        Log.d(TAG, String.format("默认班次类型创建成功 - 早班ID: %d, 夜班ID: %d, 休息ID: %d",
                                dayShiftId, nightShiftId, restDayId));
                    } catch (Exception e) {
                        Log.e(TAG, "创建默认班次类型失败", e);
                    }
                } else {
                    Log.d(TAG, "已存在班次类型，跳过初始化");
                }
            } catch (Exception e) {
                Log.e(TAG, "初始化默认班次类型时发生异常", e);
            }
        });
    }

    public void getShiftTypeByIdDirect(long id, OnShiftTypeLoadedCallback callback) {
        executorService.execute(() -> {
            ShiftTypeEntity shiftType = shiftTypeDao.getShiftTypeByIdDirect(id);
            if (callback != null) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onShiftTypeLoaded(shiftType));
            }
        });
    }

    public void getAllTypesSync(OnShiftTypesLoadedCallback callback) {
        Log.d(TAG, "getAllTypesSync: 开始同步获取所有班次类型");
        executorService.execute(() -> {
            try {
                List<ShiftTypeEntity> types = shiftTypeDao.getAllTypesSync();
                Log.d(TAG, "getAllTypesSync: 获取到 " + (types != null ? types.size() : 0) + " 个班次类型");
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onShiftTypesLoaded(types));
                }
            } catch (Exception e) {
                Log.e(TAG, "getAllTypesSync: 获取班次类型失败", e);
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onShiftTypesLoaded(null));
                }
            }
        });
    }

    public interface OnShiftTypeLoadedCallback {
        void onShiftTypeLoaded(ShiftTypeEntity shiftType);
    }

    public interface OnShiftTypesLoadedCallback {
        void onShiftTypesLoaded(List<ShiftTypeEntity> types);
    }
}