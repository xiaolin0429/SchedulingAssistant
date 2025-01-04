package com.schedule.assistant.ui.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentAlarmBinding;
import java.util.Calendar;

public class AlarmFragment extends Fragment {
    private FragmentAlarmBinding binding;
    private AlarmViewModel viewModel;
    private AlarmAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        
        setupRecyclerView();
        setupAddButton();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new AlarmAdapter(
            alarm -> viewModel.toggleAlarm(alarm),
            alarm -> viewModel.deleteAlarm(alarm)
        );
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupAddButton() {
        binding.addAlarmFab.setOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(R.string.select_alarm_time)
            .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            viewModel.addAlarm(hour, minute);
        });

        picker.show(getChildFragmentManager(), "time_picker");
    }

    private void observeViewModel() {
        viewModel.getAlarms().observe(getViewLifecycleOwner(), alarms -> {
            if (alarms != null && !alarms.isEmpty()) {
                adapter.submitList(alarms);
                binding.emptyView.setVisibility(View.GONE);
            } else {
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 