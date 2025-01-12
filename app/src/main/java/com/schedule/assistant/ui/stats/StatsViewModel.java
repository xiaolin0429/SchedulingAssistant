package com.schedule.assistant.ui.stats;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.HashMap;
import android.util.Log;
import java.util.ArrayList;

public class StatsViewModel extends AndroidViewModel {
    private static final String TAG = "StatsViewModel";
    private final ShiftRepository repository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final MutableLiveData<Date> selectedMonth = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> monthShifts = new MutableLiveData<>();
    private final MutableLiveData<Map<Long, Integer>> shiftTypeCounts = new MutableLiveData<>();
    private final MutableLiveData<Map<Long, Double>> shiftTypePercentages = new MutableLiveData<>();
    private final MutableLiveData<Double> totalWorkHours = new MutableLiveData<>();
    private final MutableLiveData<Double> averageWorkHours = new MutableLiveData<>();
    private final MutableLiveData<WorkHoursRecord> workHoursRecord = new MutableLiveData<>();
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    private LiveData<List<Shift>> currentShiftsLiveData;
    private androidx.lifecycle.Observer<List<Shift>> shiftsObserver;

    @Override
    protected void onCleared() {
        super.onCleared();
        if (currentShiftsLiveData != null && shiftsObserver != null) {
            currentShiftsLiveData.removeObserver(shiftsObserver);
        }
    }

    public record WorkHoursRecord(double maxHours, String maxDate, double minHours, String minDate) {
    }

    public StatsViewModel(Application application) {
        super(application);
        repository = new ShiftRepository(application);
        shiftTypeRepository = new ShiftTypeRepository(application);
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 初始化为当前月份
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        selectMonth(calendar.getTime());
    }

    public void selectMonth(Date month) {
        if (month == null)
            return;

        // 移除之前的观察者
        if (currentShiftsLiveData != null && shiftsObserver != null) {
            currentShiftsLiveData.removeObserver(shiftsObserver);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);
        Log.d(TAG, "Fetching shifts between " + startDateStr + " and " + endDateStr);

        selectedMonth.setValue(month);

        // 观察班次数据的变化并更新统计信息
        currentShiftsLiveData = repository.getShiftsBetween(startDateStr, endDateStr);
        shiftsObserver = shifts -> {
            if (shifts != null) {
                Log.d(TAG, "Found " + shifts.size() + " shifts for the selected month");
                monthShifts.setValue(shifts);
                updateShiftTypeCounts(shifts);
                updateWorkHoursStatistics(shifts);
            } else {
                Log.d(TAG, "No shifts found for the selected month");
                monthShifts.setValue(new ArrayList<>());
                shiftTypeCounts.setValue(new HashMap<>());
                shiftTypePercentages.setValue(new HashMap<>());
                totalWorkHours.setValue(0.0);
                averageWorkHours.setValue(0.0);
                workHoursRecord.setValue(null);
            }
        };
        currentShiftsLiveData.observeForever(shiftsObserver);
    }

    private void updateShiftTypeCounts(List<Shift> shifts) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Shift shift : shifts) {
            long typeId = shift.getShiftTypeId();
            counts.compute(typeId, (k, count) -> count == null ? 1 : count + 1);
        }
        shiftTypeCounts.setValue(counts);

        // 计算百分比
        Map<Long, Double> percentages = new HashMap<>();
        if (!shifts.isEmpty()) {
            int total = shifts.size();
            for (Map.Entry<Long, Integer> entry : counts.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / total;
                percentages.put(entry.getKey(), percentage);
            }
        }
        shiftTypePercentages.setValue(percentages);
    }

    private void updateWorkHoursStatistics(List<Shift> shifts) {
        double total = 0;
        double maxHours = 0;
        double minHours = Double.MAX_VALUE;
        String maxDate = "";
        String minDate = "";

        for (Shift shift : shifts) {
            try {
                String startTime = shift.getStartTime();
                String endTime = shift.getEndTime();

                if (startTime == null || endTime == null ||
                        startTime.equals("-") || endTime.equals("-")) {
                    continue;
                }

                double hours = calculateWorkHours(startTime, endTime);
                if (hours > 0) {
                    total += hours;

                    if (hours > maxHours) {
                        maxHours = hours;
                        maxDate = shift.getDate();
                    }
                    if (hours < minHours) {
                        minHours = hours;
                        minDate = shift.getDate();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error calculating work hours for shift", e);
            }
        }

        totalWorkHours.setValue(total);

        if (!shifts.isEmpty()) {
            averageWorkHours.setValue(total / shifts.size());
        } else {
            averageWorkHours.setValue(0.0);
        }

        if (!maxDate.isEmpty() && !minDate.isEmpty()) {
            workHoursRecord.setValue(new WorkHoursRecord(maxHours, maxDate, minHours, minDate));
        } else {
            workHoursRecord.setValue(null);
        }
    }

    private double calculateWorkHours(String startTime, String endTime) {
        try {
            Date startTimeDate = timeFormat.parse(startTime);
            Date endTimeDate = timeFormat.parse(endTime);

            if (startTimeDate == null || endTimeDate == null) {
                return 0;
            }

            long diffMillis = endTimeDate.getTime() - startTimeDate.getTime();
            if (diffMillis < 0) {
                // 跨天处理
                diffMillis += 24 * 60 * 60 * 1000;
            }

            return diffMillis / (1000.0 * 60 * 60);
        } catch (Exception e) {
            Log.e("StatsViewModel", "Error calculating work hours: " + e.getMessage());
            return 0;
        }
    }

    public LiveData<Date> getSelectedMonth() {
        return selectedMonth;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public LiveData<Map<Long, Integer>> getShiftTypeCounts() {
        return shiftTypeCounts;
    }

    public LiveData<Map<Long, Double>> getShiftTypePercentages() {
        return shiftTypePercentages;
    }

    public LiveData<Double> getTotalWorkHours() {
        return totalWorkHours;
    }

    public LiveData<Double> getAverageWorkHours() {
        return averageWorkHours;
    }

    public LiveData<WorkHoursRecord> getWorkHoursRecord() {
        return workHoursRecord;
    }

    public LiveData<String> getShiftTypeName(long shiftTypeId) {
        return Transformations.map(shiftTypeRepository.getShiftTypeById(shiftTypeId), shiftType -> {
            if (shiftType != null) {
                return shiftType.getName();
            }
            return "未知班次";
        });
    }

    public LiveData<Integer> getShiftTypeColor(long shiftTypeId) {
        return Transformations.map(shiftTypeRepository.getShiftTypeById(shiftTypeId), shiftType -> {
            if (shiftType != null) {
                try {
                    return shiftType.getColor();
                } catch (Exception e) {
                    Log.e(TAG, "Error getting color for shift type: " + shiftTypeId, e);
                    return null;
                }
            }
            return null;
        });
    }
}