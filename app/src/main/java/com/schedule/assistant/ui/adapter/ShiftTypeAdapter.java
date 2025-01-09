package com.schedule.assistant.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.ItemShiftTypeBinding;
import java.util.Objects;

public class ShiftTypeAdapter extends ListAdapter<ShiftTypeEntity, ShiftTypeAdapter.ViewHolder> {
    private static final String TAG = "ShiftTypeAdapter";
    private final OnShiftTypeActionListener listener;
    private ViewHolder currentOpenedItem = null;
    private static final long DEBOUNCE_TIME = 300L;
    private long lastClickTime = 0;

    public ShiftTypeAdapter(OnShiftTypeActionListener listener) {
        super(new DiffUtil.ItemCallback<>() {
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
                        oldItem.getColor() == newItem.getColor();
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

    public void closeOpenedItem() {
        // 用于调试按钮状态变化，记录当前是否有打开的项目
        //Log.d(TAG, "closeOpenedItem called, currentOpenedItem: " + (currentOpenedItem != null));
        if (currentOpenedItem != null) {
            currentOpenedItem.hideButtons();
            currentOpenedItem = null;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTypeBinding binding;

        ViewHolder(ItemShiftTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // 设置整个卡片的点击事件
            binding.contentLayout.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    // 用于调试防抖动逻辑
                    //Log.d(TAG, "contentLayout click debounced");
                    return;
                }
                lastClickTime = currentTime;

                if (currentOpenedItem == this) {
                    // 用于调试按钮状态切换
                    //Log.d(TAG, "contentLayout closing current item");
                    hideButtons();
                    currentOpenedItem = null;
                } else {
                    // 用于调试按钮状态切换
                    //Log.d(TAG, "contentLayout opening new item");
                    if (currentOpenedItem != null) {
                        currentOpenedItem.hideButtons();
                    }
                    showButtons();
                    currentOpenedItem = this;
                }
            });

            // 设置编辑按钮点击事件
            binding.editButton.setOnClickListener(v -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    return;
                }
                lastClickTime = currentTime;

                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShiftTypeEdit(getItem(position));
                    hideButtons();
                    currentOpenedItem = null;
                }
            });

            // 设置删除按钮点击事件
            binding.deleteButton.setOnClickListener(v -> {
                // 用于调试删除按钮点击事件的触发
                //Log.d(TAG, "deleteButton clicked");
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DEBOUNCE_TIME) {
                    // 用于调试防抖动逻辑
                    //Log.d(TAG, "deleteButton click debounced");
                    return;
                }
                lastClickTime = currentTime;

                int position = getBindingAdapterPosition();
                // 用于调试项目位置
                //Log.d(TAG, "deleteButton position: " + position);
                if (position != RecyclerView.NO_POSITION) {
                    ShiftTypeEntity shiftType = getItem(position);
                    // 用于调试班次类型信息和默认状态
                    //Log.d(TAG, "deleteButton shiftType: " + shiftType.getName() + ", isDefault: " + shiftType.isDefault());
                    if (shiftType.isDefault()) {
                        // 用于调试默认班次类型的处理
                        //Log.d(TAG, "deleteButton showing toast for default type");
                        android.widget.Toast.makeText(
                            v.getContext(),
                            R.string.cannot_delete_default_shift_type,
                            android.widget.Toast.LENGTH_SHORT
                        ).show();
                        hideButtons();
                        currentOpenedItem = null;
                        return;
                    }
                    // 用于调试删除操作的触发
                    //Log.d(TAG, "deleteButton calling listener.onShiftTypeDelete");
                    listener.onShiftTypeDelete(shiftType);
                    hideButtons();
                    currentOpenedItem = null;
                }
            });
        }

        void bind(ShiftTypeEntity shiftType) {
            // 用于调试数据绑定过程
            //Log.d(TAG, "binding shiftType: " + shiftType.getName() + ", isDefault: " + shiftType.isDefault());
            binding.typeName.setText(shiftType.getName());
            if (shiftType.getStartTime() != null && shiftType.getEndTime() != null) {
                binding.typeTime.setText(String.format("%s - %s", 
                    shiftType.getStartTime(), shiftType.getEndTime()));
                binding.typeTime.setVisibility(View.VISIBLE);
            } else {
                binding.typeTime.setVisibility(View.GONE);
            }

            binding.colorIndicator.setBackgroundColor(shiftType.getColor());
            
            // 只设置透明度，不禁用按钮
            binding.deleteButton.setAlpha(shiftType.isDefault() ? 0.5f : 1.0f);
            // 用于调试删除按钮状态
            //Log.d(TAG, "deleteButton alpha: " + (shiftType.isDefault() ? 0.5f : 1.0f));

            // 重置按钮状态
            hideButtons();
            if (currentOpenedItem == this) {
                currentOpenedItem = null;
            }
        }

        void showButtons() {
            binding.buttonLayout.setVisibility(View.VISIBLE);
        }

        void hideButtons() {
            binding.buttonLayout.setVisibility(View.GONE);
        }
    }

    public interface OnShiftTypeActionListener {
        void onShiftTypeEdit(ShiftTypeEntity shiftType);
        void onShiftTypeDelete(ShiftTypeEntity shiftType);
    }
}