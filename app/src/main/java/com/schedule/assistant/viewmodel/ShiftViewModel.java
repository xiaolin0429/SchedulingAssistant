package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.SortOption;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTemplateRepository;
import java.util.List;

public class ShiftViewModel extends AndroidViewModel {
    private final ShiftRepository shiftRepository;
    private final ShiftTemplateRepository templateRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<SortOption> currentSortOption = new MutableLiveData<>(SortOption.DATE_ASC);
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>(true);
    private final MutableLiveData<List<Shift>> shiftsData = new MutableLiveData<>();

    public ShiftViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        templateRepository = new ShiftTemplateRepository(application);

        // 初始加载数据
        loadShifts();
    }

    private void loadShifts() {
        shiftRepository.getAllShifts().observeForever(shifts -> {
            shiftsData.setValue(shifts);
        });
    }

    // 添加刷新方法
    public void refreshShifts() {
        loadShifts();
    }

    public LiveData<List<ShiftTemplate>> getDefaultTemplates() {
        return templateRepository.getDefaultTemplates();
    }

    public LiveData<List<ShiftTemplate>> getAllTemplates() {
        return templateRepository.getAllTemplates();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<Shift>> getAllShifts() {
        return shiftsData;
    }

    public LiveData<SortOption> getCurrentSortOption() {
        return currentSortOption;
    }

    public LiveData<Boolean> getIsAscending() {
        return isAscending;
    }

    public void setSortOption(SortOption option) {
        currentSortOption.setValue(option);
        isAscending.setValue(option == SortOption.DATE_ASC);
    }

    public void insertShift(Shift shift) {
        try {
            shiftRepository.insert(shift);
            refreshShifts();
        } catch (Exception e) {
            errorMessage.setValue("error_insert_failed");
        }
    }

    public void updateShift(Shift shift) {
        try {
            shiftRepository.update(shift);
            refreshShifts();
        } catch (Exception e) {
            errorMessage.setValue("error_update_failed");
        }
    }

    public void delete(Shift shift) {
        try {
            shiftRepository.delete(shift);
            refreshShifts();
        } catch (Exception e) {
            errorMessage.setValue("error_delete_failed");
        }
    }

    public void updateNote(String date, String note) {
        try {
            shiftRepository.getShiftByDateDirect(date, shift -> {
                if (shift == null) {
                    Shift newShift = new Shift(date, ShiftType.NO_SHIFT);
                    newShift.setNote(note);
                    shiftRepository.insert(newShift);
                } else {
                    shift.setNote(note);
                    shiftRepository.update(shift);
                }
            });
        } catch (Exception e) {
            errorMessage.setValue("error_update_note_failed");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 移除观察者，防止内存泄漏
        shiftRepository.getAllShifts().removeObserver(shifts -> {
        });
    }
}