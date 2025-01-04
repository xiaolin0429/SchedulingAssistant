package com.schedule.assistant.ui.shift;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.AppDatabase;

import java.time.LocalDate;
import java.util.List;

public class ShiftViewModel extends AndroidViewModel {
    private final ShiftRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> currentMonth = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> shifts = new MutableLiveData<>();

    public ShiftViewModel(Application application) {
        super(application);
        repository = new ShiftRepository(application);
        setMonth(LocalDate.now());
    }

    public LiveData<List<Shift>> getShifts() {
        return shifts;
    }

    public void setMonth(LocalDate month) {
        currentMonth.setValue(month);
        loadMonthShifts(month);
    }

    private void loadMonthShifts(LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = month.withDayOfMonth(month.lengthOfMonth());
        repository.getShiftsBetween(start.toString(), end.toString())
                .observeForever(monthShifts -> shifts.setValue(monthShifts));
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
} 