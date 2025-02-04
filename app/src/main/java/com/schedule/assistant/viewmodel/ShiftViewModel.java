package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.SortOption;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTemplateRepository;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;

public class ShiftViewModel extends AndroidViewModel {
    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository templateRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<SortOption> currentSortOption = new MutableLiveData<>(SortOption.DATE_ASC);
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>(true);
    private final MutableLiveData<List<Shift>> shiftsData = new MutableLiveData<>();
    private LiveData<List<Shift>> allShiftsLiveData;
    private final androidx.lifecycle.Observer<List<Shift>> shiftsObserver;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ShiftViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        templateRepository = new ShiftTemplateRepository(application);

        // 创建观察者
        shiftsObserver = shifts -> {
            if (shifts != null) {
                // 确保在主线程更新UI数据
                mainHandler.post(() -> shiftsData.setValue(shifts));
            }
        };

        // 初始化LiveData并添加观察者
        initializeShiftsObserver();
    }

    private void initializeShiftsObserver() {
        if (allShiftsLiveData != null && shiftsObserver != null) {
            allShiftsLiveData.removeObserver(shiftsObserver);
        }
        allShiftsLiveData = shiftRepository.getAllShifts();
        if (shiftsObserver != null) {
            allShiftsLiveData.observeForever(shiftsObserver);
        }
    }

    // 强制刷新数据
    public void refreshShifts() {
        mainHandler.post(() -> {
            initializeShiftsObserver();
            // 通知数据已更新
            List<Shift> currentShifts = shiftsData.getValue();
            if (currentShifts != null) {
                shiftsData.setValue(new ArrayList<>(currentShifts));
            }
        });
    }

    public LiveData<List<ShiftTemplate>> getAllTemplates() {
        return templateRepository.getAllTemplates();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setSortOption(SortOption option) {
        currentSortOption.setValue(option);
        isAscending.setValue(option == SortOption.DATE_ASC);
    }

    public void insertShift(Shift shift) {
        try {
            shiftRepository.insert(shift, new ShiftRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        refreshShifts();
                        // 确保UI立即更新
                        initializeShiftsObserver();
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> errorMessage.setValue("error_insert_failed"));
                }
            });
        } catch (Exception e) {
            mainHandler.post(() -> errorMessage.setValue("error_insert_failed"));
        }
    }

    public void updateShift(Shift shift) {
        try {
            shiftRepository.update(shift, new ShiftRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        refreshShifts();
                        // 确保UI立即更新
                        initializeShiftsObserver();
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> errorMessage.setValue("error_update_failed"));
                }
            });
        } catch (Exception e) {
            mainHandler.post(() -> errorMessage.setValue("error_update_failed"));
        }
    }

    public void delete(Shift shift) {
        try {
            shiftRepository.delete(shift, new ShiftRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        refreshShifts();
                        // 确保UI立即更新
                        initializeShiftsObserver();
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> errorMessage.setValue("error_delete_failed"));
                }
            });
        } catch (Exception e) {
            mainHandler.post(() -> errorMessage.setValue("error_delete_failed"));
        }
    }

    public void updateNote(String date, String note) {
        try {
            shiftRepository.getShiftByDateDirect(date, shift -> {
                if (shift == null) {
                    Shift newShift = new Shift(date, ShiftType.NO_SHIFT);
                    newShift.setNote(note);
                    shiftRepository.insert(newShift, new ShiftRepository.RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            refreshShifts();
                        }

                        @Override
                        public void onError(String error) {
                            errorMessage.setValue("error_update_note_failed");
                        }
                    });
                } else {
                    shift.setNote(note);
                    shiftRepository.update(shift, new ShiftRepository.RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            refreshShifts();
                        }

                        @Override
                        public void onError(String error) {
                            errorMessage.setValue("error_update_note_failed");
                        }
                    });
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("error_update_note_failed");
        }
    }

    public void updateShiftTemplate(ShiftTemplate template, boolean updateExistingShifts) {
        try {
            templateRepository.update(template);
            if (updateExistingShifts) {
                // 更新所有相关班次的时间
                shiftRepository.updateShiftTimesByType(
                        template.getId(),
                        template.getStartTime(),
                        template.getEndTime(),
                        new ShiftRepository.RepositoryCallback() {
                            @Override
                            public void onSuccess() {
                                mainHandler.post(() -> refreshShifts());
                            }

                            @Override
                            public void onError(String error) {
                                mainHandler.post(() -> errorMessage.setValue(error));
                            }
                        });
            } else {
                mainHandler.post(() -> refreshShifts());
            }
        } catch (Exception e) {
            mainHandler.post(() -> errorMessage.setValue("error_update_failed"));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (allShiftsLiveData != null && shiftsObserver != null) {
            allShiftsLiveData.removeObserver(shiftsObserver);
        }
        mainHandler.removeCallbacksAndMessages(null);
    }
}