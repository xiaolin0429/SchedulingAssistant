package com.schedule.assistant.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
    private static final SimpleDateFormat MONTH_FORMATTER = new SimpleDateFormat("yyyy年M月", Locale.CHINESE);

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
        setupMonthSelector();
        observeViewModel();
        
        // Load current month by default
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        viewModel.selectMonth(calendar.getTime());
    }

    private void setupChart() {
        PieChart chart = binding.pieChart;
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setMaxAngle(360f);
        chart.setRotationAngle(0);
        chart.setDrawEntryLabels(true);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void setupMonthSelector() {
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
                binding.monthText.setText(MONTH_FORMATTER.format(month));
            }
        });

        viewModel.getShiftTypeCounts().observe(getViewLifecycleOwner(), this::updateChart);
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
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<ShiftType, Integer> entry : typeCounts.entrySet()) {
            ShiftType type = entry.getKey();
            int count = entry.getValue();
            entries.add(new PieEntry(count, getString(type.getNameResId())));
            colors.add(ContextCompat.getColor(requireContext(), type.getColorResId()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 