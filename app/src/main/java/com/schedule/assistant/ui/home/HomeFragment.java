package com.schedule.assistant.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.res.ResourcesCompat;

import com.kizitonwose.calendarview.model.CalendarDay;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentHomeBinding;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.ui.calendar.CalendarDayBinder;
import com.schedule.assistant.ui.calendar.CalendarHeaderBinder;
import com.schedule.assistant.ui.dialog.NoteDialogFragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements CalendarDayBinder.OnDayClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private LocalDate selectedDate;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private CalendarDayBinder calendarDayBinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupCalendarView();
        setupButtons();
        observeViewModel();
        updateMonthDisplay();

        // 初始化时加载当天的排班信息
        LocalDate today = LocalDate.now();
        selectedDate = today;
        viewModel.selectDate(today);
        calendarDayBinder.setSelectedDate(today);
        binding.calendarView.notifyDateChanged(today);
    }

    private void setupCalendarView() {
        calendarDayBinder = new CalendarDayBinder(day -> {
            LocalDate date = day.getDate();
            selectedDate = date;
            viewModel.selectDate(date);
            calendarDayBinder.setSelectedDate(date);
            binding.calendarView.notifyDateChanged(date);
        }, viewModel, getViewLifecycleOwner());

        calendarDayBinder.setCalendarView(binding.calendarView);
        binding.calendarView.setDayBinder(calendarDayBinder);
        binding.calendarView.setMonthHeaderBinder(new CalendarHeaderBinder());

        // 设置日历范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(6);
        YearMonth lastMonth = currentMonth.plusMonths(6);
        binding.calendarView.setup(firstMonth, lastMonth, DayOfWeek.MONDAY);
        binding.calendarView.scrollToMonth(currentMonth);

        // 初始化时加载当前月份的数据
        viewModel.loadMonthShifts(currentMonth);
    }

    private void setupButtons() {
        // 设置顶部功能按钮
        binding.gridViewButton.setOnClickListener(v -> {
            // TODO: 实现网格视图切换
        });

        binding.listViewButton.setOnClickListener(v -> {
            // TODO: 实现列表视图切换
        });

        binding.cloudSyncButton.setOnClickListener(v -> {
            // TODO: 实现云同步功能
        });

        binding.settingsButton.setOnClickListener(v -> {
            // TODO: 实现设置功能
        });

        // 设置底部功能按钮
        binding.addNoteButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                Shift currentShift = viewModel.getSelectedShift().getValue();
                String currentNote = currentShift != null ? currentShift.getNote() : null;
                NoteDialogFragment dialog = NoteDialogFragment.newInstance(selectedDate, currentNote);
                dialog.show(getChildFragmentManager(), "add_note");
            } else {
                Toast.makeText(requireContext(), R.string.please_select_date, Toast.LENGTH_SHORT).show();
            }
        });

        binding.startScheduleButton.setOnClickListener(v -> {
            if (selectedDate != null) {
                showShiftTypeDialog();
            } else {
                Toast.makeText(requireContext(), R.string.please_select_date, Toast.LENGTH_SHORT).show();
            }
        });

        binding.nextShiftButton.setOnClickListener(v -> {
            // TODO: 实现第二班次功能
        });
    }

    private void observeViewModel() {
        // 观察选中日期的排班信息
        viewModel.getSelectedShift().observe(getViewLifecycleOwner(), this::updateTodayShiftInfo);

        // 观察月度排班信息
        viewModel.getMonthShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null) {
                Map<String, Shift> shiftsMap = new HashMap<>();
                for (Shift shift : shifts) {
                    shiftsMap.put(shift.getDate(), shift);
                }
                calendarDayBinder.updateShifts(shiftsMap);
                binding.calendarView.notifyCalendarChanged();
                updateShiftCounts(shifts);
            }
        });
    }

    private void updateShiftCounts(List<Shift> shifts) {
        if (shifts == null)
            return;

        // 使用Map统计每种班次类型的数量
        Map<Long, Integer> typeCountMap = new HashMap<>();

        // 统计每种班次类型的数量
        for (Shift shift : shifts) {
            if (shift.getType() == ShiftType.CUSTOM) {
                long typeId = shift.getShiftTypeId();
                typeCountMap.merge(typeId, 1, Integer::sum);
            }
        }

        // 获取所有班次类型并更新统计显示
        viewModel.getAllShiftTypes().observe(getViewLifecycleOwner(), shiftTypes -> {
            if (shiftTypes != null) {
                // 获取统计容器
                LinearLayout container = binding.shiftCountContainer;
                // 清空容器
                container.removeAllViews();

                // 为每个班次类型创建并添加统计显示
                for (ShiftTypeEntity shiftType : shiftTypes) {
                    Integer count = typeCountMap.get(shiftType.getId());
                    if (count == null)
                        count = 0;

                    // 创建新的TextView
                    TextView countView = new TextView(requireContext());
                    countView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    // 设置外边距
                    if (container.getChildCount() > 0) {
                        ((LinearLayout.LayoutParams) countView.getLayoutParams())
                                .setMarginStart(getResources().getDimensionPixelSize(R.dimen.spacing_medium));
                    }

                    // 设置文本
                    countView.setText(getString(R.string.shift_count_format, shiftType.getName(), count));
                    countView.setTextSize(14);

                    // 使用try-with-resources处理TypedArray
                    int[] attrs = new int[] { android.R.attr.textColorPrimary };
                    try (android.content.res.TypedArray ta = requireContext().obtainStyledAttributes(attrs)) {
                        countView.setTextColor(ta.getColor(0, getResources().getColor(R.color.black, null)));
                    }

                    // 使用ResourcesCompat.getDrawable
                    Drawable circle = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_circle_green, null);
                    if (circle != null) {
                        circle.setTint(shiftType.getColor()); // 使用班次类型的颜色
                        countView.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
                        countView.setCompoundDrawablePadding(
                                getResources().getDimensionPixelSize(R.dimen.spacing_small));
                    }

                    // 设置垂直居中
                    countView.setGravity(Gravity.CENTER_VERTICAL);

                    // 添加到容器
                    container.addView(countView);
                }
            }
        });
    }

    private void updateMonthDisplay() {
        YearMonth currentMonth = YearMonth.now();
        binding.yearMonthText.setText(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
    }

    @Override
    public void onDayClick(CalendarDay day) {
        LocalDate previousDate = selectedDate; // 保存上一次选中的日期

        if (day.getDate().equals(selectedDate)) {
            selectedDate = null;
            updateTodayShiftInfo(null);
        } else {
            selectedDate = day.getDate();
            viewModel.selectDate(selectedDate);
        }

        calendarDayBinder.setSelectedDate(selectedDate);

        // 刷新上一次选中的日期显示
        if (previousDate != null) {
            binding.calendarView.notifyDateChanged(previousDate);
        }
        // 刷新当前点击的日期显示
        binding.calendarView.notifyDateChanged(day.getDate());
    }

    private void showShiftTypeDialog() {
        if (selectedDate == null) {
            Toast.makeText(requireContext(), R.string.please_select_date, Toast.LENGTH_SHORT).show();
            return;
        }

        // 从ViewModel获取所有班次类型
        viewModel.getAllShiftTypes().observe(getViewLifecycleOwner(), shiftTypes -> {
            if (shiftTypes == null || shiftTypes.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_shift_types_available, Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建一次性观察者检查是否存在班次
            androidx.lifecycle.Observer<Shift> existingShiftObserver = new androidx.lifecycle.Observer<>() {
                @Override
                public void onChanged(Shift existingShift) {
                    if (existingShift != null) {
                        // 如果已存在班次，显示确认对话框
                        new AlertDialog.Builder(requireContext())
                                .setTitle(R.string.error)
                                .setMessage(R.string.error_duplicate_shift)
                                .setPositiveButton(R.string.confirm,
                                        (dialog, which) -> showShiftTypeSelectionDialog(shiftTypes))
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    } else {
                        // 如果不存在班次，直接显示选择对话框
                        showShiftTypeSelectionDialog(shiftTypes);
                    }
                    // 只移除这个临时观察者
                    viewModel.getSelectedShift().removeObserver(this);
                }
            };

            // 添加临时观察者
            viewModel.getSelectedShift().observe(getViewLifecycleOwner(), existingShiftObserver);
        });
    }

    private void showShiftTypeSelectionDialog(List<ShiftTypeEntity> shiftTypes) {
        String[] shiftTypeNames = new String[shiftTypes.size()];
        for (int i = 0; i < shiftTypes.size(); i++) {
            shiftTypeNames[i] = shiftTypes.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_shift_type)
                .setItems(shiftTypeNames, (dialog, which) -> {
                    ShiftTypeEntity selectedType = shiftTypes.get(which);
                    Shift shift = new Shift(selectedDate.format(formatter), ShiftType.CUSTOM);
                    shift.setShiftTypeId(selectedType.getId());
                    shift.setStartTime(selectedType.getStartTime());
                    shift.setEndTime(selectedType.getEndTime());
                    viewModel.insertShift(shift);

                    // 刷新日历显示
                    binding.calendarView.notifyCalendarChanged();
                    // 刷新月度数据
                    viewModel.loadMonthShifts(YearMonth.from(selectedDate));
                    // 更新选中日期的排班信息
                    viewModel.selectDate(selectedDate);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateTodayShiftInfo(Shift shift) {
        if (shift != null) {
            // 如果是自定义班次类型，从数据库获取名称
            if (shift.getType() == ShiftType.CUSTOM) {
                viewModel.getShiftTypeById(shift.getShiftTypeId()).observe(getViewLifecycleOwner(), shiftType -> {
                    if (shiftType != null) {
                        binding.shiftTypeText.setText(shiftType.getName());
                    } else {
                        binding.shiftTypeText.setText(getString(shift.getType().getNameResId()));
                    }
                });
            } else {
                binding.shiftTypeText.setText(getString(shift.getType().getNameResId()));
            }
            binding.shiftTimeText.setText(String.format("%s - %s",
                    shift.getStartTime(), shift.getEndTime()));
            binding.shiftNoteText.setText(shift.getNote());
            binding.shiftInfoLayout.setVisibility(View.VISIBLE);
            binding.noShiftText.setVisibility(View.GONE);
        } else {
            binding.shiftInfoLayout.setVisibility(View.GONE);
            binding.noShiftText.setVisibility(View.VISIBLE);
        }
    }
}