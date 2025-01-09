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
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.databinding.DialogShiftTemplateBinding;
import com.schedule.assistant.viewmodel.ShiftTemplateViewModel;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.model.ColorShape;

public class ShiftTemplateDialogFragment extends BottomSheetDialogFragment {
    private DialogShiftTemplateBinding binding;
    private ShiftTemplateViewModel viewModel;
    private ShiftTemplate currentTemplate;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private int selectedColor = 0xFF4CAF50; // 默认颜色

    public static ShiftTemplateDialogFragment newInstance(@Nullable ShiftTemplate template) {
        ShiftTemplateDialogFragment fragment = new ShiftTemplateDialogFragment();
        Bundle args = new Bundle();
        if (template != null) {
            args.putLong("template_id", template.getId());
            args.putString("name", template.getName());
            args.putString("start_time", template.getStartTime());
            args.putString("end_time", template.getEndTime());
            args.putInt("color", template.getColor());
            args.putBoolean("is_default", template.isDefault());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShiftTemplateViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = DialogShiftTemplateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        loadTemplateData();
        setupListeners();
    }

    private void setupViews() {
        binding.startTimeLayout.setEndIconOnClickListener(v -> showTimePicker(true));
        binding.endTimeLayout.setEndIconOnClickListener(v -> showTimePicker(false));
        binding.selectColorButton.setOnClickListener(v -> showColorPicker());
    }

    private void loadTemplateData() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("template_id")) {
            binding.nameInput.setText(args.getString("name"));
            binding.startTimeInput.setText(args.getString("start_time"));
            binding.endTimeInput.setText(args.getString("end_time"));
            selectedColor = args.getInt("color");
            binding.colorPreview.setBackgroundColor(selectedColor);

            currentTemplate = new ShiftTemplate(
                    args.getString("name"),
                    args.getString("start_time"),
                    args.getString("end_time"),
                    args.getInt("color"));
            currentTemplate.setId(args.getLong("template_id"));
            currentTemplate.setDefault(args.getBoolean("is_default"));
        }
    }

    private void setupListeners() {
        binding.saveButton.setOnClickListener(v -> saveTemplate());
        binding.cancelButton.setOnClickListener(v -> dismiss());
    }

    private void showTimePicker(boolean isStartTime) {
        LocalTime defaultTime = LocalTime.of(isStartTime ? 9 : 18, 0);
        String currentTime = isStartTime ? binding.startTimeInput.getText().toString()
                : binding.endTimeInput.getText().toString();

        if (!currentTime.isEmpty()) {
            defaultTime = LocalTime.parse(currentTime, timeFormatter);
        }

        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    if (isStartTime) {
                        binding.startTimeInput.setText(time);
                    } else {
                        binding.endTimeInput.setText(time);
                    }
                },
                defaultTime.getHour(),
                defaultTime.getMinute(),
                true);
        dialog.show();
    }

    private void showColorPicker() {
        new ColorPickerDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_color))
                .setColorShape(ColorShape.SQAURE)
                .setDefaultColor(selectedColor)
                .setColorListener((color, colorHex) -> {
                    selectedColor = color;
                    binding.colorPreview.setBackgroundColor(color);
                })
                .show();
    }

    private void saveTemplate() {
        String name = binding.nameInput.getText().toString().trim();
        String startTime = binding.startTimeInput.getText().toString().trim();
        String endTime = binding.endTimeInput.getText().toString().trim();

        if (name.isEmpty()) {
            binding.nameLayout.setError(getString(R.string.error_empty_name));
            return;
        }

        ShiftTemplate template = new ShiftTemplate(name, startTime, endTime, selectedColor);
        if (currentTemplate != null) {
            template.setId(currentTemplate.getId());
            template.setDefault(currentTemplate.isDefault());
            viewModel.update(template);
        } else {
            viewModel.insert(template);
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}