package com.schedule.assistant.ui.stats;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentStatsBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class StatsFragment extends Fragment {
    private static final String TAG = "StatsFragment";
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;
    private SimpleDateFormat monthFormat;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        monthFormat = new SimpleDateFormat(getString(R.string.month_year_format), Locale.getDefault());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        setupChart();
        setupMonthNavigation();
        observeViewModel();

        // 初始化为当前月份
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        viewModel.selectMonth(calendar.getTime());
    }

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
    }

    private void observeViewModel() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> {
            if (month != null) {
                binding.monthText.setText(monthFormat.format(month));
                Log.d(TAG, "Selected month updated: " + monthFormat.format(month));
            }
        });

        viewModel.getMonthShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null) {
                Log.d(TAG, "Month shifts updated: " + shifts.size() + " shifts found");
                // 更新总班次数
                binding.totalShiftCount.setText(getString(R.string.total_shift_count, shifts.size()));
                // 更新图表和其他统计信息
                updateViewVisibility(!shifts.isEmpty());
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

    private void updateViewVisibility(boolean hasData) {
        binding.pieChart.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.emptyView.setVisibility(hasData ? View.GONE : View.VISIBLE);
        binding.statsContainer.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.workHoursContainer.setVisibility(hasData ? View.VISIBLE : View.GONE);
    }

    private void setupChart() {
        PieChart chart = binding.pieChart;
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.getDescription().setEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setEntryLabelTextSize(11f);

        // 根据主题设置文字颜色
        int textColor = requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_high_type,
                requireContext().getTheme());
        chart.setEntryLabelColor(textColor);
        chart.getLegend().setEnabled(false);

        // 设置边距
        chart.setExtraOffsets(25f, 15f, 25f, 15f);
        chart.setMinOffset(20f);
    }

    private void updateChart(Map<Long, Integer> typeCounts) {
        if (typeCounts == null || typeCounts.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
            binding.statsContainer.setVisibility(View.GONE);
            binding.workHoursContainer.setVisibility(View.GONE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);
        binding.statsContainer.setVisibility(View.VISIBLE);
        binding.workHoursContainer.setVisibility(View.VISIBLE);

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
        for (int i = 0; i < totalOperations; i++) {
            final int index = i;
            Long typeId = typeIds.get(i);

            // 获取班次类型名称
            viewModel.getShiftTypeName(typeId).observe(getViewLifecycleOwner(), shiftTypeName -> {
                Integer count = typeCounts.get(typeId);
                entries.set(index, new PieEntry(count != null ? count : 0, shiftTypeName));
                completedOperations[0]++;

                // 当所有异步操作完成时更新图表
                if (completedOperations[0] == totalOperations) {
                    // 移除所有可能的null条目
                    entries.removeIf(Objects::isNull);
                    updatePieChart(entries);
                    // 更新颜色
                    updateChartColors(typeIds);
                }
            });
        }
    }

    private void updateChartColors(List<Long> typeIds) {
        if (typeIds == null || typeIds.isEmpty()) {
            return;
        }

        int[] colors = new int[typeIds.size()];
        final int[] completedOperations = { 0 };
        final int totalOperations = typeIds.size();

        for (int i = 0; i < typeIds.size(); i++) {
            final int index = i;
            Long typeId = typeIds.get(i);
            viewModel.getShiftTypeColor(typeId).observe(getViewLifecycleOwner(), color -> {
                if (color != null && color != 0) {
                    colors[index] = color;
                } else {
                    colors[index] = generateDefaultColor(typeId.intValue());
                }
                completedOperations[0]++;

                if (completedOperations[0] == totalOperations) {
                    updatePieChartColors(colors);
                }
            });
        }
    }

    private void updatePieChart(List<PieEntry> entries) {
        if (binding == null)
            return;

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getChartColors());

        // 设置文字和线条
        dataSet.setValueTextSize(11f);
        int textColor = requireContext().getResources().getColor(
                com.google.android.material.R.color.material_on_surface_emphasis_high_type,
                requireContext().getTheme());
        dataSet.setValueTextColor(textColor);
        dataSet.setValueLineColor(textColor);

        // 调整标签位置和连接线
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.3f);
        dataSet.setValueLinePart2Length(0.3f);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLineWidth(1f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

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

    private void updatePieChartColors(int[] colors) {
        if (binding == null)
            return;

        PieData data = binding.pieChart.getData();
        if (data != null) {
            PieDataSet dataSet = (PieDataSet) data.getDataSet();
            dataSet.setColors(colors);
            binding.pieChart.invalidate();
        }
    }

    private int[] getChartColors() {
        Map<Long, Integer> typeCounts = viewModel.getShiftTypeCounts().getValue();
        if (typeCounts == null || typeCounts.isEmpty()) {
            return new int[0];
        }

        List<Long> typeIds = new ArrayList<>(typeCounts.keySet());
        typeIds.sort(Long::compareTo); // 确保顺序一致性
        int[] colors = new int[typeIds.size()];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = generateDefaultColor(typeIds.get(i).intValue());
        }

        return colors;
    }

    private int generateDefaultColor(int typeId) {
        // 使用默认的颜色数组
        int[] defaultColors = {
                getResources().getColor(R.color.day_shift_color, requireContext().getTheme()),
                getResources().getColor(R.color.night_shift_color, requireContext().getTheme()),
                getResources().getColor(R.color.rest_day_color, requireContext().getTheme()),
                getResources().getColor(R.color.early_shift_color, requireContext().getTheme()),
                getResources().getColor(R.color.late_shift_color, requireContext().getTheme())
        };

        // 如果typeId超出默认颜色数组范围，则使用HSV生成新颜色
        if (typeId >= defaultColors.length) {
            float hue = (typeId * 137.5f) % 360f; // 使用黄金角度137.5度来生成分散的颜色
            float saturation = 0.75f;
            float value = 0.95f;
            return android.graphics.Color.HSVToColor(new float[] { hue, saturation, value });
        }

        return defaultColors[typeId % defaultColors.length];
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}