package com.schedule.assistant.ui.shift;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.databinding.ItemShiftBinding;
import java.util.Objects;

public class ShiftAdapter extends ListAdapter<Shift, ShiftAdapter.ShiftViewHolder> {
    private static final DiffUtil.ItemCallback<Shift> DIFF_CALLBACK = new DiffUtil.ItemCallback<Shift>() {
        @Override
        public boolean areItemsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return oldItem.getDate().equals(newItem.getDate());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Shift oldItem, @NonNull Shift newItem) {
            return Objects.equals(oldItem.getType(), newItem.getType()) &&
                   Objects.equals(oldItem.getStartTime(), newItem.getStartTime()) &&
                   Objects.equals(oldItem.getEndTime(), newItem.getEndTime());
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
            binding.shiftNameText.setText(itemView.getContext()
                .getString(shift.getType().getNameResId()));
            binding.shiftTimeText.setText(String.format("%s - %s", 
                shift.getStartTime(), shift.getEndTime()));
            binding.typeIndicator.setBackgroundResource(shift.getType().getColorResId());
        }
    }
} 