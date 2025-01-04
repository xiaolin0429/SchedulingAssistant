package com.schedule.assistant.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    public HomeViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        loadCurrentMonthShifts();
    }

    private void loadCurrentMonthShifts() {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        LiveData<List<Shift>> shiftsLiveData = shiftRepository.getShiftsBetween(
            startOfMonth.format(DateTimeFormatter.ISO_DATE),
            endOfMonth.format(DateTimeFormatter.ISO_DATE)
        );
        shiftsLiveData.observeForever(shifts -> monthShifts.setValue(shifts));
    }

    public void selectDate(LocalDate date) {
        if (date != null) {
            LiveData<Shift> shiftLiveData = shiftRepository.getShiftByDate(
                date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );
            shiftLiveData.observeForever(shift -> {
                selectedShift.setValue(shift);
            });
        } else {
            selectedShift.setValue(null);
        }
    }

    public void insertShift(Shift shift) {
        shiftRepository.insert(shift);
        // 插入后刷新数据
        selectDate(LocalDate.parse(shift.getDate()));
        loadMonthShifts(YearMonth.from(LocalDate.parse(shift.getDate())));
    }

    public void updateShift(Shift shift) {
        shiftRepository.update(shift);
    }

    public void deleteShift(Shift shift) {
        shiftRepository.delete(shift);
    }

    public LiveData<Shift> getSelectedShift() {
        return selectedShift;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public void loadMonthShifts(YearMonth yearMonth) {
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LiveData<List<Shift>> shiftsLiveData = shiftRepository.getShiftsBetween(
            startOfMonth.format(DateTimeFormatter.ISO_DATE),
            endOfMonth.format(DateTimeFormatter.ISO_DATE)
        );
        shiftsLiveData.observeForever(shifts -> {
            monthShifts.setValue(shifts != null ? shifts : Collections.emptyList());
        });
    }
} 