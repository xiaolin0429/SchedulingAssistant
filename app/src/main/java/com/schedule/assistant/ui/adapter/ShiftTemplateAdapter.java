package com.schedule.assistant.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.ShiftTemplate;
import com.schedule.assistant.databinding.ItemShiftTemplateBinding;
import java.util.Objects;

public class ShiftTemplateAdapter extends ListAdapter<ShiftTemplate, ShiftTemplateAdapter.ViewHolder> {
    private final OnTemplateClickListener listener;

    public interface OnTemplateClickListener {
        void onTemplateClick(ShiftTemplate template);
        void onTemplateEdit(ShiftTemplate template);
        void onTemplateDelete(ShiftTemplate template);
    }

    public ShiftTemplateAdapter(OnTemplateClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftTemplateBinding binding = ItemShiftTemplateBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTemplateBinding binding;

        ViewHolder(ItemShiftTemplateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ShiftTemplate template) {
            binding.templateName.setText(template.getName());
            if (template.getStartTime() != null && template.getEndTime() != null) {
                binding.templateTime.setText(String.format("%s - %s", 
                    template.getStartTime(), template.getEndTime()));
                binding.templateTime.setVisibility(View.VISIBLE);
            } else {
                binding.templateTime.setVisibility(View.GONE);
            }

            // 设置颜色指示器
            binding.colorIndicator.setBackgroundColor(template.getColor());

            // 设置点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTemplateClick(getItem(position));
                    }
                }
            });

            // 编辑按钮
            binding.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTemplateEdit(getItem(position));
                    }
                }
            });

            // 删除按钮
            binding.deleteButton.setVisibility(template.isDefault() ? View.GONE : View.VISIBLE);
            binding.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTemplateDelete(getItem(position));
                    }
                }
            });
        }
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<ShiftTemplate> {
        @Override
        public boolean areItemsTheSame(@NonNull ShiftTemplate oldItem, @NonNull ShiftTemplate newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ShiftTemplate oldItem, @NonNull ShiftTemplate newItem) {
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                   oldItem.getColor() == newItem.getColor() &&
                   oldItem.isDefault() == newItem.isDefault() &&
                   ((oldItem.getStartTime() == null && newItem.getStartTime() == null) ||
                    (oldItem.getStartTime() != null && oldItem.getStartTime().equals(newItem.getStartTime()))) &&
                   ((oldItem.getEndTime() == null && newItem.getEndTime() == null) ||
                    (oldItem.getEndTime() != null && oldItem.getEndTime().equals(newItem.getEndTime())));
        }
    }
} 