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
import com.schedule.assistant.databinding.FragmentShiftBinding;
import com.schedule.assistant.data.entity.Shift;

public class ShiftFragment extends Fragment {
    private FragmentShiftBinding binding;
    private ShiftViewModel viewModel;
    private ShiftAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentShiftBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShiftViewModel.class);
        
        setupRecyclerView();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new ShiftAdapter();
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void observeViewModel() {
        viewModel.getShifts().observe(getViewLifecycleOwner(), shifts -> {
            if (shifts != null && !shifts.isEmpty()) {
                adapter.submitList(shifts);
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