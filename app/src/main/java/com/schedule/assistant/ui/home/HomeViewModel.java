package com.schedule.assistant.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.repository.ShiftRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ShiftRepository shiftRepository;
    private final MutableLiveData<Shift> selectedShift = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> monthShifts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LiveData<Shift> currentShiftLiveData;
    private LiveData<List<Shift>> currentMonthShiftsLiveData;

    public HomeViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
    }

    public void selectDate(LocalDate date) {
        if (date != null) {
            String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            currentShiftLiveData = shiftRepository.getShiftByDate(formattedDate);
            currentShiftLiveData.observeForever(selectedShift::setValue);
        } else {
            selectedShift.setValue(null);
        }
    }

    public void insertShift(Shift shift) {
        try {
            shiftRepository.insert(shift);
            // 插入后刷新数据
            LocalDate shiftDate = LocalDate.parse(shift.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            selectDate(shiftDate);
            loadMonthShifts(YearMonth.from(shiftDate));
        } catch (Exception e) {
            errorMessage.setValue("Failed to insert shift: " + e.getMessage());
        }
    }

    public void updateShift(Shift shift) {
        try {
            shiftRepository.update(shift);
        } catch (Exception e) {
            errorMessage.setValue("Failed to update shift: " + e.getMessage());
        }
    }

    public void deleteShift(Shift shift) {
        try {
            shiftRepository.delete(shift);
        } catch (Exception e) {
            errorMessage.setValue("Failed to delete shift: " + e.getMessage());
        }
    }

    public LiveData<Shift> getSelectedShift() {
        return selectedShift;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadMonthShifts(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        String startDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        currentMonthShiftsLiveData = shiftRepository.getShiftsBetween(startDate, endDate);
        currentMonthShiftsLiveData.observeForever(monthShifts::setValue);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentShiftLiveData != null) {
            currentShiftLiveData.removeObserver(selectedShift::setValue);
        }
        if (currentMonthShiftsLiveData != null) {
            currentMonthShiftsLiveData.removeObserver(monthShifts::setValue);
        }
    }
} 