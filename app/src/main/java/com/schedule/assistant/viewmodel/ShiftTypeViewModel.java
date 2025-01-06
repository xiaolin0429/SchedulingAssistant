package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import java.util.List;

public class ShiftTypeViewModel extends AndroidViewModel {
    private final ShiftTypeRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Long> shiftTypeUpdateEvent = new MutableLiveData<>();

    public ShiftTypeViewModel(@NonNull Application application) {
        super(application);
        repository = new ShiftTypeRepository(application);
        // 初始化默认班次类型
        repository.initializeDefaultShiftTypes();
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        return repository.getAllShiftTypes();
    }

    public LiveData<List<ShiftTypeEntity>> getDefaultShiftTypes() {
        return repository.getDefaultShiftTypes();
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
            repository.update(shiftType);
            shiftTypeUpdateEvent.setValue(System.currentTimeMillis());
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

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Long> getShiftTypeUpdateEvent() {
        return shiftTypeUpdateEvent;
    }
} 