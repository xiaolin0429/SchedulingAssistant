package com.schedule.assistant.data.repository;

import androidx.lifecycle.LiveData;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.schedule.assistant.data.dao.ShiftDao;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftRepository {
    private static final String TAG = "ShiftRepository";
    private final ShiftDao shiftDao;
    private final ExecutorService executorService;
    private RepositoryCallback callback;

    public interface RepositoryCallback {
        void onSuccess();

        void onError(String error);
    }

    public ShiftRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        shiftDao = db.shiftDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void setCallback(RepositoryCallback callback) {
        this.callback = callback;
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

    public void insert(Shift shift, RepositoryCallback callback) {
        if (shift == null) {
            if (callback != null) {
                callback.onError("error_invalid_shift");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                // 验证日期字符串
                String date = shift.getDate();
                if (date.trim().isEmpty()) {
                    throw new IllegalArgumentException("error_date_required");
                }

                // 验证班次类型
                if (shift.getType() == ShiftType.NO_SHIFT) {
                    throw new IllegalArgumentException("error_shift_type_required");
                }

                // 确保所有字符串字段不为null
                shift.setStartTime(shift.getStartTime() != null ? shift.getStartTime() : "");
                shift.setEndTime(shift.getEndTime() != null ? shift.getEndTime() : "");
                shift.setNote(shift.getNote() != null ? shift.getNote() : "");

                // 检查是否已存在相同日期的班次
                Shift existingShift = shiftDao.getShiftByDateDirect(date);
                if (existingShift != null) {
                    // 如果已存在，更新而不是插入
                    shift.setId(existingShift.getId());
                    shiftDao.update(shift);
                } else {
                    shiftDao.insert(shift);
                }

                // 在主线程中回调成功
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Validation error: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Database operation error", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onError(e.getMessage() != null ? e.getMessage() : "error_database_operation");
                    }
                });
            }
        });
    }

    public void update(Shift shift, RepositoryCallback callback) {
        if (shift == null) {
            if (callback != null) {
                callback.onError("error_invalid_shift");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                shiftDao.update(shift);
                // 在主线程中回调成功
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating shift", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onError("error_database_operation");
                    }
                });
            }
        });
    }

    public void delete(Shift shift, RepositoryCallback callback) {
        if (shift == null) {
            if (callback != null) {
                callback.onError("error_invalid_shift");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                shiftDao.delete(shift);
                // 在主线程中回调成功
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting shift", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onError("error_database_operation");
                    }
                });
            }
        });
    }

    public void getShiftByDateDirect(String date, OnShiftLoadedCallback callback) {
        executorService.execute(() -> {
            Shift shift = shiftDao.getShiftByDateDirect(date);
            if (callback != null) {
                callback.onShiftLoaded(shift);
            }
        });
    }

    public interface OnShiftLoadedCallback {
        void onShiftLoaded(Shift shift);
    }
}