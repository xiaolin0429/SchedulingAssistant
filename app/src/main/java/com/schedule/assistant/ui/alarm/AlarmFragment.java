package com.schedule.assistant.ui.alarm;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.databinding.FragmentAlarmBinding;
import com.schedule.assistant.ui.adapter.AlarmAdapter;
import com.schedule.assistant.viewmodel.AlarmViewModel;

import java.util.Calendar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

/**
 * 闹钟列表界面
 * 负责闹钟的展示、添加、编辑和删除
 */
public class AlarmFragment extends Fragment implements AlarmAdapter.OnAlarmActionListener {
    private FragmentAlarmBinding binding;
    private AlarmViewModel viewModel;
    private AlarmAdapter adapter;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1;
    private AlarmEntity currentEditingAlarm;
    private TextView currentSoundText;

    // 注册铃声选择结果回调
    private final ActivityResultLauncher<Intent> soundPickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null && currentEditingAlarm != null && currentSoundText != null) {
                    currentEditingAlarm.setSoundUri(uri.toString());
                    currentSoundText.setText(getSoundName(uri.toString()));
                }
            }
        }
    );

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
        setupClickListeners();
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

    /**
     * 显示时间选择器
     * @param alarm 要编辑的闹钟，如果是新建则为null
     */
    private void showTimePicker(@Nullable AlarmEntity alarm) {
        Calendar calendar = Calendar.getInstance();
        if (alarm != null) {
            calendar.setTimeInMillis(alarm.getTimeInMillis());
        }

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(alarm == null ? R.string.add_alarm : R.string.edit_alarm)
            .build();

        picker.addOnPositiveButtonClickListener(v -> {
            calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
            calendar.set(Calendar.MINUTE, picker.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 如果设置的时间早于当前时间，设置为第二天
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (alarm == null) {
                // 创建新闹钟
                viewModel.requestAlarmPermissions(requireActivity(), () -> 
                    viewModel.createAlarm(
                        requireActivity(),
                        picker.getHour(),
                        picker.getMinute(),
                        getString(R.string.default_alarm_name),
                        false,
                        0,
                        null,
                        true
                    )
                );
            } else {
                // 更新现有闹钟
                alarm.setTimeInMillis(calendar.getTimeInMillis());
                viewModel.updateAlarm(requireActivity(), alarm);
            }
        });

        picker.show(getChildFragmentManager(), "time_picker");
    }

    private void setupClickListeners() {
        // 添加闹钟按钮点击事件
        binding.fabAddAlarm.setOnClickListener(v -> {
            adapter.closeOpenedItem(); // 关闭已打开的按钮
            showTimePicker(null);
        });

        // 列表滚动时关闭打开的按钮
        binding.alarmList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    adapter.closeOpenedItem();
                }
            }
        });

        // 设置RecyclerView的触摸和点击事件监听
        binding.alarmList.setOnClickListener(v -> adapter.closeOpenedItem());
        
        // 重写RecyclerView的performClick方法
        binding.alarmList.setOnClickListener(v -> adapter.closeOpenedItem());
        binding.alarmList.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                // 检查点击是否在任何item上
                View child = binding.alarmList.findChildViewUnder(event.getX(), event.getY());
                if (child == null) {
                    // 点击在空白区域
                    adapter.closeOpenedItem();
                }
            }
            v.performClick();
            return false; // 继续传递事件
        });

        // 设置根布局的点击事件，用于处理列表外的点击
        binding.getRoot().setOnClickListener(v -> {
            if (v == binding.getRoot()) {
                adapter.closeOpenedItem();
            }
        });

        // 设置空视图的点击事件
        binding.emptyView.setOnClickListener(v -> adapter.closeOpenedItem());
    }

    @Override
    public void onAlarmEdit(AlarmEntity alarm) {
        adapter.closeOpenedItem();
        showEditAlarmDialog(alarm);
    }

    @Override
    public void onAlarmDelete(AlarmEntity alarm) {
        adapter.closeOpenedItem();
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_alarm_title)
            .setMessage(R.string.alarm_delete_confirm)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                viewModel.deleteAlarm(alarm);
                showDeleteSuccessMessage();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onAlarmToggle(AlarmEntity alarm, boolean enabled) {
        viewModel.toggleAlarm(requireActivity(), alarm.getId(), enabled);
    }

    private void showDeleteSuccessMessage() {
        Snackbar.make(
            binding.getRoot(),
            R.string.alarm_delete_success,
            Snackbar.LENGTH_SHORT
        ).show();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.closeOpenedItem(); // Fragment暂停时关闭打开的按钮
    }

    /**
     * 显示闹钟编辑对话框
     */
    private void showEditAlarmDialog(AlarmEntity alarm) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_alarm, null);
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(getHourFromMillis(alarm.getTimeInMillis()))
            .setMinute(getMinuteFromMillis(alarm.getTimeInMillis()))
            .setTitleText(R.string.edit_alarm_time)
            .build();

        EditText nameEdit = dialogView.findViewById(R.id.edit_alarm_name);
        nameEdit.setText(alarm.getName());

        CheckBox repeatCheck = dialogView.findViewById(R.id.check_repeat);
        repeatCheck.setChecked(alarm.isRepeat());

        LinearLayout repeatDaysLayout = dialogView.findViewById(R.id.layout_repeat_days);
        repeatDaysLayout.setVisibility(alarm.isRepeat() ? View.VISIBLE : View.GONE);

        // 重复日期选择
        CheckBox[] dayCheckBoxes = new CheckBox[7];
        int[] dayCheckBoxIds = {
            R.id.check_day_0, R.id.check_day_1, R.id.check_day_2,
            R.id.check_day_3, R.id.check_day_4, R.id.check_day_5,
            R.id.check_day_6
        };
        for (int i = 0; i < 7; i++) {
            dayCheckBoxes[i] = dialogView.findViewById(dayCheckBoxIds[i]);
            dayCheckBoxes[i].setChecked((alarm.getRepeatDays() & (1 << i)) != 0);
        }

        repeatCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repeatDaysLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // 声音和振动设置
        CheckBox vibrateCheck = dialogView.findViewById(R.id.check_vibrate);
        vibrateCheck.setChecked(alarm.isVibrate());

        TextView soundText = dialogView.findViewById(R.id.text_sound);
        soundText.setText(getSoundName(alarm.getSoundUri()));

        MaterialButton soundButton = dialogView.findViewById(R.id.button_sound);
        soundButton.setOnClickListener(v -> showSoundPicker(alarm, soundText));

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_alarm)
            .setView(dialogView)
            .setPositiveButton(R.string.save, (dialog, which) -> {
                // 保存时间设置
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(alarm.getTimeInMillis());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());

                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                alarm.setTimeInMillis(calendar.getTimeInMillis());
                alarm.setName(nameEdit.getText().toString().trim());
                alarm.setRepeat(repeatCheck.isChecked());

                // 保存重复日期设置
                if (alarm.isRepeat()) {
                    int repeatDays = 0;
                    for (int i = 0; i < 7; i++) {
                        if (dayCheckBoxes[i].isChecked()) {
                            repeatDays |= (1 << i);
                        }
                    }
                    alarm.setRepeatDays(repeatDays);
                }

                alarm.setVibrate(vibrateCheck.isChecked());
                viewModel.updateAlarm(requireActivity(), alarm);
                showUpdateSuccessMessage();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();

        // 显示时间选择器
        timePicker.show(getChildFragmentManager(), "time_picker");
    }

    /**
     * 显示声音选择器
     */
    private void showSoundPicker(AlarmEntity alarm, TextView soundText) {
        currentEditingAlarm = alarm;
        currentSoundText = soundText;
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_alarm_sound));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, 
            alarm.getSoundUri().isEmpty() ? null : Uri.parse(alarm.getSoundUri()));
        soundPickerLauncher.launch(intent);
    }

    /**
     * 获取铃声名称
     */
    private String getSoundName(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return getString(R.string.default_alarm_sound);
        }
        Ringtone ringtone = RingtoneManager.getRingtone(requireContext(), Uri.parse(uriString));
        return ringtone != null ? ringtone.getTitle(requireContext()) : getString(R.string.default_alarm_sound);
    }

    /**
     * 从毫秒时间戳中获取小时
     */
    private int getHourFromMillis(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 从毫秒时间戳中获取分钟
     */
    private int getMinuteFromMillis(long timeInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * 显示更新成功提示
     */
    private void showUpdateSuccessMessage() {
        Snackbar.make(binding.getRoot(), R.string.alarm_updated_successfully, Snackbar.LENGTH_SHORT).show();
    }
} 