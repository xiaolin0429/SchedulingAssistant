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
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.ui.calendar.CalendarDayBinder;
import com.schedule.assistant.ui.calendar.CalendarHeaderBinder;
import com.schedule.assistant.ui.dialog.ShiftDetailDialogFragment;
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

        // 观察月度班次数据
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
        int dayShiftCount = 0;
        int nightShiftCount = 0;
        int restCount = 0;

        for (Shift shift : shifts) {
            switch (shift.getType()) {
                case DAY_SHIFT:
                    dayShiftCount++;
                    break;
                case NIGHT_SHIFT:
                    nightShiftCount++;
                    break;
                case REST_DAY:
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

            // 检查是否已经存在该日期的班次
            viewModel.getSelectedShift().observe(getViewLifecycleOwner(), existingShift -> {
                if (existingShift != null) {
                    // 如果已存在班次，显示确认对话框
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.shift_exists_title)
                        .setMessage(R.string.shift_exists_message)
                        .setPositiveButton(R.string.ok, (dialog, which) -> {
                            // 直接使用已获取的班次类型列表，不再重新获取
                            showShiftTypeSelectionDialog(shiftTypes);
                            // 移除观察者，避免重复触发
                            viewModel.getSelectedShift().removeObservers(getViewLifecycleOwner());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                } else {
                    // 如果不存在班次，直接显示选择对话框
                    showShiftTypeSelectionDialog(shiftTypes);
                }
                // 移除观察者，避免重复触发
                viewModel.getSelectedShift().removeObservers(getViewLifecycleOwner());
            });
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
            })
            .setNegativeButton(R.string.cancel, null)
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