package com.schedule.assistant.ui.stats;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.repository.ShiftRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsViewModel extends AndroidViewModel {
    private final ShiftRepository repository;
    private final MutableLiveData<Date> selectedMonth = new MutableLiveData<>();
    private LiveData<List<Shift>> monthShifts;
    private LiveData<Map<ShiftType, Integer>> shiftTypeCounts;

    public StatsViewModel(Application application) {
        super(application);
        repository = new ShiftRepository(application);
        
        // Initialize with current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        selectMonth(calendar.getTime());
    }

    public void selectMonth(Date month) {
        selectedMonth.setValue(month);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        Date end = calendar.getTime();

        monthShifts = repository.getShiftsBetween(start.toString(), end.toString());
        
        // Transform shifts list to type counts map
        shiftTypeCounts = Transformations.map(monthShifts, shifts -> 
            shifts.stream().collect(Collectors.groupingBy(
                Shift::getShiftType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ))
        );
    }

    public LiveData<Date> getSelectedMonth() {
        return selectedMonth;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public LiveData<Map<ShiftType, Integer>> getShiftTypeCounts() {
        return shiftTypeCounts;
    }
} 