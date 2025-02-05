package com.schedule.assistant.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.DialogAutoScheduleBinding;
import com.schedule.assistant.viewmodel.AutoScheduleViewModel;
import com.schedule.assistant.data.entity.Shift;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.YearMonth;
import java.time.DayOfWeek;
import com.schedule.assistant.ui.calendar.PreviewCalendarDayBinder;
import com.schedule.assistant.ui.calendar.PreviewMonthHeaderBinder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AutoScheduleDialogFragment extends BottomSheetDialogFragment {
    private static final String TAG = "AutoScheduleDialog";

    private DialogAutoScheduleBinding binding;
    private AutoScheduleViewModel viewModel;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private LocalDate startDate;
    private LocalDate endDate;
    private final int[] weeklyPattern = new int[7]; // 存储每周的排班模式

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: 初始化对话框");
        // 设置对话框主题
        setStyle(STYLE_NORMAL, R.style.Theme_ScheduleAssistant_Dialog);
        viewModel = new ViewModelProvider(this).get(AutoScheduleViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: 创建对话框实例");
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setDismissWithAnimation(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: 开始创建视图");
        try {
            // 使用新的主题创建LayoutInflater
            Context themedContext = new ContextThemeWrapper(requireContext(), R.style.Theme_ScheduleAssistant_Dialog);
            Log.d(TAG, "onCreateView: 创建themed context, theme id: " + R.style.Theme_ScheduleAssistant_Dialog);

            LayoutInflater themedInflater = inflater.cloneInContext(themedContext);
            Log.d(TAG, "onCreateView: 创建themed inflater");

            binding = DialogAutoScheduleBinding.inflate(themedInflater, container, false);
            Log.d(TAG, "onCreateView: 视图绑定成功");

            setupViews();
            observeViewModel();
            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: 创建视图失败", e);
            throw e;
        }
    }

    private void setupViews() {
        Log.d(TAG, "setupViews: 设置视图组件");
        // 设置日期选择器
        binding.startDate.setOnClickListener(v -> showDatePicker(true));
        binding.endDate.setOnClickListener(v -> showDatePicker(false));

        // 设置预览日历
        setupPreviewCalendar();

        // 设置按钮点击事件
        binding.previewButton.setOnClickListener(v -> generatePreview());
        binding.confirmButton.setOnClickListener(v -> generateSchedule());
        binding.cancelButton.setOnClickListener(v -> dismiss());
        Log.d(TAG, "setupViews: 视图组件设置完成");
    }

    private void setupPreviewCalendar() {
        PreviewCalendarDayBinder dayBinder = new PreviewCalendarDayBinder(
                day -> {
                    // 点击日期时的处理逻辑
                },
                viewModel,
                getViewLifecycleOwner());
        dayBinder.setCalendarView(binding.previewCalendar);
        binding.previewCalendar.setDayBinder(dayBinder);

        // 设置月份标题
        binding.previewCalendar.setMonthHeaderBinder(new PreviewMonthHeaderBinder());

        // 观察预览数据变化
        viewModel.getPreviewShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null && !shifts.isEmpty()) {
                Map<String, Shift> shiftMap = new HashMap<>();
                for (Shift shift : shifts) {
                    shiftMap.put(shift.getDate(), shift);
                }
                // 设置日期范围
                dayBinder.setDateRange(startDate, endDate);
                dayBinder.updateShifts(shiftMap);
                binding.previewCard.setVisibility(View.VISIBLE);
                binding.previewCalendar.notifyCalendarChanged();
            }
        });

        // 设置日历显示范围
        if (startDate != null) {
            YearMonth startMonth = YearMonth.from(startDate);
            YearMonth endMonth = endDate != null ? YearMonth.from(endDate) : startMonth.plusMonths(1);
            binding.previewCalendar.setup(startMonth, endMonth, DayOfWeek.MONDAY);
            binding.previewCalendar.scrollToMonth(startMonth);
        } else {
            YearMonth currentMonth = YearMonth.now();
            binding.previewCalendar.setup(
                    currentMonth.minusMonths(1),
                    currentMonth.plusMonths(1),
                    DayOfWeek.MONDAY);
            binding.previewCalendar.scrollToMonth(currentMonth);
        }
    }

    private void observeViewModel() {
        Log.d(TAG, "observeViewModel: 开始观察ViewModel数据");
        // 观察班次类型数据
        viewModel.getShiftTypes().observe(getViewLifecycleOwner(), types -> {
            Log.d(TAG, "观察到班次类型数据变化: " + (types != null ? types.size() : 0) + "个类型");
            if (types != null) {
                for (ShiftTypeEntity type : types) {
                    Log.d(TAG, "班次类型: " + type.getName() + ", ID: " + type.getId());
                }
            }
            setupSpinners(types);
        });

        // 观察错误消息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Log.e(TAG, "错误消息: " + message);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // 观察自动排班结果
        viewModel.getAutoScheduleResult().observe(getViewLifecycleOwner(), success -> {
            Log.d(TAG, "自动排班结果: " + success);
            if (success != null) {
                if (success) {
                    Toast.makeText(requireContext(), R.string.auto_schedule_success, Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), R.string.auto_schedule_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 观察是否存在已有排班
        viewModel.getHasExistingShifts().observe(getViewLifecycleOwner(), hasExistingShifts -> {
            if (hasExistingShifts != null && hasExistingShifts && viewModel.isGeneratingSchedule()) {
                // 显示确认对话框
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.auto_schedule_confirm_title)
                        .setMessage(getString(R.string.auto_schedule_confirm_message,
                                startDate.format(dateFormatter),
                                endDate.format(dateFormatter)))
                        .setPositiveButton(R.string.confirm, (dialog, which) -> viewModel.generateSchedule(weeklyPattern, startDate, endDate))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            } else if (viewModel.isGeneratingSchedule()) {
                // 直接生成排班
                viewModel.generateSchedule(weeklyPattern, startDate, endDate);
            }
        });
    }

    private void setupSpinners(List<ShiftTypeEntity> shiftTypes) {
        Log.d(TAG, "setupSpinners: 设置下拉选择器，类型数量: " + (shiftTypes != null ? shiftTypes.size() : 0));
        if (shiftTypes == null || shiftTypes.isEmpty()) {
            Log.w(TAG, "setupSpinners: 没有可用的班次类型");
            viewModel.setErrorMessage(getString(R.string.no_shift_type_selected));
            return;
        }

        try {
            // 创建自定义适配器
            ArrayAdapter<ShiftTypeEntity> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    shiftTypes) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    ShiftTypeEntity item = getItem(position);
                    if (item != null) {
                        textView.setText(item.getName());
                    }
                    return textView;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                    ShiftTypeEntity item = getItem(position);
                    if (item != null) {
                        textView.setText(item.getName());
                    }
                    return textView;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // 设置所有星期的下拉选择器
            binding.mondaySpinner.setAdapter(adapter);
            binding.tuesdaySpinner.setAdapter(adapter);
            binding.wednesdaySpinner.setAdapter(adapter);
            binding.thursdaySpinner.setAdapter(adapter);
            binding.fridaySpinner.setAdapter(adapter);
            binding.saturdaySpinner.setAdapter(adapter);
            binding.sundaySpinner.setAdapter(adapter);
            Log.d(TAG, "setupSpinners: 下拉选择器设置完成");
        } catch (Exception e) {
            Log.e(TAG, "setupSpinners: 设置下拉选择器失败", e);
            throw e;
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Log.d(TAG, "showDatePicker: 显示日期选择器, isStartDate: " + isStartDate);
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStartDate ? R.string.start_date : R.string.end_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            Log.d(TAG, "选择的日期: " + selectedDate);

            if (isStartDate) {
                startDate = selectedDate;
                binding.startDate.setText(selectedDate.format(dateFormatter));
            } else {
                endDate = selectedDate;
                binding.endDate.setText(selectedDate.format(dateFormatter));
            }

            // 更新预览日历的日期范围
            PreviewCalendarDayBinder dayBinder = (PreviewCalendarDayBinder) binding.previewCalendar.getDayBinder();
            if (dayBinder != null && startDate != null) {
                dayBinder.setDateRange(startDate, endDate);
                // 更新日历显示范围
                YearMonth startMonth = YearMonth.from(startDate);
                YearMonth endMonth = endDate != null ? YearMonth.from(endDate) : startMonth.plusMonths(1);
                binding.previewCalendar.setup(startMonth, endMonth, DayOfWeek.MONDAY);
                binding.previewCalendar.scrollToMonth(startMonth);
            }
        });

        datePicker.show(getChildFragmentManager(), "date_picker");
    }

    private void generatePreview() {
        Log.d(TAG, "generatePreview: 开始生成预览");
        if (validateDates()) {
            Log.w(TAG, "generatePreview: 日期验证失败");
            return;
        }
        // 显示预览卡片
        binding.previewCard.setVisibility(View.VISIBLE);

        // 设置预览日历的日期范围
        PreviewCalendarDayBinder dayBinder = (PreviewCalendarDayBinder) binding.previewCalendar.getDayBinder();
        if (dayBinder != null) {
            dayBinder.setDateRange(startDate, endDate);
        }

        updateWeeklyPattern();
        viewModel.generatePreview(weeklyPattern, startDate, endDate);
    }

    private void generateSchedule() {
        Log.d(TAG, "generateSchedule: 开始生成排班");
        if (validateDates()) {
            Log.w(TAG, "generateSchedule: 日期验证失败");
            return;
        }
        updateWeeklyPattern();

        // 先检查是否存在已有排班
        viewModel.checkExistingShifts(startDate, endDate);
    }

    private boolean validateDates() {
        Log.d(TAG, "validateDates: 验证日期, startDate: " + startDate + ", endDate: " + endDate);
        if (startDate == null || endDate == null) {
            viewModel.setErrorMessage(getString(R.string.invalid_date_range));
            return true;
        }
        if (endDate.isBefore(startDate)) {
            viewModel.setErrorMessage(getString(R.string.error_invalid_time_range));
            return true;
        }
        return false;
    }

    private void updateWeeklyPattern() {
        Log.d(TAG, "updateWeeklyPattern: 更新每周排班模式");
        // 获取每个下拉选择器选中的位置作为班次类型索引
        // 注意：weeklyPattern数组的索引0-6对应星期一到星期日
        weeklyPattern[0] = binding.mondaySpinner.getSelectedItemPosition(); // 星期一
        weeklyPattern[1] = binding.tuesdaySpinner.getSelectedItemPosition(); // 星期二
        weeklyPattern[2] = binding.wednesdaySpinner.getSelectedItemPosition(); // 星期三
        weeklyPattern[3] = binding.thursdaySpinner.getSelectedItemPosition(); // 星期四
        weeklyPattern[4] = binding.fridaySpinner.getSelectedItemPosition(); // 星期五
        weeklyPattern[5] = binding.saturdaySpinner.getSelectedItemPosition(); // 星期六
        weeklyPattern[6] = binding.sundaySpinner.getSelectedItemPosition(); // 星期日
        Log.d(TAG, "周一到周日的排班模式: " + java.util.Arrays.toString(weeklyPattern));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: 销毁视图");
        binding = null;
    }
}