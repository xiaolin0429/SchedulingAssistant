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
 * 负责闹钟列表项的展示和交互
 */
public class AlarmAdapter extends ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder> {
    private final OnAlarmActionListener listener;
    private final SimpleDateFormat timeFormat;
    private AlarmViewHolder currentOpenedItem = null;
    private static final long DEBOUNCE_TIME = 300L;
    private long lastClickTime = 0;

    private static final DiffUtil.ItemCallback<AlarmEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
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

    /**
     * 关闭当前打开的项目
     */
    public void closeOpenedItem() {
        if (currentOpenedItem != null) {
            currentOpenedItem.hideButtons();
            currentOpenedItem = null;
        }
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder {
        private final ItemAlarmBinding binding;

        AlarmViewHolder(ItemAlarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setupClickListeners();
        }

        /**
         * 设置点击事件监听器
         */
        private void setupClickListeners() {
            // 内容区域点击事件
            binding.contentLayout.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    return;
                }
                lastClickTime = currentTime;

                if (currentOpenedItem == this) {
                    hideButtons();
                    currentOpenedItem = null;
                } else {
                    if (currentOpenedItem != null) {
                        currentOpenedItem.hideButtons();
                    }
                    showButtons();
                    currentOpenedItem = this;
                }
            });

            // 编辑按钮点击事件
            binding.editButton.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    return;
                }
                lastClickTime = currentTime;

                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlarmEdit(getItem(position));
                    hideButtons();
                    currentOpenedItem = null;
                }
            });

            // 删除按钮点击事件
            binding.deleteButton.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    return;
                }
                lastClickTime = currentTime;

                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAlarmDelete(getItem(position));
                    hideButtons();
                    currentOpenedItem = null;
                }
            });

            // 开关切换事件
            binding.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAlarmToggle(getItem(position), isChecked);
                    }
                }
            });
        }

        /**
         * 绑定闹钟数据
         */
        void bind(AlarmEntity alarm) {
            // 设置时间
            binding.timeText.setText(timeFormat.format(new Date(alarm.getTimeInMillis())));
            
            // 设置名称
            binding.nameText.setText(alarm.getName());
            
            // 设置重复信息
            binding.repeatText.setText(getRepeatText(alarm));
            
            // 设置开关状态
            binding.enableSwitch.setChecked(alarm.isEnabled());
            
            // 重置按钮状态
            hideButtons();
            if (currentOpenedItem == this) {
                currentOpenedItem = null;
            }
        }

        /**
         * 获取重复文本
         */
        private String getRepeatText(AlarmEntity alarm) {
            if (!alarm.isRepeat()) {
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

        void showButtons() {
            binding.buttonLayout.setVisibility(View.VISIBLE);
            binding.enableSwitch.setVisibility(View.GONE);
        }

        void hideButtons() {
            binding.buttonLayout.setVisibility(View.GONE);
            binding.enableSwitch.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 闹钟操作监听器接口
     */
    public interface OnAlarmActionListener {
        void onAlarmEdit(AlarmEntity alarm);
        void onAlarmDelete(AlarmEntity alarm);
        void onAlarmToggle(AlarmEntity alarm, boolean enabled);
    }
} 