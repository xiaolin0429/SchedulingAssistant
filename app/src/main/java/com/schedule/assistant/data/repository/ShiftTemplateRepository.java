package com.schedule.assistant.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.ShiftTemplateDao;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftType;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftTemplateRepository {
    private final ShiftTemplateDao shiftTemplateDao;
    private final ExecutorService executorService;

    public ShiftTemplateRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        shiftTemplateDao = database.shiftTemplateDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(ShiftTemplate template) {
        executorService.execute(() -> shiftTemplateDao.insert(template));
    }

    public void update(ShiftTemplate template) {
        executorService.execute(() -> shiftTemplateDao.update(template));
    }

    public void delete(ShiftTemplate template) {
        executorService.execute(() -> shiftTemplateDao.delete(template));
    }

    public LiveData<List<ShiftTemplate>> getAllTemplates() {
        return shiftTemplateDao.getAllTemplates();
    }

    public LiveData<List<ShiftTemplate>> getDefaultTemplates() {
        return shiftTemplateDao.getDefaultTemplates();
    }

    public LiveData<ShiftTemplate> getTemplateById(long id) {
        return shiftTemplateDao.getTemplateById(id);
    }

    public void initializeDefaultTemplates() {
        executorService.execute(() -> {
            if (shiftTemplateDao.getTemplateCount() == 0) {
                try {
                    // 创建默认班次模板
                    ShiftTemplate dayShift = new ShiftTemplate("早班", "08:00", "18:00", 0xFF4CAF50);
                    dayShift.setDefault(true);
                    dayShift.setType(ShiftType.DAY_SHIFT);
                    dayShift.setUpdateTime(System.currentTimeMillis());
                    
                    ShiftTemplate nightShift = new ShiftTemplate("夜班", "20:00", "08:00", 0xFF2196F3);
                    nightShift.setDefault(true);
                    nightShift.setType(ShiftType.NIGHT_SHIFT);
                    nightShift.setUpdateTime(System.currentTimeMillis());
                    
                    ShiftTemplate restDay = new ShiftTemplate("休息", "-", "-", 0xFFFF9800);
                    restDay.setDefault(true);
                    restDay.setType(ShiftType.REST_DAY);
                    restDay.setUpdateTime(System.currentTimeMillis());

                    shiftTemplateDao.insert(dayShift);
                    shiftTemplateDao.insert(nightShift);
                    shiftTemplateDao.insert(restDay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
} 