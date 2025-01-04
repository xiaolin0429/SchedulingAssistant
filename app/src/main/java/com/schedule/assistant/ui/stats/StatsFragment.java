package com.schedule.assistant.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
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
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.databinding.FragmentStatsBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {
    private FragmentStatsBinding binding;
    private StatsViewModel viewModel;
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
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
                viewModel.selectMonth(calendar.getTime());
            }
        });

        binding.nextMonthButton.setOnClickListener(v -> {
            Date currentMonth = viewModel.getSelectedMonth().getValue();
            if (currentMonth != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(currentMonth);
                calendar.add(Calendar.MONTH, 1);
                viewModel.selectMonth(calendar.getTime());
            }
        });
    }

    private void observeViewModel() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), month -> {
            if (month != null) {
                binding.monthText.setText(monthFormat.format(month));
            }
        });

        viewModel.getShiftTypeCounts().observe(getViewLifecycleOwner(), this::updateChart);
    }

    private void setupChart() {
        PieChart chart = binding.pieChart;
        chart.setDrawHoleEnabled(true);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.getDescription().setEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setEntryLabelTextSize(12f);
        chart.setEntryLabelColor(Color.BLACK);
        chart.getLegend().setEnabled(false);
    }

    private void updateChart(Map<ShiftType, Integer> typeCounts) {
        if (typeCounts == null || typeCounts.isEmpty()) {
            binding.pieChart.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
            return;
        }

        binding.pieChart.setVisibility(View.VISIBLE);
        binding.emptyView.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<ShiftType, Integer> entry : typeCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), getString(entry.getKey().getNameResId())));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getChartColors());
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.4f);
        dataSet.setValueLinePart2Length(0.4f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

    private int[] getChartColors() {
        return new int[]{
            getResources().getColor(R.color.day_shift_color, null),
            getResources().getColor(R.color.night_shift_color, null),
            getResources().getColor(R.color.rest_day_color, null)
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 