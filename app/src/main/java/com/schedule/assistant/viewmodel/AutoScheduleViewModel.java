package com.schedule.assistant.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.data.repository.ShiftTypeRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AutoScheduleViewModel extends AndroidViewModel {
    private static final String TAG = "AutoScheduleViewModel";
    private final ShiftRepository shiftRepository;
    private final ShiftTypeRepository shiftTypeRepository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> autoScheduleResult = new MutableLiveData<>();
    private final MutableLiveData<List<Shift>> previewShifts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasExistingShifts = new MutableLiveData<>();
    private androidx.lifecycle.Observer<List<Shift>> shiftsObserver;
    private boolean isGeneratingSchedule = false;
    private LiveData<List<Shift>> currentShiftsLiveData;

    public boolean isGeneratingSchedule() {
        return !isGeneratingSchedule;
    }

    public AutoScheduleViewModel(Application application) {
        super(application);
        shiftRepository = new ShiftRepository(application);
        shiftTypeRepository = new ShiftTypeRepository(application);
    }

    public LiveData<List<ShiftTypeEntity>> getShiftTypes() {
        return shiftTypeRepository.getAllShiftTypes();
    }

    public LiveData<ShiftTypeEntity> getShiftTypeById(long id) {
        return shiftTypeRepository.getShiftTypeById(id);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        errorMessage.setValue(message);
    }

    public LiveData<Boolean> getAutoScheduleResult() {
        return autoScheduleResult;
    }

    public LiveData<List<Shift>> getPreviewShifts() {
        return previewShifts;
    }

    public LiveData<Boolean> getHasExistingShifts() {
        return hasExistingShifts;
    }

    private void removeCurrentObserver() {
        if (shiftsObserver != null && currentShiftsLiveData != null) {
            currentShiftsLiveData.removeObserver(shiftsObserver);
            shiftsObserver = null;
            currentShiftsLiveData = null;
        }
    }

    public void checkExistingShifts(LocalDate startDate, LocalDate endDate) {
        if (isGeneratingSchedule) {
            Log.d(TAG, "checkExistingShifts: 正在生成排班，跳过检查");
            return;
        }
        
        Log.d(TAG, "checkExistingShifts: 检查日期范围内是否存在排班");
        String startDateStr = startDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        
        // 移除之前的观察者
        removeCurrentObserver();
        
        // 获取新的 LiveData
        currentShiftsLiveData = shiftRepository.getShiftsBetween(startDateStr, endDateStr);
        
        // 创建新的观察者
        shiftsObserver = shifts -> {
            boolean hasShifts = shifts != null && !shifts.isEmpty();
            Log.d(TAG, "checkExistingShifts: 日期范围内" + (hasShifts ? "存在" : "不存在") + "排班");
            hasExistingShifts.postValue(hasShifts);
        };
        
        // 注册观察者
        currentShiftsLiveData.observeForever(shiftsObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        isGeneratingSchedule = false;
        removeCurrentObserver();
    }

    public void initializeDefaultShiftTypes() {
        Log.d(TAG, "initializeDefaultShiftTypes: 开始初始化默认班次类型");
        shiftTypeRepository.initializeDefaultShiftTypes();
    }

    public void generatePreview(int[] weeklyPattern, LocalDate startDate, LocalDate endDate) {
        Log.d(TAG, "generatePreview: 开始生成预览");
        shiftTypeRepository.getAllTypesSync(types -> {
            Log.d(TAG, "generatePreview: 获取到的班次类型: " + (types != null ? types.size() : 0) + "个");
            if (types != null) {
                for (ShiftTypeEntity type : types) {
                    Log.d(TAG, "班次类型: " + type.getName() + ", ID: " + type.getId());
                }
            }
            
            if (types == null || types.isEmpty()) {
                Log.e(TAG, "generatePreview: 班次类型为空");
                errorMessage.postValue(getApplication().getString(R.string.error_load_shift_types));
                return;
            }

            List<Shift> shifts = generateShifts(weeklyPattern, types, startDate, endDate);
            Log.d(TAG, "generatePreview: 生成的排班数量: " + shifts.size());
            previewShifts.postValue(shifts);
        });
    }

    public void generateSchedule(int[] weeklyPattern, LocalDate startDate, LocalDate endDate) {
        Log.d(TAG, "generateSchedule: 开始生成排班");
        isGeneratingSchedule = true;
        
        // 在生成排班前移除观察者
        removeCurrentObserver();
        
        shiftTypeRepository.getAllTypesSync(types -> {
            Log.d(TAG, "generateSchedule: 获取到的班次类型: " + (types != null ? types.size() : 0) + "个");
            if (types != null) {
                for (ShiftTypeEntity type : types) {
                    Log.d(TAG, "班次类型: " + type.getName() + ", ID: " + type.getId());
                }
            }

            if (types == null || types.isEmpty()) {
                Log.e(TAG, "generateSchedule: 班次类型为空");
                errorMessage.postValue(getApplication().getString(R.string.error_load_shift_types));
                isGeneratingSchedule = false;
                return;
            }

            List<Shift> shifts = generateShifts(weeklyPattern, types, startDate, endDate);
            Log.d(TAG, "generateSchedule: 生成的排班数量: " + shifts.size());
            
            try {
                Log.d(TAG, "generateSchedule: 开始删除现有排班");
                shiftRepository.deleteShiftsBetween(startDate, endDate);
                
                Log.d(TAG, "generateSchedule: 开始插入新排班");
                shiftRepository.insertAll(shifts, new ShiftRepository.RepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "generateSchedule: 排班生成成功");
                        autoScheduleResult.postValue(true);
                        isGeneratingSchedule = false;
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "generateSchedule: 排班生成失败: " + error);
                        errorMessage.postValue(getApplication().getString(R.string.auto_schedule_failed));
                        autoScheduleResult.postValue(false);
                        isGeneratingSchedule = false;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "generateSchedule: 排班生成异常", e);
                errorMessage.postValue(getApplication().getString(R.string.auto_schedule_failed));
                autoScheduleResult.postValue(false);
                isGeneratingSchedule = false;
            }
        });
    }

    private List<Shift> generateShifts(int[] weeklyPattern, List<ShiftTypeEntity> types, 
                                     LocalDate startDate, LocalDate endDate) {
        List<Shift> shifts = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 获取当前日期是星期几（1-7，其中1代表星期一）
            int dayOfWeek = currentDate.getDayOfWeek().getValue() - 1;
            
            // 获取当天应该排的班次类型索引
            int typeIndex = weeklyPattern[dayOfWeek];
            
            // 如果typeIndex >= 0，说明需要排班
            if (typeIndex >= 0 && typeIndex < types.size()) {
                ShiftTypeEntity type = types.get(typeIndex);
                try {
                    // 获取班次类型，优先使用type.getType()，如果为空则尝试从名称转换
                    ShiftType shiftType = type.getType();
                    if (shiftType == null) {
                        try {
                            // 尝试将名称转换为枚举值
                            shiftType = ShiftType.valueOf(type.getName().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            // 如果是用户自定义的班次类型，使用CUSTOM
                            Log.d(TAG, "使用自定义班次类型: " + type.getName());
                            shiftType = ShiftType.CUSTOM;
                        }
                    }
                    
                    Shift shift = new Shift(
                        currentDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                        shiftType,
                        type.getStartTime(),
                        type.getEndTime()
                    );
                    shift.setShiftTypeId(type.getId());
                    shifts.add(shift);
                } catch (Exception e) {
                    Log.e(TAG, "创建班次时发生错误: " + e.getMessage());
                    errorMessage.postValue(getApplication().getString(R.string.error_create_shift));
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }

        return shifts;
    }
} 