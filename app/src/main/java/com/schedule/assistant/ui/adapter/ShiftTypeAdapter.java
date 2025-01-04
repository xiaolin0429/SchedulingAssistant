package com.schedule.assistant.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.ItemShiftTypeBinding;
import java.util.Objects;

public class ShiftTypeAdapter extends ListAdapter<ShiftTypeEntity, ShiftTypeAdapter.ViewHolder> {
    private final OnShiftTypeClickListener listener;

    public ShiftTypeAdapter(OnShiftTypeClickListener listener) {
        super(new DiffUtil.ItemCallback<ShiftTypeEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull ShiftTypeEntity oldItem, @NonNull ShiftTypeEntity newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ShiftTypeEntity oldItem, @NonNull ShiftTypeEntity newItem) {
                return Objects.equals(oldItem.getName(), newItem.getName()) &&
                       ((oldItem.getStartTime() == null && newItem.getStartTime() == null) ||
                        (oldItem.getStartTime() != null && oldItem.getStartTime().equals(newItem.getStartTime()))) &&
                       ((oldItem.getEndTime() == null && newItem.getEndTime() == null) ||
                        (oldItem.getEndTime() != null && oldItem.getEndTime().equals(newItem.getEndTime()))) &&
                       oldItem.getColor() == newItem.getColor() &&
                       Objects.equals(oldItem.getType(), newItem.getType());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftTypeBinding binding = ItemShiftTypeBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTypeBinding binding;

        ViewHolder(ItemShiftTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShiftTypeClick(getItem(position));
                }
            });

            binding.deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShiftTypeDelete(getItem(position));
                }
            });
        }

        void bind(ShiftTypeEntity shiftType) {
            binding.typeName.setText(shiftType.getName());
            if (shiftType.getStartTime() != null && shiftType.getEndTime() != null) {
                binding.typeTime.setText(String.format("%s - %s", 
                    shiftType.getStartTime(), shiftType.getEndTime()));
                binding.typeTime.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.typeTime.setVisibility(android.view.View.GONE);
            }

            // 设置颜色指示器
            binding.colorIndicator.setBackgroundColor(shiftType.getColor());

            // 如果是默认班次，隐藏删除按钮
            binding.deleteButton.setVisibility(
                shiftType.isDefault() ? android.view.View.GONE : android.view.View.VISIBLE
            );
        }
    }

    public interface OnShiftTypeClickListener {
        void onShiftTypeClick(ShiftTypeEntity shiftType);
        void onShiftTypeDelete(ShiftTypeEntity shiftType);
    }
}