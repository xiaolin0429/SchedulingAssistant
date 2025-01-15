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
import android.content.res.Configuration;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.navigation.Navigation;

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
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.schedule.assistant.utils.LocaleHelper;

public class HomeFragment extends Fragment implements CalendarDayBinder.OnDayClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private LocalDate selectedDate;
    private SimpleDateFormat yearMonthFormat; // 添加日期格式化器
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private CalendarDayBinder calendarDayBinder;

    /**
     * 更新日期格式化器
     * 根据当前语言环境设置正确的日期格式
     */
    private void updateDateFormats() {
        String pattern = getString(R.string.date_format_year_month);
        // 使用 LocaleHelper 获取正确的 Locale
        Locale currentLocale = LocaleHelper.getCurrentLocale(requireContext());
        yearMonthFormat = new SimpleDateFormat(pattern, currentLocale);

        // 如果当前显示的月份不为空，则刷新显示
        if (selectedDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, selectedDate.getYear());
            calendar.set(Calendar.MONTH, selectedDate.getMonthValue() - 1);
            binding.yearMonthText.setText(yearMonthFormat.format(calendar.getTime()));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDateFormats();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        updateDateFormats();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复时更新日期格式，以确保正确的语言设置
        updateDateFormats();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupCalendarView();
        setupButtons();
        observeViewModel();
    }

    private void setupCalendarView() {
        // 使用正确的构造函数参数
        calendarDayBinder = new CalendarDayBinder(this, viewModel, getViewLifecycleOwner());
        binding.calendarView.setDayBinder(calendarDayBinder);
        // 设置CalendarView对象
        calendarDayBinder.setCalendarView(binding.calendarView);
        binding.calendarView.setMonthHeaderBinder(new CalendarHeaderBinder(requireContext()));

        // 设置日历范围
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(6);
        YearMonth lastMonth = currentMonth.plusMonths(6);
        binding.calendarView.setup(firstMonth, lastMonth, DayOfWeek.SUNDAY);
        binding.calendarView.scrollToMonth(currentMonth);

        // 添加月份切换监听器
        binding.calendarView.setMonthScrollListener(calendarMonth -> {
            if (binding != null) {
                // 转换 CalendarMonth 到 YearMonth
                YearMonth month = YearMonth.of(calendarMonth.getYear(), calendarMonth.getMonth());

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, month.getYear());
                calendar.set(Calendar.MONTH, month.getMonthValue() - 1);
                binding.yearMonthText.setText(yearMonthFormat.format(calendar.getTime()));

                // 加载新月份的数据
                viewModel.loadMonthShifts(month);
            }
            return null;
        });

        // 初始化时加载当前月份的数据
        viewModel.loadMonthShifts(currentMonth);

        // 初始化年月显示
        if (binding != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, currentMonth.getYear());
            calendar.set(Calendar.MONTH, currentMonth.getMonthValue() - 1);
            binding.yearMonthText.setText(yearMonthFormat.format(calendar.getTime()));
        }
    }

    private void setupButtons() {
        // 设置顶部功能按钮
        binding.calendarViewButton.setOnClickListener(v -> {
            // 切换到日历视图
            updateViewButtonsState();
        });

        binding.cloudSyncButton.setOnClickListener(v -> {
            // TODO: 实现云同步功能
        });

        binding.settingsButton.setOnClickListener(v -> {
            // 导航到设置页面
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_settingsFragment);
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
        viewModel.getSelectedShift().observe(getViewLifecycleOwner(), shift -> {
            updateTodayShiftInfo(shift);
            // 确保日历视图也更新
            if (selectedDate != null) {
                binding.calendarView.notifyDateChanged(selectedDate);
            }
        });

        // 观察月度排班信息
        viewModel.getMonthShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null) {
                Map<String, Shift> shiftsMap = new HashMap<>();
                for (Shift shift : shifts) {
                    shiftsMap.put(shift.getDate(), shift);
                }
                calendarDayBinder.updateShifts(shiftsMap);
                // 强制刷新整个日历视图
                binding.calendarView.notifyCalendarChanged();
                // 如果有选中日期，确保它被正确高亮
                if (selectedDate != null) {
                    binding.calendarView.notifyDateChanged(selectedDate);
                }
                updateShiftCounts(shifts);

                // 更新年月显示
                if (selectedDate != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, selectedDate.getYear());
                    calendar.set(Calendar.MONTH, selectedDate.getMonthValue() - 1);
                    binding.yearMonthText.setText(yearMonthFormat.format(calendar.getTime()));
                }
            }
        });

        // 观察错误消息
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDayClick(CalendarDay day) {
        LocalDate previousDate = selectedDate;
        selectedDate = day.getDate();

        // 更新ViewModel中的选中日期
        viewModel.selectDate(selectedDate);

        // 更新日历视图的选中状态
        calendarDayBinder.setSelectedDate(selectedDate);

        // 刷新相关日期的显示
        if (previousDate != null) {
            binding.calendarView.notifyDateChanged(previousDate);
        }
        binding.calendarView.notifyDateChanged(selectedDate);

        // 强制刷新当前月份数据
        viewModel.loadMonthShifts(YearMonth.from(selectedDate));
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
            binding.shiftInfoLayout.setVisibility(View.VISIBLE);
            binding.noShiftText.setVisibility(View.GONE);

            // 如果是自定义班次类型，从数据库获取名称
            if (shift.getType() == ShiftType.CUSTOM) {
                viewModel.getShiftTypeById(shift.getShiftTypeId()).observe(getViewLifecycleOwner(), shiftType -> {
                    if (shiftType != null) {
                        binding.shiftTypeText.setText(shiftType.getName());
                        // 确保日历视图也更新
                        if (selectedDate != null) {
                            binding.calendarView.notifyDateChanged(selectedDate);
                        }
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
        } else {
            binding.shiftInfoLayout.setVisibility(View.GONE);
            binding.noShiftText.setVisibility(View.VISIBLE);
        }
    }

    // 更新视图按钮状态
    private void updateViewButtonsState() {
        // 设置日历视图按钮高亮
        binding.calendarViewButton.setAlpha(1.0f);
    }
}