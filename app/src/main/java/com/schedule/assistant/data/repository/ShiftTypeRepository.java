package com.schedule.assistant.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.ShiftTypeDao;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftTypeRepository {
    private final ShiftTypeDao shiftTypeDao;
    private final ExecutorService executorService;

    public ShiftTypeRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        shiftTypeDao = database.shiftTypeDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(ShiftTypeEntity shiftType) {
        executorService.execute(() -> shiftTypeDao.insert(shiftType));
    }

    public void update(ShiftTypeEntity shiftType) {
        executorService.execute(() -> shiftTypeDao.update(shiftType));
    }

    public void delete(ShiftTypeEntity shiftType) {
        executorService.execute(() -> shiftTypeDao.delete(shiftType));
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        return shiftTypeDao.getAllShiftTypes();
    }

    public LiveData<List<ShiftTypeEntity>> getDefaultShiftTypes() {
        return shiftTypeDao.getDefaultShiftTypes();
    }

    public LiveData<ShiftTypeEntity> getShiftTypeById(long id) {
        return shiftTypeDao.getShiftTypeById(id);
    }

    public void initializeDefaultShiftTypes() {
        executorService.execute(() -> {
            if (shiftTypeDao.getShiftTypeCount() == 0) {
                // 创建默认班次类型
                ShiftTypeEntity dayShift = new ShiftTypeEntity("早班", "08:00", "18:00", 0xFF4CAF50, ShiftType.DAY_SHIFT);
                dayShift.setDefault(true);
                dayShift.setUpdateTime(System.currentTimeMillis());
                
                ShiftTypeEntity nightShift = new ShiftTypeEntity("夜班", "20:00", "08:00", 0xFF2196F3, ShiftType.NIGHT_SHIFT);
                nightShift.setDefault(true);
                nightShift.setUpdateTime(System.currentTimeMillis());
                
                ShiftTypeEntity restDay = new ShiftTypeEntity("休息", "-", "-", 0xFFFF9800, ShiftType.REST_DAY);
                restDay.setDefault(true);
                restDay.setUpdateTime(System.currentTimeMillis());

                // 确保所有必需字段都有值
                try {
                    shiftTypeDao.insert(dayShift);
                    shiftTypeDao.insert(nightShift);
                    shiftTypeDao.insert(restDay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
} 