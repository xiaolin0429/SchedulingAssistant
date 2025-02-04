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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.ShiftTemplate;
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
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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
            if (errorMessage == null)
                return;

            switch (errorMessage) {
                case "error_duplicate_shift":
                    binding.dateLayout.setError(getString(R.string.error_duplicate_shift));
                    break;
                case "error_date_required":
                    binding.dateLayout.setError(getString(R.string.error_date_required));
                    break;
                case "error_shift_type_required":
                    binding.shiftTypeLayout.setError(getString(R.string.error_shift_type_required));
                    break;
                case "error_invalid_shift":
                case "error_insert_failed":
                case "error_update_failed":
                case "error_database_operation":
                    // 显示通用错误消息
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.error)
                            .setMessage(R.string.error_database_operation)
                            .setPositiveButton(R.string.confirm, null)
                            .show();
                    break;
                case "error_required_fields":
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.error)
                            .setMessage(R.string.error_required_fields)
                            .setPositiveButton(R.string.confirm, null)
                            .show();
                    break;
            }
        });
    }

    private void setupViews() {
        // 设置标题
        binding.titleText.setText(currentShift == null ? R.string.add_schedule : R.string.edit_shift);

        // 从ViewModel获取所有班次模板
        viewModel.getAllTemplates().observe(getViewLifecycleOwner(), templates -> {
            if (templates != null && !templates.isEmpty()) {
                String[] shiftNames = new String[templates.size()];
                for (int i = 0; i < templates.size(); i++) {
                    shiftNames[i] = templates.get(i).getName();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        shiftNames);
                binding.shiftTypeInput.setAdapter(adapter);

                // 如果是编辑模式，设置当前值
                if (currentShift != null) {
                    binding.dateEditText.setText(currentShift.getDate());
                    // 根据当前班次类型找到对应的模板名称
                    for (ShiftTemplate template : templates) {
                        if (template.getType() == currentShift.getType()) {
                            binding.shiftTypeInput.setText(template.getName(), false);
                            break;
                        }
                    }
                    binding.startTimeEditText.setText(currentShift.getStartTime());
                    binding.endTimeEditText.setText(currentShift.getEndTime());
                    binding.noteEditText.setText(currentShift.getNote());
                } else {
                    // 新建模式下设置默认值
                    binding.shiftTypeInput.setText(shiftNames[0], false);
                }
            }
        });

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
        LocalDate initialDate = currentShift != null ? LocalDate.parse(currentShift.getDate(), dateFormatter)
                : LocalDate.now();

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    binding.dateEditText.setText(selectedDate.format(dateFormatter));
                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth());
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
                true);
        dialog.show();
    }

    private void saveShift() {
        String date = binding.dateEditText.getText().toString();
        String shiftTypeName = binding.shiftTypeInput.getText().toString();
        String startTime = binding.startTimeEditText.getText().toString();
        String endTime = binding.endTimeEditText.getText().toString();
        String note = binding.noteEditText.getText().toString();

        if (!validateInputs()) {
            return;
        }

        // 处理结束时间
        final String finalEndTime = endTime.equals("00:00") ? "23:59" : endTime;

        // 从选择的模板名称获取对应的班次类型
        viewModel.getAllTemplates().observe(getViewLifecycleOwner(), templates -> {
            ShiftType selectedType = null;
            String selectedStartTime = startTime;
            String selectedEndTime = finalEndTime;

            // 首先尝试从模板中获取班次类型
            for (ShiftTemplate template : templates) {
                if (template.getName().equals(shiftTypeName)) {
                    selectedType = template.getType();
                    // 如果没有手动设置时间，使用模板的默认时间
                    if (startTime.isEmpty()) {
                        selectedStartTime = template.getStartTime();
                    }
                    if (finalEndTime.isEmpty()) {
                        selectedEndTime = template.getEndTime();
                    }
                    break;
                }
            }

            // 如果没有找到对应的模板，使用默认的班次类型映射
            if (selectedType == null) {
                selectedType = getDefaultShiftType(shiftTypeName);
                // 设置默认时间
                if (startTime.isEmpty()) {
                    selectedStartTime = getDefaultStartTime(selectedType);
                }
                if (finalEndTime.isEmpty()) {
                    selectedEndTime = getDefaultEndTime(selectedType);
                }
            }

            if (selectedType == null) {
                binding.shiftTypeLayout.setError(getString(R.string.error_type_required));
                return;
            }

            Shift shift = new Shift(date, selectedType);
            shift.setStartTime(selectedStartTime);
            shift.setEndTime(selectedEndTime);
            shift.setNote(note);

            if (currentShift != null) {
                shift.setId(currentShift.getId());
                viewModel.updateShift(shift);
            } else {
                viewModel.insertShift(shift);
            }

            dismiss();
        });
    }

    // 获取默认的班次类型（兜底策略）
    private ShiftType getDefaultShiftType(String shiftTypeName) {
        return switch (shiftTypeName) {
            case "早班" -> ShiftType.DAY_SHIFT;
            case "晚班" -> ShiftType.NIGHT_SHIFT;
            case "休息" -> ShiftType.REST_DAY;
            default -> ShiftType.DAY_SHIFT; // 默认返回早班
        };
    }

    // 获取默认的开始时间
    private String getDefaultStartTime(ShiftType type) {
        return switch (type) {
            case DAY_SHIFT -> "08:00";
            case NIGHT_SHIFT -> "16:00";
            default -> "";
        };
    }

    // 获取默认的结束时间
    private String getDefaultEndTime(ShiftType type) {
        return switch (type) {
            case DAY_SHIFT -> "16:00";
            case NIGHT_SHIFT -> "00:00";
            default -> "";
        };
    }

    private boolean validateInputs() {
        boolean isValid = true;
        clearErrors();

        String date = binding.dateEditText.getText().toString().trim();
        String shiftType = binding.shiftTypeInput.getText().toString().trim();
        String startTime = binding.startTimeEditText.getText().toString().trim();
        String endTime = binding.endTimeEditText.getText().toString().trim();
        String note = binding.noteEditText.getText().toString().trim();

        // 验证日期
        if (date.isEmpty()) {
            binding.dateLayout.setError(getString(R.string.error_date_required));
            isValid = false;
        } else {
            try {
                LocalDate.parse(date, dateFormatter);
            } catch (DateTimeParseException e) {
                binding.dateLayout.setError(getString(R.string.invalid_date));
                isValid = false;
            }
        }

        // 验证班次类型
        if (shiftType.isEmpty()) {
            binding.shiftTypeLayout.setError(getString(R.string.error_shift_type_required));
            isValid = false;
        }

        // 验证时间
        // 如果其中一个时间被填写，另一个也必须填写
        if (!startTime.isEmpty() || !endTime.isEmpty()) {
            if (startTime.isEmpty()) {
                binding.startTimeLayout.setError(getString(R.string.error_time_required));
                isValid = false;
            }
            if (endTime.isEmpty()) {
                binding.endTimeLayout.setError(getString(R.string.error_time_required));
                isValid = false;
            }

            // 只有当两个时间都填写了才验证时间范围
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                try {
                    // 特殊处理00:00-00:00的情况，认为它是合法的（表示全天24小时）
                    if (!startTime.equals("00:00") || !endTime.equals("00:00")) {
                        LocalTime start = LocalTime.parse(startTime, timeFormatter);
                        // 如果结束时间是00:00，在验证时就转换为23:59
                        LocalTime end = endTime.equals("00:00") ? LocalTime.of(23, 59)
                                : LocalTime.parse(endTime, timeFormatter);
                        if (end.isBefore(start)) {
                            binding.endTimeLayout.setError(getString(R.string.error_invalid_time_range));
                            isValid = false;
                        }
                    }
                } catch (DateTimeParseException e) {
                    binding.startTimeLayout.setError(getString(R.string.error_time_required));
                    binding.endTimeLayout.setError(getString(R.string.error_time_required));
                    isValid = false;
                }
            }
        }

        // 验证备注长度
        if (!note.isEmpty() && note.length() > MAX_NOTE_LENGTH) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}