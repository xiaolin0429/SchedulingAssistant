package com.schedule.assistant.ui.dialog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.databinding.DialogShiftDetailBinding;
import com.schedule.assistant.viewmodel.ShiftViewModel;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ShiftDetailDialogFragment extends BottomSheetDialogFragment {
    private DialogShiftDetailBinding binding;
    private ShiftViewModel viewModel;
    private Shift currentShift;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_NOTE_LENGTH = 200;

    public static ShiftDetailDialogFragment newInstance(Shift shift) {
        ShiftDetailDialogFragment fragment = new ShiftDetailDialogFragment();
        Bundle args = new Bundle();
        if (shift != null) {
            // TODO: Add shift data to arguments
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShiftViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置对话框宽度为屏幕宽度
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = DialogShiftDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && errorMessage.equals("error_duplicate_shift")) {
                binding.dateLayout.setError(getString(R.string.error_duplicate_shift));
            }
        });
    }

    private void setupViews() {
        // 设置标题
        binding.titleText.setText(currentShift == null ? R.string.add_schedule : R.string.edit_shift);

        // 设置班次类型下拉菜单
        String[] shiftTypes = new String[]{
                getString(R.string.day_shift),
                getString(R.string.night_shift),
                getString(R.string.rest_day)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                shiftTypes
        );
        binding.shiftTypeInput.setAdapter(adapter);

        // 如果是编辑模式，填充现有数据
        if (currentShift != null) {
            binding.dateEditText.setText(currentShift.getDate());
            binding.shiftTypeInput.setText(getShiftTypeName(currentShift.getType()), false);
            binding.startTimeEditText.setText(currentShift.getStartTime());
            binding.endTimeEditText.setText(currentShift.getEndTime());
            binding.noteEditText.setText(currentShift.getNote());
        }

        // 设置输入框错误提示
        binding.dateLayout.setErrorIconDrawable(null);
        binding.shiftTypeLayout.setErrorIconDrawable(null);
        binding.startTimeLayout.setErrorIconDrawable(null);
        binding.endTimeLayout.setErrorIconDrawable(null);
        binding.noteLayout.setErrorIconDrawable(null);
    }

    private void setupListeners() {
        // 日期选择
        binding.dateEditText.setOnClickListener(v -> showDatePicker());

        // 时间选择
        binding.startTimeEditText.setOnClickListener(v -> showTimePicker(true));
        binding.endTimeEditText.setOnClickListener(v -> showTimePicker(false));

        // 取消按钮
        binding.cancelButton.setOnClickListener(v -> dismiss());

        // 确认按钮
        binding.confirmButton.setOnClickListener(v -> saveShift());
    }

    private void showDatePicker() {
        LocalDate initialDate = currentShift != null ? 
                LocalDate.parse(currentShift.getDate(), dateFormatter) : 
                LocalDate.now();
        
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.dateEditText.setText(selectedDate.format(dateFormatter));
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );
        dialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        LocalTime initialTime = LocalTime.now();
        if (currentShift != null) {
            String timeStr = isStartTime ? currentShift.getStartTime() : currentShift.getEndTime();
            if (timeStr != null) {
                initialTime = LocalTime.parse(timeStr, timeFormatter);
            }
        }

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                    String formattedTime = selectedTime.format(timeFormatter);
                    if (isStartTime) {
                        binding.startTimeEditText.setText(formattedTime);
                    } else {
                        binding.endTimeEditText.setText(formattedTime);
                    }
                },
                initialTime.getHour(),
                initialTime.getMinute(),
                true
        );
        dialog.show();
    }

    private void saveShift() {
        if (!validateInputs()) return;

        String date = binding.dateEditText.getText().toString();
        String startTime = binding.startTimeEditText.getText().toString();
        String endTime = binding.endTimeEditText.getText().toString();
        String note = binding.noteEditText.getText().toString();
        ShiftType type = getSelectedShiftType();

        if (currentShift == null) {
            // 创建新班次
            Shift newShift = new Shift(date, type);
            newShift.setStartTime(startTime);
            newShift.setEndTime(endTime);
            newShift.setNote(note);
            viewModel.insert(newShift);
        } else {
            // 更新现有班次
            currentShift.setDate(date);
            currentShift.setType(type);
            currentShift.setStartTime(startTime);
            currentShift.setEndTime(endTime);
            currentShift.setNote(note);
            viewModel.update(currentShift);
        }

        dismiss();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // 验证日期
        if (TextUtils.isEmpty(binding.dateEditText.getText().toString())) {
            binding.dateLayout.setError(getString(R.string.error_date_required));
            isValid = false;
        } else {
            try {
                LocalDate.parse(binding.dateEditText.getText().toString(), dateFormatter);
            } catch (DateTimeParseException e) {
                binding.dateLayout.setError(getString(R.string.invalid_date));
                isValid = false;
            }
        }

        // 验证班次类型
        if (TextUtils.isEmpty(binding.shiftTypeInput.getText().toString())) {
            binding.shiftTypeLayout.setError(getString(R.string.error_shift_type_required));
            isValid = false;
        }

        // 验证时间
        if (TextUtils.isEmpty(binding.startTimeEditText.getText().toString())) {
            binding.startTimeLayout.setError(getString(R.string.error_time_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(binding.endTimeEditText.getText().toString())) {
            binding.endTimeLayout.setError(getString(R.string.error_time_required));
            isValid = false;
        }

        // 验证时间范围
        if (!TextUtils.isEmpty(binding.startTimeEditText.getText().toString()) && !TextUtils.isEmpty(binding.endTimeEditText.getText().toString())) {
            try {
                LocalTime start = LocalTime.parse(binding.startTimeEditText.getText().toString(), timeFormatter);
                LocalTime end = LocalTime.parse(binding.endTimeEditText.getText().toString(), timeFormatter);
                if (end.isBefore(start)) {
                    binding.endTimeLayout.setError(getString(R.string.error_invalid_time_range));
                    isValid = false;
                }
            } catch (DateTimeParseException e) {
                binding.startTimeLayout.setError(getString(R.string.error_time_required));
                binding.endTimeLayout.setError(getString(R.string.error_time_required));
                isValid = false;
            }
        }

        // 验证备注长度
        if (!TextUtils.isEmpty(binding.noteEditText.getText().toString()) && binding.noteEditText.getText().toString().length() > MAX_NOTE_LENGTH) {
            binding.noteLayout.setError(getString(R.string.error_note_too_long));
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        binding.dateLayout.setError(null);
        binding.shiftTypeLayout.setError(null);
        binding.startTimeLayout.setError(null);
        binding.endTimeLayout.setError(null);
        binding.noteLayout.setError(null);
    }

    private String getShiftTypeName(ShiftType type) {
        return getString(type.getNameResId());
    }

    private ShiftType getSelectedShiftType() {
        String selectedShiftTypeName = binding.shiftTypeInput.getText().toString();
        if (getString(R.string.day_shift).equals(selectedShiftTypeName)) {
            return ShiftType.DAY_SHIFT;
        } else if (getString(R.string.night_shift).equals(selectedShiftTypeName)) {
            return ShiftType.NIGHT_SHIFT;
        } else if (getString(R.string.rest_day).equals(selectedShiftTypeName)) {
            return ShiftType.REST_DAY;
        } else {
            return ShiftType.NO_SHIFT;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 