package com.schedule.assistant.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import android.os.Handler;
import android.os.Looper;

public class HomeViewModel extends AndroidViewModel {
    private final ShiftRepository shiftRepository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Shift> selectedShift = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> monthShifts = new MutableLiveData<>();
    private LiveData<Shift> currentShiftLiveData;
    private LiveData<List<Shift>> currentMonthShiftsLiveData;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isRefreshing = false;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Observer<Shift> shiftObserver;
    private Observer<List<Shift>> monthShiftsObserver;
    private LocalDate currentDate;
    private YearMonth currentMonth;

    public HomeViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        shiftTypeRepository = new ShiftTypeRepository(application);
        initializeObservers();
    }

    private void initializeObservers() {
        shiftObserver = shift -> {
            if (!isRefreshing) {
                mainHandler.post(() -> selectedShift.setValue(shift));
            }
        };

        monthShiftsObserver = shifts -> {
            if (!isRefreshing) {
                mainHandler.post(() -> monthShifts.setValue(shifts));
            }
        };
    }

    public void selectDate(LocalDate date) {
        if (date == null || isRefreshing || (currentDate != null && currentDate.equals(date))) {
            return;
        }

        currentDate = date;
        String dateStr = date.format(dateFormatter);
        updateSelectedShiftObserver(dateStr);

        // 只有在月份变化时才刷新月度数据
        YearMonth newMonth = YearMonth.from(date);
        if (currentMonth == null || !currentMonth.equals(newMonth)) {
            currentMonth = newMonth;
            loadMonthShifts(newMonth);
        }
    }

    private void updateSelectedShiftObserver(String dateStr) {
        if (currentShiftLiveData != null) {
            currentShiftLiveData.removeObserver(shiftObserver);
        }
        currentShiftLiveData = shiftRepository.getShiftByDate(dateStr);
        currentShiftLiveData.observeForever(shiftObserver);
    }

    public void insertShift(Shift shift) {
        try {
            if (shift.getStartTime() == null)
                shift.setStartTime("");
            if (shift.getEndTime() == null)
                shift.setEndTime("");
            if (shift.getNote() == null)
                shift.setNote("");
            shift.setUpdateTime(System.currentTimeMillis());

            shiftRepository.insert(shift, new ShiftRepository.RepositoryCallback() {
                @Override
                public void onSuccess() {
                    mainHandler.post(() -> {
                        try {
                            isRefreshing = true;
                            LocalDate shiftDate = LocalDate.parse(shift.getDate(), dateFormatter);
                            refreshAllData(shiftDate);
                        } finally {
                            isRefreshing = false;
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    mainHandler.post(() -> errorMessage.setValue(error));
                }
            });
        } catch (Exception e) {
            mainHandler.post(() -> errorMessage.setValue("error_save_shift"));
        }
    }

    private void refreshAllData(LocalDate date) {
        if (date == null)
            return;

        YearMonth month = YearMonth.from(date);
        updateMonthShiftsObserver(month);
        updateSelectedShiftObserver(date.format(dateFormatter));
    }

    private void updateMonthShiftsObserver(YearMonth month) {
        String startDate = month.atDay(1).format(dateFormatter);
        String endDate = month.atEndOfMonth().format(dateFormatter);

        if (currentMonthShiftsLiveData != null) {
            currentMonthShiftsLiveData.removeObserver(monthShiftsObserver);
        }

        currentMonthShiftsLiveData = shiftRepository.getShiftsBetween(startDate, endDate);
        currentMonthShiftsLiveData.observeForever(monthShiftsObserver);
    }

    public void loadMonthShifts(YearMonth month) {
        if (month == null || isRefreshing || (currentMonth != null && currentMonth.equals(month))) {
            return;
        }

        currentMonth = month;
        updateMonthShiftsObserver(month);
    }

    public LiveData<Shift> getSelectedShift() {
        return selectedShift;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        return shiftTypeRepository.getAllShiftTypes();
    }

    public LiveData<ShiftTypeEntity> getShiftTypeById(long id) {
        return shiftTypeRepository.getShiftTypeById(id);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mainHandler.removeCallbacksAndMessages(null);
        if (currentShiftLiveData != null) {
            currentShiftLiveData.removeObserver(shiftObserver);
        }
        if (currentMonthShiftsLiveData != null) {
            currentMonthShiftsLiveData.removeObserver(monthShiftsObserver);
        }
    }
}