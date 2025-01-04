package com.schedule.assistant.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.databinding.ItemShiftBinding;

import java.util.Objects;

public class ShiftListAdapter extends ListAdapter<Shift, ShiftListAdapter.ShiftViewHolder> {

    private final OnShiftClickListener listener;

    public interface OnShiftClickListener {
        void onShiftClick(Shift shift);
        void onShiftLongClick(Shift shift);
    }

    public ShiftListAdapter(OnShiftClickListener listener) {
        super(new ShiftDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftBinding binding = ItemShiftBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ShiftViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        Shift shift = getItem(position);
        if (shift == null) return;
        
        holder.bind(shift);
        
        if (shift.isNewlyAdded()) {
            Animation slideIn = AnimationUtils.loadAnimation(
                holder.itemView.getContext(), 
                R.anim.item_animation_slide_in
            );
            if (slideIn != null) {
                holder.itemView.startAnimation(slideIn);
                shift.setNewlyAdded(false);
            }
        }
    }

    static class ShiftViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftBinding binding;
        private final OnShiftClickListener listener;

        ShiftViewHolder(ItemShiftBinding binding, OnShiftClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(Shift shift) {
            binding.shiftNameText.setText(shift.getType().toString());
            binding.shiftTimeText.setText(String.format("%s - %s", 
                shift.getStartTime(), shift.getEndTime()));
            
            // 根据班次类型设置不同的背景色
            int backgroundColor = shift.getType().getColorResId();
            binding.getRoot().setCardBackgroundColor(
                itemView.getContext().getColor(backgroundColor));

            // 设置点击事件
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onShiftClick(shift));
                itemView.setOnLongClickListener(v -> {
                    listener.onShiftLongClick(shift);
                    return true;
                });
            }
        }
    }

    private static class ShiftDiffCallback extends DiffUtil.ItemCallback<Shift> {
        @Override
        public boolean areItemsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return Objects.equals(oldItem.getDate(), newItem.getDate()) &&
                   Objects.equals(oldItem.getType(), newItem.getType()) &&
                   Objects.equals(oldItem.getStartTime(), newItem.getStartTime()) &&
                   Objects.equals(oldItem.getEndTime(), newItem.getEndTime()) &&
                   Objects.equals(oldItem.getNote(), newItem.getNote());
        }
    }
} 