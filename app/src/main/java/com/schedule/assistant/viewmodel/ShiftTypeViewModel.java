package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import com.schedule.assistant.data.repository.ShiftRepository;
import java.util.List;

public class ShiftTypeViewModel extends AndroidViewModel {
    private final ShiftTypeRepository repository;
    private final ShiftRepository shiftRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Long> shiftTypeUpdateEvent = new MutableLiveData<>();
    private final MutableLiveData<ShiftTypeEntity> showUpdateConfirmDialog = new MutableLiveData<>();

    public ShiftTypeViewModel(@NonNull Application application) {
        super(application);
        repository = new ShiftTypeRepository(application);
        shiftRepository = new ShiftRepository(application);
        // 初始化默认班次类型
        repository.initializeDefaultShiftTypes();
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        return repository.getAllShiftTypes();
    }

    public void insert(ShiftTypeEntity shiftType) {
        try {
            repository.insert(shiftType);
            shiftTypeUpdateEvent.setValue(System.currentTimeMillis());
        } catch (Exception e) {
            errorMessage.setValue("添加班次类型失败: " + e.getMessage());
        }
    }

    public void update(ShiftTypeEntity shiftType) {
        try {
            // 检查时间是否发生变化
            repository.getShiftTypeByIdDirect(shiftType.getId(), oldShiftType -> {
                if (oldShiftType != null &&
                        (!oldShiftType.getStartTime().equals(shiftType.getStartTime()) ||
                                !oldShiftType.getEndTime().equals(shiftType.getEndTime()))) {
                    // 如果时间发生变化，触发确认对话框
                    showUpdateConfirmDialog.postValue(shiftType);
                } else {
                    // 如果时间没有变化，直接更新
                    try {
                        repository.update(shiftType);
                        shiftTypeUpdateEvent.postValue(System.currentTimeMillis());
                    } catch (Exception e) {
                        errorMessage.postValue("更新班次类型失败: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("更新班次类型失败: " + e.getMessage());
        }
    }

    public void delete(ShiftTypeEntity shiftType) {
        try {
            if (shiftType.isDefault()) {
                errorMessage.setValue("默认班次类型不能删除");
                return;
            }
            repository.delete(shiftType);
            shiftTypeUpdateEvent.setValue(System.currentTimeMillis());
        } catch (Exception e) {
            errorMessage.setValue("删除班次类型失败: " + e.getMessage());
        }
    }

    public void updateWithExistingShifts(ShiftTypeEntity shiftType) {
        try {
            repository.update(shiftType);
            // 更新所有相关班次的时间
            shiftRepository.updateShiftTimesByType(
                    shiftType.getId(),
                    shiftType.getStartTime(),
                    shiftType.getEndTime(),
                    new ShiftRepository.RepositoryCallback() {
                        @Override
                        public void onSuccess() {
                            shiftTypeUpdateEvent.setValue(System.currentTimeMillis());
                        }

                        @Override
                        public void onError(String error) {
                            errorMessage.setValue(error);
                        }
                    });
        } catch (Exception e) {
            errorMessage.setValue("error_update_failed");
        }
    }

    public void updateCurrentOnly(ShiftTypeEntity shiftType) {
        try {
            repository.update(shiftType);
            shiftTypeUpdateEvent.setValue(System.currentTimeMillis());
        } catch (Exception e) {
            errorMessage.setValue("更新班次类型失败: " + e.getMessage());
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<ShiftTypeEntity> getShowUpdateConfirmDialog() {
        return showUpdateConfirmDialog;
    }

    public void clearUpdateConfirmDialog() {
        showUpdateConfirmDialog.setValue(null);
    }

}