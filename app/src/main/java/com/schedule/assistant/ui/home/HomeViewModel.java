package com.schedule.assistant.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTemplateRepository;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final ShiftRepository shiftRepository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final MutableLiveData<Shift> selectedShift = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> monthShifts = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private LiveData<Shift> currentShiftLiveData;
    private LiveData<List<Shift>> currentMonthShiftsLiveData;

    public HomeViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        ShiftTemplateRepository templateRepository = new ShiftTemplateRepository(application);
        shiftTypeRepository = new ShiftTypeRepository(application);
        // 初始化默认班次类型和模板
        shiftTypeRepository.initializeDefaultShiftTypes();
        templateRepository.initializeDefaultTemplates();
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
        if (shift == null) {
            errorMessage.setValue("error_invalid_shift");
            return;
        }

        try {
            // 验证必要字段
            if (shift.getDate().trim().isEmpty()) {
                errorMessage.setValue("error_date_required");
                return;
            }

            if (shift.getType() == ShiftType.NO_SHIFT) {
                errorMessage.setValue("error_type_required");
                return;
            }

            // 确保所有字符串字段不为null
            if (shift.getStartTime() == null) shift.setStartTime("");
            if (shift.getEndTime() == null) shift.setEndTime("");
            if (shift.getNote() == null) shift.setNote("");

            // 设置更新时间
            shift.setUpdateTime(System.currentTimeMillis());

            shiftRepository.insert(shift);
            
            // 插入后刷新数据
            LocalDate shiftDate = LocalDate.parse(shift.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            selectDate(shiftDate);
            loadMonthShifts(YearMonth.from(shiftDate));
        } catch (Exception e) {
            errorMessage.setValue("error_save_shift");
        }
    }

    public LiveData<Shift> getSelectedShift() {
        return selectedShift;
    }

    public LiveData<List<Shift>> getMonthShifts() {
        return monthShifts;
    }

    public void loadMonthShifts(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        String startDate = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        currentMonthShiftsLiveData = shiftRepository.getShiftsBetween(startDate, endDate);
        currentMonthShiftsLiveData.observeForever(monthShifts::setValue);
    }

    public LiveData<List<ShiftTypeEntity>> getAllShiftTypes() {
        return shiftTypeRepository.getAllShiftTypes();
    }

    public LiveData<ShiftTypeEntity> getShiftTypeById(long id) {
        return shiftTypeRepository.getShiftTypeById(id);
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