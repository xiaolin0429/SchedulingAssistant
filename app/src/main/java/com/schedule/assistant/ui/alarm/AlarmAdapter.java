package com.schedule.assistant.ui.alarm;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.Alarm;
import com.schedule.assistant.databinding.ItemAlarmBinding;

public class AlarmAdapter extends ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder> {
    private final OnAlarmToggleListener toggleListener;
    private final OnAlarmDeleteListener deleteListener;

    private static final DiffUtil.ItemCallback<Alarm> DIFF_CALLBACK = new DiffUtil.ItemCallback<Alarm>() {
        @Override
        public boolean areItemsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Alarm oldItem, @NonNull Alarm newItem) {
            return oldItem.getHour() == newItem.getHour() &&
                   oldItem.getMinute() == newItem.getMinute() &&
                   oldItem.isEnabled() == newItem.isEnabled();
        }
    };

    public AlarmAdapter(OnAlarmToggleListener toggleListener, OnAlarmDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.toggleListener = toggleListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAlarmBinding binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new AlarmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlarmBinding binding;

        AlarmViewHolder(ItemAlarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Alarm alarm) {
            binding.timeText.setText(alarm.getTimeString());
            binding.enableSwitch.setChecked(alarm.isEnabled());
            
            binding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked != alarm.isEnabled()) {
                    toggleListener.onAlarmToggle(alarm);
                }
            });
            
            binding.deleteButton.setOnClickListener(v -> deleteListener.onAlarmDelete(alarm));
        }
    }

    public interface OnAlarmToggleListener {
        void onAlarmToggle(Alarm alarm);
    }

    public interface OnAlarmDeleteListener {
        void onAlarmDelete(Alarm alarm);
    }
} 