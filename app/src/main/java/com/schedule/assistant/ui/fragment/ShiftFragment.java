package com.schedule.assistant.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.SortOption;
import com.schedule.assistant.databinding.FragmentShiftManagementBinding;
import com.schedule.assistant.ui.adapter.ShiftListAdapter;
import com.schedule.assistant.ui.dialog.ShiftDetailDialogFragment;
import com.schedule.assistant.util.RecyclerViewAnimationUtil;
import com.schedule.assistant.viewmodel.ShiftViewModel;

import java.util.List;

public class ShiftFragment extends Fragment implements ShiftListAdapter.OnShiftClickListener {

    private FragmentShiftManagementBinding binding;
    private ShiftViewModel viewModel;
    private ShiftListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ShiftViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentShiftManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        adapter = new ShiftListAdapter(this);
        binding.shiftRecyclerView.setAdapter(adapter);
        binding.shiftRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 初始化时运行布局动画
        RecyclerViewAnimationUtil.runLayoutAnimation(binding.shiftRecyclerView);
    }

    private void setupObservers() {
        viewModel.getAllShifts().observe(getViewLifecycleOwner(), shifts -> {
            adapter.submitList(shifts);
            // 数据更新后运行布局动画
            RecyclerViewAnimationUtil.runLayoutAnimation(binding.shiftRecyclerView);
        });

        viewModel.getIsAscending().observe(getViewLifecycleOwner(), isAscending -> {
            binding.sortButton.setImageResource(isAscending ? 
                R.drawable.ic_sort_ascending : R.drawable.ic_sort_descending);
        });

        // 设置排序按钮点击事件
        binding.sortButton.setOnClickListener(v -> showSortOptionsDialog());
    }

    private void deleteShift(Shift shift) {
        if (shift == null) return;
        
        Animation slideOut = AnimationUtils.loadAnimation(requireContext(), R.anim.item_animation_slide_out);
        if (slideOut == null) {
            viewModel.delete(shift);
            return;
        }

        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                viewModel.delete(shift);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        List<Shift> currentList = adapter.getCurrentList();
        if (currentList == null) {
            viewModel.delete(shift);
            return;
        }

        RecyclerView.ViewHolder viewHolder = binding.shiftRecyclerView
                .findViewHolderForAdapterPosition(currentList.indexOf(shift));
        if (viewHolder != null && viewHolder.itemView != null) {
            viewHolder.itemView.startAnimation(slideOut);
        } else {
            viewModel.delete(shift);
        }
    }

    private void showSortOptionsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_options, null);
        RadioGroup radioGroup = (RadioGroup) dialogView;

        // 设置当前选中的排序选项
        viewModel.getCurrentSortOption().observe(getViewLifecycleOwner(), currentOption -> {
            int selectedId;
            switch (currentOption) {
                case DATE_ASC:
                    selectedId = R.id.sortByDateAsc;
                    break;
                case DATE_DESC:
                    selectedId = R.id.sortByDateDesc;
                    break;
                case TYPE:
                    selectedId = R.id.sortByType;
                    break;
                case UPDATE_TIME:
                    selectedId = R.id.sortByUpdateTime;
                    break;
                default:
                    selectedId = R.id.sortByDateAsc;
            }
            radioGroup.check(selectedId);
        });

        // 创建对话框
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_options)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    // 根据选中的选项设置排序方式
                    int checkedId = radioGroup.getCheckedRadioButtonId();
                    SortOption selectedOption;
                    if (checkedId == R.id.sortByDateAsc) {
                        selectedOption = SortOption.DATE_ASC;
                    } else if (checkedId == R.id.sortByDateDesc) {
                        selectedOption = SortOption.DATE_DESC;
                    } else if (checkedId == R.id.sortByType) {
                        selectedOption = SortOption.TYPE;
                    } else {
                        selectedOption = SortOption.UPDATE_TIME;
                    }
                    viewModel.setSortOption(selectedOption);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onShiftClick(Shift shift) {
        showShiftDetailDialog(shift);
    }

    @Override
    public void onShiftLongClick(Shift shift) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    viewModel.delete(shift);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showShiftDetailDialog(@Nullable Shift shift) {
        ShiftDetailDialogFragment dialog = ShiftDetailDialogFragment.newInstance(shift);
        dialog.show(getChildFragmentManager(), "shift_detail");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 