package com.schedule.assistant.ui.shift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.databinding.FragmentShiftTemplateBinding;
import com.schedule.assistant.ui.adapter.ShiftTemplateAdapter;
import com.schedule.assistant.ui.dialog.ShiftTemplateDialogFragment;
import com.schedule.assistant.viewmodel.ShiftTemplateViewModel;

public class ShiftTemplateFragment extends Fragment implements ShiftTemplateAdapter.OnTemplateClickListener {
    private FragmentShiftTemplateBinding binding;
    private ShiftTemplateViewModel viewModel;
    private ShiftTemplateAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftTemplateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShiftTemplateViewModel.class);
        
        setupRecyclerView();
        setupButtons();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ShiftTemplateAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupButtons() {
        binding.addButton.setOnClickListener(v -> showTemplateDialog(null));
        binding.manageTypesButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.navigation_shift_type));
    }

    private void observeViewModel() {
        viewModel.getAllTemplates().observe(getViewLifecycleOwner(), templates -> {
            adapter.submitList(templates);
            binding.emptyView.setVisibility(templates.isEmpty() ? View.VISIBLE : View.GONE);
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
    public void onTemplateClick(ShiftTemplate template) {
        // 点击模板时的操作（如果需要）
    }

    @Override
    public void onTemplateEdit(ShiftTemplate template) {
        showTemplateDialog(template);
    }

    @Override
    public void onTemplateDelete(ShiftTemplate template) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_template_title)
            .setMessage(R.string.delete_template_message)
            .setPositiveButton(R.string.ok, (dialog, which) -> viewModel.delete(template))
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showTemplateDialog(@Nullable ShiftTemplate template) {
        ShiftTemplateDialogFragment dialog = ShiftTemplateDialogFragment.newInstance(template);
        dialog.show(getChildFragmentManager(), "template_dialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 