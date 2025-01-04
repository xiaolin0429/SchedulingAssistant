package com.schedule.assistant.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.SortOption;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftRepository.OnShiftLoadedCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShiftViewModel extends AndroidViewModel {
    private final ShiftRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<SortOption> currentSortOption = new MutableLiveData<>(SortOption.DATE_ASC);
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>(true);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ShiftViewModel(@NonNull Application application) {
        super(application);
        repository = new ShiftRepository(application);
    }

    public LiveData<List<Shift>> getAllShifts() {
        return repository.getAllShifts();
    }

    public void insert(Shift shift) {
        try {
            repository.insert(shift);
        } catch (Exception e) {
            errorMessage.setValue("Failed to insert shift: " + e.getMessage());
        }
    }

    public void update(Shift shift) {
        try {
            repository.update(shift);
        } catch (Exception e) {
            errorMessage.setValue("Failed to update shift: " + e.getMessage());
        }
    }

    public void delete(Shift shift) {
        try {
            repository.delete(shift);
        } catch (Exception e) {
            errorMessage.setValue("Failed to delete shift: " + e.getMessage());
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<SortOption> getCurrentSortOption() {
        return currentSortOption;
    }

    public void setSortOption(SortOption option) {
        currentSortOption.setValue(option);
        isAscending.setValue(option == SortOption.DATE_ASC);
    }

    public LiveData<Boolean> getIsAscending() {
        return isAscending;
    }

    public void updateNote(String date, String note) {
        executor.execute(() -> {
            repository.getShiftByDateDirect(date, shift -> {
                if (shift == null) {
                    Shift newShift = new Shift(date, ShiftType.NO_SHIFT);
                    newShift.setNote(note);
                    repository.insert(newShift);
                } else {
                    shift.setNote(note);
                    repository.update(shift);
                }
            });
        });
    }
} 