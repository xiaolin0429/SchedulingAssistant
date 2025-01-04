package com.schedule.assistant.ui.alarm;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.databinding.ItemAlarmBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AlarmAdapter extends ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder> {
    private final OnAlarmToggleListener toggleListener;
    private final OnAlarmDeleteListener deleteListener;
    private final SimpleDateFormat timeFormat;

    private static final DiffUtil.ItemCallback<AlarmEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {
            return oldItem.getTime() == newItem.getTime() &&
                   oldItem.isEnabled() == newItem.isEnabled() &&
                   Objects.equals(oldItem.getName(), newItem.getName());
        }
    };

    public AlarmAdapter(OnAlarmToggleListener toggleListener, OnAlarmDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.toggleListener = toggleListener;
        this.deleteListener = deleteListener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
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

        void bind(AlarmEntity alarm) {
            binding.timeText.setText(timeFormat.format(new Date(alarm.getTime())));
            binding.enableSwitch.setChecked(alarm.isEnabled());
            
            binding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked != alarm.isEnabled()) {
                    toggleListener.onAlarmToggle(alarm);
                }
            });
            
            binding.deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    deleteListener.onAlarmDelete(getItem(position));
                }
            });
        }
    }

    public interface OnAlarmToggleListener {
        void onAlarmToggle(AlarmEntity alarm);
    }

    public interface OnAlarmDeleteListener {
        void onAlarmDelete(AlarmEntity alarm);
    }
} 