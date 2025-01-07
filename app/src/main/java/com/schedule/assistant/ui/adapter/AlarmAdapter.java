package com.schedule.assistant.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.AlarmEntity;
import com.schedule.assistant.databinding.ItemAlarmBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 闹钟列表适配器
 */
public class AlarmAdapter extends ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder> {
    private final OnAlarmActionListener listener;
    private final SimpleDateFormat timeFormat;
    private boolean isEditMode = false;

    private static final DiffUtil.ItemCallback<AlarmEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<AlarmEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AlarmEntity oldItem, @NonNull AlarmEntity newItem) {
            return oldItem.getTimeInMillis() == newItem.getTimeInMillis() &&
                   oldItem.isEnabled() == newItem.isEnabled() &&
                   oldItem.isRepeat() == newItem.isRepeat() &&
                   oldItem.getRepeatDays() == newItem.getRepeatDays() &&
                   oldItem.getName().equals(newItem.getName());
        }
    };

    public AlarmAdapter(OnAlarmActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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

    public void setEditMode(boolean editMode) {
        if (this.isEditMode != editMode) {
            this.isEditMode = editMode;
            notifyDataSetChanged();
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlarmBinding binding;

        AlarmViewHolder(ItemAlarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AlarmEntity alarm) {
            // 设置时间
            binding.timeText.setText(timeFormat.format(new Date(alarm.getTimeInMillis())));
            
            // 设置名称
            binding.nameText.setText(alarm.getName());
            
            // 设置重复信息
            binding.repeatText.setText(getRepeatText(alarm));
            
            // 设置开关状态
            binding.enableSwitch.setChecked(alarm.isEnabled());
            
            // 根据编辑模式显示/隐藏删除按钮
            binding.deleteButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
            binding.enableSwitch.setVisibility(isEditMode ? View.GONE : View.VISIBLE);

            // 设置点击事件
            binding.getRoot().setOnClickListener(v -> {
                if (!isEditMode) {
                    listener.onAlarmClick(alarm);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                setEditMode(!isEditMode);
                return true;
            });

            // 设置开关切换事件
            binding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    listener.onAlarmToggle(alarm, isChecked);
                }
            });

            // 设置删除事件
            binding.deleteButton.setOnClickListener(v -> listener.onAlarmDelete(alarm));
        }

        private String getRepeatText(AlarmEntity alarm) {
            if (!alarm.isRepeat()) {
                // 检查是否是今天
                Calendar alarmTime = Calendar.getInstance();
                alarmTime.setTimeInMillis(alarm.getTimeInMillis());
                
                Calendar now = Calendar.getInstance();
                if (alarmTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    alarmTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                    return "今天";
                }
                if (alarmTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    alarmTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1) {
                    return "明天";
                }
                return "单次";
            }

            StringBuilder days = new StringBuilder();
            int repeatDays = alarm.getRepeatDays();
            String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
            
            // 检查是否是每天重复
            if (repeatDays == 0x7F) {
                return "每天";
            }
            
            // 检查是否是工作日重复
            if (repeatDays == 0x3E) {
                return "工作日";
            }
            
            // 检查是否是周末重复
            if (repeatDays == 0x41) {
                return "周末";
            }

            // 其他自定义重复
            for (int i = 0; i < 7; i++) {
                if ((repeatDays & (1 << i)) != 0) {
                    if (days.length() > 0) {
                        days.append("、");
                    }
                    days.append(weekDays[i]);
                }
            }
            return days.toString();
        }
    }

    public interface OnAlarmActionListener {
        void onAlarmClick(AlarmEntity alarm);
        void onAlarmToggle(AlarmEntity alarm, boolean enabled);
        void onAlarmDelete(AlarmEntity alarm);
    }
} 