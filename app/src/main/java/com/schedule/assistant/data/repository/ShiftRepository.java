package com.schedule.assistant.data.repository;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Shift;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Application;
import com.schedule.assistant.data.AppDatabase;

public class ShiftRepository {
    private final ShiftDao shiftDao;
    private final ExecutorService executorService;

    public ShiftRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        shiftDao = db.shiftDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Shift>> getAllShifts() {
        return shiftDao.getAllShifts();
    }

    public LiveData<List<Shift>> getShiftsBetween(String startDate, String endDate) {
        return shiftDao.getShiftsBetween(startDate, endDate);
    }

    public LiveData<Shift> getShiftByDate(String date) {
        return shiftDao.getShiftByDate(date);
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

    public LiveData<List<Shift>> getShiftsWithNotes() {
        return shiftDao.getShiftsWithNotes();
    }

    public void updateNote(long shiftId, String note) {
        executorService.execute(() -> shiftDao.updateNote(shiftId, note));
    }

    public void getShiftByDateDirect(String date, OnShiftLoadedCallback callback) {
        executorService.execute(() -> {
            Shift shift = shiftDao.getShiftByDateDirect(date);
            callback.onShiftLoaded(shift);
        });
    }

    public interface OnShiftLoadedCallback {
        void onShiftLoaded(Shift shift);
    }
} 