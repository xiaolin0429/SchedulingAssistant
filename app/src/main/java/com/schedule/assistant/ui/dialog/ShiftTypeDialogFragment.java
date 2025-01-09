package com.schedule.assistant.ui.dialog;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.DialogShiftTypeBinding;
import com.schedule.assistant.viewmodel.ShiftTypeViewModel;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import android.widget.ArrayAdapter;

public class ShiftTypeDialogFragment extends BottomSheetDialogFragment {
    private DialogShiftTypeBinding binding;
    private ShiftTypeViewModel viewModel;
    private ShiftTypeEntity currentShiftType;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private int selectedColor = 0xFF4CAF50; // 默认颜色

    public static ShiftTypeDialogFragment newInstance(@Nullable ShiftTypeEntity shiftType) {
        ShiftTypeDialogFragment fragment = new ShiftTypeDialogFragment();
        Bundle args = new Bundle();
        if (shiftType != null) {
            args.putLong("id", shiftType.getId());
            args.putString("name", shiftType.getName());
            args.putString("start_time", shiftType.getStartTime());
            args.putString("end_time", shiftType.getEndTime());
            args.putInt("color", shiftType.getColor());
            args.putBoolean("is_default", shiftType.isDefault());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShiftTypeViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);

        // 从参数中恢复当前编辑的班次类型
        Bundle args = getArguments();
        if (args != null && args.containsKey("id")) {
            currentShiftType = new ShiftTypeEntity(
                args.getString("name"),
                args.getString("start_time"),
                args.getString("end_time"),
                args.getInt("color")
            );
            currentShiftType.setId(args.getLong("id"));
            currentShiftType.setDefault(args.getBoolean("is_default"));
            selectedColor = args.getInt("color");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = DialogShiftTypeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        // 设置标题
        binding.titleText.setText(currentShiftType == null ? 
            R.string.add_shift_type : R.string.edit_shift_type);

        // 如果是编辑模式，填充现有数据
        if (currentShiftType != null) {
            binding.nameInput.setText(currentShiftType.getName());
            binding.startTimeInput.setText(currentShiftType.getStartTime());
            binding.endTimeInput.setText(currentShiftType.getEndTime());
            binding.colorPreview.setBackgroundColor(currentShiftType.getColor());
        }
    }

    private void setupListeners() {
        // 时间选择
        binding.startTimeInput.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeInput.setOnClickListener(v -> showTimePicker(false));

        // 颜色选择
        binding.colorPreview.setOnClickListener(v -> showColorPicker());

        // 取消按钮
        binding.cancelButton.setOnClickListener(v -> dismiss());

        // 确认按钮
        binding.confirmButton.setOnClickListener(v -> saveShiftType());
    }

    private void showTimePicker(boolean isStartTime) {
        LocalTime initialTime = LocalTime.now();
        String currentTime = isStartTime ? 
            binding.startTimeInput.getText().toString() : 
            binding.endTimeInput.getText().toString();

        if (!currentTime.isEmpty()) {
            try {
                initialTime = LocalTime.parse(currentTime, timeFormatter);
            } catch (Exception ignored) {}
        }

        TimePickerDialog dialog = new TimePickerDialog(
            requireContext(),
            (view, hourOfDay, minute) -> {
                LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                String formattedTime = selectedTime.format(timeFormatter);
                if (isStartTime) {
                    binding.startTimeInput.setText(formattedTime);
                } else {
                    binding.endTimeInput.setText(formattedTime);
                }
            },
            initialTime.getHour(),
            initialTime.getMinute(),
            true
        );
        dialog.show();
    }

    private void showColorPicker() {
        new ColorPickerDialog
            .Builder(requireContext())
            .setTitle(R.string.select_color)
            .setColorShape(ColorShape.CIRCLE)
            .setDefaultColor(selectedColor)
            .setColorListener((color, colorHex) -> {
                selectedColor = color;
                binding.colorPreview.setBackgroundColor(color);
            })
            .show();
    }

    private void saveShiftType() {
        String name = binding.nameInput.getText().toString().trim();
        String startTime = binding.startTimeInput.getText().toString().trim();
        String endTime = binding.endTimeInput.getText().toString().trim();

        // 验证输入
        if (name.isEmpty()) {
            binding.nameLayout.setError(getString(R.string.error_empty_name));
            return;
        }

        // 创建或更新班次类型
        ShiftTypeEntity shiftType;
        shiftType = Objects.requireNonNullElseGet(currentShiftType, () -> new ShiftTypeEntity(name, startTime, endTime, selectedColor));

        shiftType.setName(name);
        shiftType.setStartTime(startTime);
        shiftType.setEndTime(endTime);
        shiftType.setColor(selectedColor);
        shiftType.setUpdateTime(System.currentTimeMillis());

        // 保存到数据库
        if (currentShiftType != null) {
            viewModel.update(shiftType);
        } else {
            viewModel.insert(shiftType);
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 