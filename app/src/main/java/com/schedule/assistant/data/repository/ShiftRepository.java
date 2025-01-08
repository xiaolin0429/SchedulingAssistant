package com.schedule.assistant.data.repository;

import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Application;
import com.schedule.assistant.data.AppDatabase;

public class ShiftRepository {
    private final ShiftDao shiftDao;
    private final ExecutorService executorService;
    private OnOperationCallback callback;

    public interface OnOperationCallback {
        void onError(String error);
    }

    public void setCallback(OnOperationCallback callback) {
        this.callback = callback;
    }

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
        if (shift == null) {
            if (callback != null) {
                callback.onError("error_invalid_shift");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                // 验证必要字段
                if (shift.getDate() == null || shift.getDate().trim().isEmpty()) {
                    throw new IllegalArgumentException("Date cannot be null or empty");
                }
                if (shift.getType() == null || shift.getType() == ShiftType.NO_SHIFT) {
                    throw new IllegalArgumentException("Invalid shift type");
                }

                // 确保所有字符串字段不为null
                if (shift.getStartTime() == null)
                    shift.setStartTime("");
                if (shift.getEndTime() == null)
                    shift.setEndTime("");
                if (shift.getNote() == null)
                    shift.setNote("");

                // 检查是否已存在相同日期的班次
                Shift existingShift = shiftDao.getShiftByDateDirect(shift.getDate());
                if (existingShift != null) {
                    // 如果已存在，更新而不是插入
                    shift.setId(existingShift.getId());
                    shiftDao.update(shift);
                } else {
                    shiftDao.insert(shift);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError(e.getMessage() != null ? e.getMessage() : "error_database_operation");
                }
            }
        });
    }

    public void update(Shift shift) {
        if (shift == null) {
            if (callback != null)
                callback.onError("error_invalid_shift");
            return;
        }

        // 确保必需字段不为空
        if (shift.getDate() == null || shift.getType() == null) {
            if (callback != null)
                callback.onError("error_required_fields");
            return;
        }

        executorService.execute(() -> {
            try {
                shiftDao.update(shift);
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onError("error_database_operation");
                }
            }
        });
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