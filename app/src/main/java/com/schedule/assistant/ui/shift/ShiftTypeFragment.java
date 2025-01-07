package com.schedule.assistant.ui.shift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.FragmentShiftTypeBinding;
import com.schedule.assistant.ui.adapter.ShiftTypeAdapter;
import com.schedule.assistant.ui.dialog.ShiftTypeDialogFragment;
import com.schedule.assistant.viewmodel.ShiftTypeViewModel;

public class ShiftTypeFragment extends Fragment implements ShiftTypeAdapter.OnShiftTypeActionListener {
    private FragmentShiftTypeBinding binding;
    private ShiftTypeViewModel viewModel;
    private ShiftTypeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftTypeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShiftTypeViewModel.class);
        setupRecyclerView();
        setupAddButton();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ShiftTypeAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 设置RecyclerView的触摸监听，点击空白区域时关闭已打开的按钮
        binding.recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                adapter.closeOpenedItem();
            }
            // 确保点击事件能够正常传递
            v.performClick();
            // 返回false以允许其他触摸事件继续传递
            return false;
        });

        // 添加点击事件监听器以满足可访问性要求
        binding.recyclerView.setOnClickListener(v -> {
            // 空实现，仅用于满足可访问性要求
        });
    }

    private void setupAddButton() {
        binding.addButton.setOnClickListener(v -> showShiftTypeDialog(null));
    }

    private void observeViewModel() {
        viewModel.getAllShiftTypes().observe(getViewLifecycleOwner(), shiftTypes -> {
            adapter.submitList(shiftTypes);
            binding.emptyView.setVisibility(shiftTypes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.error)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            }
        });
    }

    @Override
    public void onShiftTypeEdit(ShiftTypeEntity shiftType) {
        showShiftTypeDialog(shiftType);
    }

    @Override
    public void onShiftTypeDelete(ShiftTypeEntity shiftType) {
        if (shiftType.isDefault()) {
            // 在底部显示Toast提示
            android.widget.Toast.makeText(
                requireContext(),
                R.string.cannot_delete_default_shift_type,
                android.widget.Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_shift_type)
            .setMessage(R.string.confirm_delete_shift_type)
            .setPositiveButton(R.string.confirm, (dialog, which) -> viewModel.delete(shiftType))
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showShiftTypeDialog(@Nullable ShiftTypeEntity shiftType) {
        ShiftTypeDialogFragment dialog = ShiftTypeDialogFragment.newInstance(shiftType);
        dialog.show(getChildFragmentManager(), "shift_type_dialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 