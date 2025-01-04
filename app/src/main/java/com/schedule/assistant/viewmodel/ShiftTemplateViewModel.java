package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.repository.ShiftTemplateRepository;

public class ShiftTemplateViewModel extends AndroidViewModel {
    private final ShiftTemplateRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ShiftTemplateViewModel(@NonNull Application application) {
        super(application);
        repository = new ShiftTemplateRepository(application);
        // 初始化默认班次模板
        repository.initializeDefaultTemplates();
    }

    public LiveData<List<ShiftTemplate>> getAllTemplates() {
        return repository.getAllTemplates();
    }

    public LiveData<List<ShiftTemplate>> getDefaultTemplates() {
        return repository.getDefaultTemplates();
    }

    public void insert(ShiftTemplate template) {
        try {
            repository.insert(template);
        } catch (Exception e) {
            errorMessage.setValue("添加班次模板失败: " + e.getMessage());
        }
    }

    public void update(ShiftTemplate template) {
        try {
            repository.update(template);
        } catch (Exception e) {
            errorMessage.setValue("更新班次模板失败: " + e.getMessage());
        }
    }

    public void delete(ShiftTemplate template) {
        try {
            if (template.isDefault()) {
                errorMessage.setValue("默认班次模板不能删除");
                return;
            }
            repository.delete(template);
        } catch (Exception e) {
            errorMessage.setValue("删除班次模板失败: " + e.getMessage());
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
} 