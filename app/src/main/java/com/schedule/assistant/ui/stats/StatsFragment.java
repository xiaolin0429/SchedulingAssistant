package com.schedule.assistant.ui.stats;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.databinding.FragmentStatsBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import androidx.core.util.Pair;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.schedule.assistant.utils.LocaleHelper;

/**
 * 统计模块的Fragment，用于显示班次统计信息
 * 包括：班次类型分布饼图、班次数量统计、工作时长统计和工作时长柱状图
 */
public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;
    private SimpleDateFormat monthFormat;  // 月份显示格式
    private SimpleDateFormat dateFormat;   // 日期显示格式

    /**
     * 更新日期格式化器
     * 根据当前语言环境设置正确的日期格式
     */
    private void updateDateFormats() {
        String pattern = getString(R.string.month_year_format);
        // 使用 LocaleHelper 获取正确的 Locale
        Locale currentLocale = LocaleHelper.getCurrentLocale(requireContext());
        Log.d(TAG, "Current locale: " + currentLocale.getLanguage() + ", pattern: " + pattern);
        
        monthFormat = new SimpleDateFormat(pattern, currentLocale);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", currentLocale);
        
        // 如果当前显示的月份不为空，则刷新显示
        if (viewModel != null && viewModel.getSelectedMonth().getValue() != null) {
            binding.monthText.setText(monthFormat.format(viewModel.getSelectedMonth().getValue()));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDateFormats();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        updateDateFormats();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        setupChart();
        setupBarChart();
        setupMonthNavigation();
        observeViewModel();

        // 初始化为当前月份
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        viewModel.selectMonth(calendar.getTime());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次恢复时更新日期格式，以确保正确的语言设置
        updateDateFormats();
    }

    /**
     * 设置月份导航和日期选择功能
     * 包括：上一月、下一月按钮，快速选择菜单，日期范围选择器
     */
    private void setupMonthNavigation() {
        binding.previousMonthButton.setOnClickListener(v -> {
            Date currentMonth = viewModel.getSelectedMonth().getValue();
            if (currentMonth != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentMonth);
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                viewModel.selectMonth(calendar.getTime());
            }
        });

        binding.nextMonthButton.setOnClickListener(v -> {
            Date currentMonth = viewModel.getSelectedMonth().getValue();
            if (currentMonth != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentMonth);
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                viewModel.selectMonth(calendar.getTime());
            }
        });

        // 设置快速选择按钮
        binding.quickSelectButton.setOnClickListener(v -> showQuickSelectMenu());

        // 设置日期范围选择按钮
        binding.dateRangeButton.setOnClickListener(v -> showDateRangePicker());

        // 设置清除范围按钮
        binding.clearRangeChip.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            viewModel.selectMonth(calendar.getTime());
        });

        binding.clearRangeChip.setOnCloseIconClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            viewModel.selectMonth(calendar.getTime());
        });
    }

    /**
     * 显示快速选择菜单
     * 提供：本月、上月、近三月的快速选择选项
     */
    private void showQuickSelectMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), binding.quickSelectButton);
        popup.getMenu().add(Menu.NONE, 1, Menu.NONE, R.string.current_month);
        popup.getMenu().add(Menu.NONE, 2, Menu.NONE, R.string.last_month);
        popup.getMenu().add(Menu.NONE, 3, Menu.NONE, R.string.last_three_months);

        popup.setOnMenuItemClickListener(item -> switch (item.getItemId()) {
            case 1 -> {
                viewModel.selectQuickRange(StatsViewModel.QuickRange.CURRENT_MONTH);
                yield true;
            }
            case 2 -> {
                viewModel.selectQuickRange(StatsViewModel.QuickRange.LAST_MONTH);
                yield true;
            }
            case 3 -> {
                viewModel.selectQuickRange(StatsViewModel.QuickRange.LAST_THREE_MONTHS);
                yield true;
            }
            default -> false;
        });

        popup.show();
    }

    /**
     * 显示日期范围选择器
     * 允许用户选择自定义的日期范围进行统计
     */
    private void showDateRangePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText(R.string.date_range_title);
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

        picker.addOnPositiveButtonClickListener(selection -> {
            Date startDate = new Date(selection.first);
            Date endDate = new Date(selection.second);
            viewModel.selectDateRange(startDate, endDate);
        });

        picker.show(getChildFragmentManager(), "date_range_picker");
    }

    /**
     * 观察ViewModel中的数据变化并更新UI
     * 包括：月份显示、日期范围、班次数据、统计信息等
     */
    private void observeViewModel() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> {
            if (month != null) {
                binding.monthText.setText(monthFormat.format(month));
                binding.clearRangeChip.setVisibility(View.GONE);
                Log.d(TAG, "Selected month updated: " + monthFormat.format(month));
            }
        });

        viewModel.getSelectedDateRange().observe(getViewLifecycleOwner(), dateRange -> {
            if (dateRange != null) {
                String startDateStr = dateFormat.format(dateRange.startDate());
                String endDateStr = dateFormat.format(dateRange.endDate());
                binding.monthText.setText(getString(R.string.date_range_format, startDateStr, endDateStr));
                binding.clearRangeChip.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMonthShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null) {
                Log.d(TAG, "Month shifts updated: " + shifts.size() + " shifts found");
                // 根据选择的时间范围类型决定显示的文案
                boolean isDateRange = viewModel.getSelectedDateRange().getValue() != null && 
                    viewModel.getSelectedMonth().getValue() == null;
                binding.totalShiftCount.setText(getString(
                    isDateRange ? R.string.total_shift_count_range : R.string.total_shift_count,
                    shifts.size()
                ));
                // 更新图表和其他统计信息
                updateViewVisibility(!shifts.isEmpty());
                // 更新柱状图
                updateBarChart(shifts);
            } else {
                Log.d(TAG, "Month shifts is null");
                updateViewVisibility(false);
            }
        });

        viewModel.getShiftTypeCounts().observe(getViewLifecycleOwner(), typeCounts -> {
            Log.d(TAG, "Shift type counts updated: " + (typeCounts != null ? typeCounts.size() : 0) + " types");
            updateChart(typeCounts);
        });

        viewModel.getShiftTypePercentages().observe(getViewLifecycleOwner(), percentages -> {
            Log.d(TAG, "Shift type percentages updated: " + (percentages != null ? percentages.size() : 0) + " types");
            updatePercentages(percentages);
        });

        viewModel.getTotalWorkHours().observe(getViewLifecycleOwner(), hours -> {
            Log.d(TAG, "Total work hours updated: " + hours);
            updateTotalWorkHours(hours);
        });

        viewModel.getAverageWorkHours().observe(getViewLifecycleOwner(), hours -> {
            Log.d(TAG, "Average work hours updated: " + hours);
            updateAverageWorkHours(hours);
        });

        viewModel.getWorkHoursRecord().observe(getViewLifecycleOwner(), record -> {
            Log.d(TAG, "Work hours record updated");
            updateWorkHoursRecord(record);
        });
    }

    /**
     * 根据是否有数据更新视图的可见性
     * @param hasData 是否有数据
     */
    private void updateViewVisibility(boolean hasData) {
        binding.pieChart.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.pieChartEmptyView.setVisibility(hasData ? View.GONE : View.VISIBLE);
        binding.statsContainer.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.workHoursContainer.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.legendContainer.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置饼图的基本属性
     * 包括：中心孔、标签、图例等设置
     */
    private void setupChart() {
        PieChart chart = binding.pieChart;
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.getDescription().setEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setEntryLabelTextSize(11f);

        // 禁用默认图例
        chart.getLegend().setEnabled(false);

        // 根据主题设置文字颜色
        int textColor = requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_high_type,
                requireContext().getTheme());
        chart.setEntryLabelColor(textColor);
        chart.setCenterTextColor(textColor);

        // 设置中心孔的颜色为卡片背景色
        chart.setHoleColor(requireContext().getResources().getColor(
                com.google.android.material.R.color.design_default_color_surface,
                requireContext().getTheme()));

        // 设置边距
        chart.setExtraOffsets(8f, 8f, 8f, 8f);
    }

    /**
     * 更新自定义图例
     * @param entries 饼图数据项
     * @param colors 对应的颜色数组
     */
    private void updateCustomLegend(List<PieEntry> entries, int[] colors) {
        LinearLayout legendContainer = binding.legendContainer;
        legendContainer.removeAllViews();

        // 为每个条目创建图例项
        for (int i = 0; i < entries.size(); i++) {
            View legendItem = getLayoutInflater().inflate(R.layout.item_chart_legend, legendContainer, false);

            View colorIndicator = legendItem.findViewById(R.id.legendColorIndicator);
            TextView legendText = legendItem.findViewById(R.id.legendText);

            colorIndicator.setBackgroundColor(colors[i]);
            legendText.setText(entries.get(i).getLabel());

            // 添加间距
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            legendItem.setLayoutParams(params);

            legendContainer.addView(legendItem);
        }
    }

    /**
     * 使用指定的数据和颜色更新饼图
     * @param entries 饼图数据项
     * @param colors 对应的颜色数组
     */
    private void updatePieChartWithColors(List<PieEntry> entries, int[] colors) {
        if (binding == null)
            return;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        // 设置文字和线条
        dataSet.setValueTextSize(11f);
        int textColor = requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_high_type,
                requireContext().getTheme());
        dataSet.setValueTextColor(textColor);
        dataSet.setValueLineColor(textColor);

        // 配置值的显示格式
        dataSet.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        // 设置标签位置和连接线
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setValueLineWidth(1f);
        dataSet.setValueLineColor(textColor);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        binding.pieChart.setData(pieData);
        binding.pieChart.invalidate();

        // 更新自定义图例
        updateCustomLegend(entries, colors);
    }

    /**
     * 获取班次类型的默认颜色
     * @param shiftTypeName 班次类型名称
     * @return 对应的默认颜色
     */
    private int getDefaultColor(String shiftTypeName) {
        // 根据班次类型名称返回默认颜色
        if (getString(R.string.day_shift).equals(shiftTypeName)) {
            return getResources().getColor(R.color.day_shift_color, requireContext().getTheme());
        } else if (getString(R.string.night_shift).equals(shiftTypeName)) {
            return getResources().getColor(R.color.night_shift_color, requireContext().getTheme());
        } else if (getString(R.string.rest_day).equals(shiftTypeName)) {
            return getResources().getColor(R.color.rest_day_color, requireContext().getTheme());
        } else {
            // 为其他类型生成一个固定的颜色
            return getResources().getColor(R.color.default_shift_color, requireContext().getTheme());
        }
    }

    /**
     * 更新班次类型百分比显示
     * @param percentages 班次类型百分比映射
     */
    private void updatePercentages(Map<Long, Double> percentages) {
        if (percentages == null || percentages.isEmpty()) {
            return;
        }

        StringBuilder statsText = new StringBuilder();
        for (Map.Entry<Long, Double> entry : percentages.entrySet()) {
            viewModel.getShiftTypeName(entry.getKey()).observe(getViewLifecycleOwner(), shiftTypeName -> {
                double percentage = entry.getValue();
                statsText.append(getString(R.string.shift_type_percentage, shiftTypeName, percentage));
                statsText.append("\n");
                binding.shiftTypeStats.setText(statsText.toString());
            });
        }
    }

    private void updateTotalWorkHours(Double hours) {
        if (hours != null) {
            binding.totalWorkHours.setText(getString(R.string.total_work_hours, hours));
        }
    }

    private void updateAverageWorkHours(Double hours) {
        if (hours != null) {
            binding.averageWorkHours.setText(getString(R.string.average_work_hours, hours));
        }
    }

    private void updateWorkHoursRecord(StatsViewModel.WorkHoursRecord record) {
        if (record != null) {
            try {
                Date maxDate = dateFormat.parse(record.maxDate());
                Date minDate = dateFormat.parse(record.minDate());
                String maxDateStr = maxDate != null ? dateFormat.format(maxDate) : record.maxDate();
                String minDateStr = minDate != null ? dateFormat.format(minDate) : record.minDate();

                binding.maxWorkHours.setText(getString(R.string.max_work_hours, record.maxHours(), maxDateStr));
                binding.minWorkHours.setText(getString(R.string.min_work_hours, record.minHours(), minDateStr));
            } catch (Exception e) {
                binding.maxWorkHours.setText(getString(R.string.max_work_hours, record.maxHours(), record.maxDate()));
                binding.minWorkHours.setText(getString(R.string.min_work_hours, record.minHours(), record.minDate()));
                Log.e(TAG, "Error formatting work hours record dates", e);
            }
        }
    }

    /**
     * 设置工作时长柱状图的基本属性
     * 包括：坐标轴、图例、缩放等设置
     */
    private void setupBarChart() {
        BarChart chart = binding.workHoursChart;
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);

        // 设置图例
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // 设置X轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_medium,
                requireContext().getTheme()));

        // 设置左Y轴
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_medium,
                requireContext().getTheme()));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f h", value);
            }
        });

        // 禁用右Y轴
        chart.getAxisRight().setEnabled(false);

        // 设置边距
        chart.setExtraOffsets(10f, 10f, 10f, 10f);
    }

    /**
     * 使用班次数据更新柱状图
     * @param shifts 班次列表
     */
    private void updateBarChart(List<Shift> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            binding.workHoursChart.setVisibility(View.GONE);
            return;
        }

        binding.workHoursChart.setVisibility(View.VISIBLE);
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // 按日期排序shifts
        shifts.sort(Comparator.comparing(Shift::getDate));

        for (int i = 0; i < shifts.size(); i++) {
            Shift shift = shifts.get(i);
            float hours = calculateWorkHours(shift.getStartTime(), shift.getEndTime());
            if (hours > 0) {
                entries.add(new BarEntry(i, hours));
                // 只显示日期的天数
                String day = shift.getDate().substring(8);
                labels.add(day);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.work_hours));
        dataSet.setColor(requireContext().getResources().getColor(R.color.colorPrimary, null));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        // 设置X轴标签
        XAxis xAxis = binding.workHoursChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        binding.workHoursChart.setData(barData);
        binding.workHoursChart.invalidate();
    }

    /**
     * 计算工作时长
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 工作时长（小时）
     */
    private float calculateWorkHours(String startTime, String endTime) {
        if (startTime == null || endTime == null ||
                startTime.equals("-") || endTime.equals("-")) {
            return 0f;
        }

        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date start = timeFormat.parse(startTime);
            Date end = timeFormat.parse(endTime);

            if (start == null || end == null) {
                return 0f;
            }

            long diffMillis = end.getTime() - start.getTime();
            if (diffMillis < 0) {
                // 跨天处理
                diffMillis += 24 * 60 * 60 * 1000;
            }

            return diffMillis / (1000f * 60 * 60);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating work hours: " + e.getMessage());
            return 0f;
        }
    }

    /**
     * 更新班次类型分布饼图
     * @param typeCounts 班次类型数量映射
     */
    private void updateChart(Map<Long, Integer> typeCounts) {
        if (typeCounts == null || typeCounts.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.pieChartEmptyView.setVisibility(View.VISIBLE);
            binding.statsContainer.setVisibility(View.GONE);
            binding.workHoursContainer.setVisibility(View.GONE);
            binding.legendContainer.setVisibility(View.GONE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);
        binding.pieChartEmptyView.setVisibility(View.GONE);
        binding.statsContainer.setVisibility(View.VISIBLE);
        binding.workHoursContainer.setVisibility(View.VISIBLE);
        binding.legendContainer.setVisibility(View.VISIBLE);

        // 创建一个有序的列表来存储班次类型ID
        List<Long> typeIds = new ArrayList<>(typeCounts.keySet());
        // 按照ID排序以确保顺序一致性
        typeIds.sort(Long::compareTo);

        // 创建一个计数器来跟踪异步操作
        final int[] completedOperations = { 0 };
        final int totalOperations = typeIds.size();
        final List<PieEntry> entries = new ArrayList<>(totalOperations);
        // 预先分配空间，使用null作为占位符
        for (int i = 0; i < totalOperations; i++) {
            entries.add(null);
        }

        // 为每个班次类型创建对应的颜色数组
        final int[] colors = new int[totalOperations];

        for (int i = 0; i < totalOperations; i++) {
            final int index = i;
            Long typeId = typeIds.get(i);

            // 获取班次类型名称和颜色
            viewModel.getShiftType(typeId).observe(getViewLifecycleOwner(), shiftType -> {
                if (shiftType != null) {
                    Integer count = typeCounts.get(typeId);
                    entries.set(index, new PieEntry(count != null ? count : 0, shiftType.getName()));

                    // 使用班次类型的实际颜色
                    colors[index] = shiftType.getColor() != 0 ? shiftType.getColor()
                            : getDefaultColor(shiftType.getName());

                    completedOperations[0]++;

                    // 当所有异步操作完成时更新图表
                    if (completedOperations[0] == totalOperations) {
                        // 移除所有可能的null条目
                        entries.removeIf(Objects::isNull);
                        updatePieChartWithColors(entries, colors);
                    }
                }
            });
        }
    }

    /**
     * 判断给定的日期范围是否为完整的一个月
     * @param range 日期范围
     * @return 是否为完整月份
     */
    private boolean isMonthRange(StatsViewModel.DateRange range) {
        Calendar start = Calendar.getInstance();
        start.setTime(range.startDate());
        Calendar end = Calendar.getInstance();
        end.setTime(range.endDate());
        
        // 检查是否是同一个月
        return start.get(Calendar.YEAR) == end.get(Calendar.YEAR) &&
               start.get(Calendar.MONTH) == end.get(Calendar.MONTH) &&
               start.get(Calendar.DAY_OF_MONTH) == 1 &&
               end.get(Calendar.DAY_OF_MONTH) == end.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}