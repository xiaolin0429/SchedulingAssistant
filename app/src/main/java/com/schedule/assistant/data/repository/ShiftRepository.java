package com.schedule.assistant.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Shift;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftRepository {
    private final ShiftDao shiftDao;
    private final ExecutorService executorService;

    public ShiftRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        shiftDao = db.shiftDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Shift> getShiftByDate(String date) {
        return shiftDao.getShiftByDate(date);
    }

    public LiveData<List<Shift>> getShiftsBetween(String startDate, String endDate) {
        return shiftDao.getShiftsBetween(startDate, endDate);
    }

    public void insert(Shift shift) {
        executorService.execute(() -> shiftDao.insert(shift));
    }

    public void update(Shift shift) {
        executorService.execute(() -> shiftDao.update(shift));
    }

    public void delete(Shift shift) {
        executorService.execute(() -> shiftDao.delete(shift));
    }
} 