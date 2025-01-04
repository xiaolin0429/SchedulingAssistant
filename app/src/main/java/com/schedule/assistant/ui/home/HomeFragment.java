package com.schedule.assistant.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentHomeBinding;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.ui.calendar.CalendarDayBinder;

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
        CalendarView calendarView = binding.calendarView;
        calendarDayBinder = new CalendarDayBinder(this);
        
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(10);
        YearMonth lastMonth = currentMonth.plusMonths(10);
        calendarView.setup(firstMonth, lastMonth, LocalDate.now().getDayOfWeek());
        calendarView.scrollToMonth(currentMonth);

        calendarView.setDayBinder(calendarDayBinder);

        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthViewContainer>() {
            @NonNull
            @Override
            public MonthViewContainer create(@NonNull View view) {
                return new MonthViewContainer(view);
            }

            @Override
            public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth month) {
                container.textView.setText(month.getYearMonth().format(DateTimeFormatter.ofPattern("yyyy年MM月")));
            }
        });
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
                // TODO: 实现添加备注功能
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
            Map<String, Shift> shiftsMap = new HashMap<>();
            for (Shift shift : shifts) {
                shiftsMap.put(shift.getDate(), shift);
            }
            calendarDayBinder.updateShifts(shiftsMap);
            binding.calendarView.notifyCalendarChanged();
            updateShiftCounts(shifts);
        });
    }

    private void updateShiftCounts(List<Shift> shifts) {
        int dayShiftCount = 0;
        int nightShiftCount = 0;
        int restCount = 0;

        for (Shift shift : shifts) {
            switch (shift.getShiftType()) {
                case DAY_SHIFT:
                    dayShiftCount++;
                    break;
                case NIGHT_SHIFT:
                    nightShiftCount++;
                    break;
                case REST:
                    restCount++;
                    break;
            }
        }

        binding.dayShiftCount.setText(getString(R.string.shift_count_format, 
            getString(R.string.day_shift), dayShiftCount));
        binding.nightShiftCount.setText(getString(R.string.shift_count_format, 
            getString(R.string.night_shift), nightShiftCount));
        binding.restDayCount.setText(getString(R.string.shift_count_format, 
            getString(R.string.rest_day), restCount));
    }

    private void updateMonthDisplay() {
        YearMonth currentMonth = YearMonth.now();
        binding.yearMonthText.setText(currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));
    }

    @Override
    public void onDayClick(CalendarDay day) {
        LocalDate previousDate = selectedDate;  // 保存上一次选中的日期
        
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
        String[] shiftTypes = {
            getString(R.string.day_shift),
            getString(R.string.night_shift),
            getString(R.string.rest_day)
        };
        
        new AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_shift_type)
            .setItems(shiftTypes, (dialog, which) -> {
                ShiftType selectedType;
                switch (which) {
                    case 0:
                        selectedType = ShiftType.DAY_SHIFT;
                        break;
                    case 1:
                        selectedType = ShiftType.NIGHT_SHIFT;
                        break;
                    default:
                        selectedType = ShiftType.REST;
                        break;
                }
                Shift shift = new Shift(selectedDate.format(formatter), selectedType);
                viewModel.insertShift(shift);
            })
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class MonthViewContainer extends ViewContainer {
        public final TextView textView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            this.textView = view.findViewById(R.id.monthText);
        }
    }

    private void updateTodayShiftInfo(Shift shift) {
        TextView todayShiftInfo = binding.todayShiftInfo;
        TextView shiftTitleText = binding.shiftTitleText;  // 需要在布局文件中给标题TextView添加id
        
        // 更新标题
        if (selectedDate != null && selectedDate.equals(LocalDate.now())) {
            shiftTitleText.setText(R.string.today_shift);
        } else {
            shiftTitleText.setText(R.string.selected_day_shift);
        }
        
        // 更新排班信息
        if (shift != null) {
            todayShiftInfo.setText(getString(shift.getShiftType().getNameResId()));
            todayShiftInfo.setBackgroundResource(R.drawable.shift_info_background);
        } else {
            todayShiftInfo.setText(R.string.no_shift_today);
            todayShiftInfo.setBackgroundResource(R.drawable.no_shift_background);
        }
        todayShiftInfo.setVisibility(View.VISIBLE);
    }
} 