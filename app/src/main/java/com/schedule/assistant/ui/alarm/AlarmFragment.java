package com.schedule.assistant.ui.alarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.databinding.FragmentAlarmBinding;
import com.schedule.assistant.ui.adapter.AlarmAdapter;
import com.schedule.assistant.viewmodel.AlarmViewModel;

/**
 * 闹钟列表界面
 */
public class AlarmFragment extends Fragment implements AlarmAdapter.OnAlarmActionListener {
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
        checkNotificationPermission();
        setupRecyclerView();
        binding.fabAddAlarm.setOnClickListener(v -> showTimePicker());
        observeViewModel();
        setupMenu();

        // 检查并更新过期的闹钟
        viewModel.checkAndUpdateExpiredAlarms();
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_alarm, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_disable_all) {
                    showDisableAllConfirmDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showDisableAllConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.disable_all_alarms_title)
            .setMessage(R.string.disable_all_alarms_message)
            .setPositiveButton(R.string.alarm_confirm, (dialog, which) -> viewModel.disableAllAlarms())
            .setNegativeButton(R.string.alarm_cancel, null)
            .show();
    }

    private void setupRecyclerView() {
        adapter = new AlarmAdapter(this);
        binding.alarmList.setAdapter(adapter);
        binding.alarmList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeViewModel() {
        viewModel.getAllAlarms().observe(getViewLifecycleOwner(), alarms -> {
            adapter.submitList(alarms);
            binding.emptyView.setVisibility(alarms == null || alarms.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(R.string.select_alarm_time)
            .build();

        picker.addOnPositiveButtonClickListener(v -> viewModel.requestAlarmPermissions(requireActivity(), () -> 
            viewModel.createAlarm(
                requireActivity(),
                picker.getHour(),
                picker.getMinute(),
                null,  // 默认名称
                false, // 默认不重复
                0,     // 默认重复日期
                null,  // 默认铃声
                true   // 默认震动
            )
        ));

        picker.show(getChildFragmentManager(), "time_picker");
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), 
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;

    @Override
    public void onAlarmClick(AlarmEntity alarm) {
        // TODO: 打开闹钟编辑界面
    }

    @Override
    public void onAlarmToggle(AlarmEntity alarm, boolean enabled) {
        viewModel.toggleAlarm(requireActivity(), alarm.getId(), enabled);
    }

    @Override
    public void onAlarmDelete(AlarmEntity alarm) {
        viewModel.deleteAlarm(alarm);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 