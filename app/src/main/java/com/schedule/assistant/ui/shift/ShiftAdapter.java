package com.schedule.assistant.ui.shift;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.databinding.ItemShiftBinding;

public class ShiftAdapter extends ListAdapter<Shift, ShiftAdapter.ShiftViewHolder> {
    private static final DiffUtil.ItemCallback<Shift> DIFF_CALLBACK = new DiffUtil.ItemCallback<Shift>() {
        @Override
        public boolean areItemsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return oldItem.getDate().equals(newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return oldItem.getDate().equals(newItem.getDate()) &&
                   oldItem.getShiftType() == newItem.getShiftType() &&
                   (oldItem.getNote() == null ? newItem.getNote() == null : oldItem.getNote().equals(newItem.getNote()));
        }
    };

    public ShiftAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShiftBinding binding = ItemShiftBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ShiftViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ShiftViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftBinding binding;

        ShiftViewHolder(ItemShiftBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Shift shift) {
            binding.dateText.setText(shift.getDate());
            binding.shiftTypeText.setText(binding.getRoot().getContext()
                .getString(shift.getShiftType().getNameResId()));
            binding.shiftTypeIndicator.setBackgroundResource(shift.getShiftType().getColorResId());
            binding.noteText.setText(shift.getNote());
        }
    }
} 