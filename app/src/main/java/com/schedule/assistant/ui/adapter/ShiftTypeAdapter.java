package com.schedule.assistant.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.data.entity.ShiftTypeEntity;
import com.schedule.assistant.databinding.ItemShiftTypeBinding;
import java.util.Objects;

public class ShiftTypeAdapter extends ListAdapter<ShiftTypeEntity, ShiftTypeAdapter.ViewHolder> {
    private final OnShiftTypeActionListener listener;
    private ItemTouchHelper touchHelper;
    private static final float SWIPE_THRESHOLD = 0.5f;
    private ViewHolder currentOpenedItem = null;  // 跟踪当前打开的项目

    public ShiftTypeAdapter(OnShiftTypeActionListener listener) {
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
                       oldItem.getColor() == newItem.getColor();
            }
        });
        this.listener = listener;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
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
        
        // 添加前景视图的点击监听，用于关闭其他打开的项目
        holder.binding.foregroundView.setOnClickListener(v -> {
            closeOpenedItem();
        });
    }

    public void closeOpenedItem() {
        if (currentOpenedItem != null) {
            currentOpenedItem.animateSwipeReset();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemShiftTypeBinding binding;
        private float currentSwipeX = 0f;
        private boolean isSwipeOpen = false;
        private ValueAnimator swipeAnimator;

        ViewHolder(ItemShiftTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.editButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShiftTypeEdit(getItem(position));
                    animateSwipeReset();
                }
            });

            binding.deleteButton.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onShiftTypeDelete(getItem(position));
                    animateSwipeReset();
                }
            });
        }

        void bind(ShiftTypeEntity shiftType) {
            binding.typeName.setText(shiftType.getName());
            if (shiftType.getStartTime() != null && shiftType.getEndTime() != null) {
                binding.typeTime.setText(String.format("%s - %s", 
                    shiftType.getStartTime(), shiftType.getEndTime()));
                binding.typeTime.setVisibility(View.VISIBLE);
            } else {
                binding.typeTime.setVisibility(View.GONE);
            }

            binding.colorIndicator.setBackgroundColor(shiftType.getColor());
            
            // 如果是默认班次，禁用删除按钮
            binding.deleteButton.setEnabled(!shiftType.isDefault());
            binding.deleteButton.setAlpha(shiftType.isDefault() ? 0.5f : 1.0f);

            // 重置滑动状态
            resetSwipe();
        }

        void onItemSwiped(float dX) {
            if (dX < 0) { // 只处理向左滑动
                // 如果有其他打开的项目，先关闭它
                if (currentOpenedItem != null && currentOpenedItem != this) {
                    currentOpenedItem.animateSwipeReset();
                }

                currentSwipeX = dX;
                float swipeThreshold = -itemView.getWidth() * SWIPE_THRESHOLD;
                
                // 更新前景视图位置
                binding.foregroundView.setTranslationX(dX);
                
                // 根据滑动距离显示/隐藏按钮
                if (dX <= swipeThreshold && !isSwipeOpen) {
                    binding.backgroundView.setVisibility(View.VISIBLE);
                    isSwipeOpen = true;
                    currentOpenedItem = this;
                } else if (dX > swipeThreshold && isSwipeOpen) {
                    binding.backgroundView.setVisibility(View.GONE);
                    isSwipeOpen = false;
                    if (currentOpenedItem == this) {
                        currentOpenedItem = null;
                    }
                }
            }
        }

        void animateSwipeReset() {
            if (swipeAnimator != null && swipeAnimator.isRunning()) {
                swipeAnimator.cancel();
            }

            swipeAnimator = ValueAnimator.ofFloat(currentSwipeX, 0f);
            swipeAnimator.setDuration(200); // 200ms的动画时长
            swipeAnimator.setInterpolator(new DecelerateInterpolator());
            swipeAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                binding.foregroundView.setTranslationX(value);
            });
            swipeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetSwipe();
                }
            });
            swipeAnimator.start();
        }

        void resetSwipe() {
            if (swipeAnimator != null && swipeAnimator.isRunning()) {
                swipeAnimator.cancel();
            }
            currentSwipeX = 0f;
            isSwipeOpen = false;
            binding.foregroundView.setTranslationX(0f);
            binding.backgroundView.setVisibility(View.GONE);
            if (currentOpenedItem == this) {
                currentOpenedItem = null;
            }
        }

        float getCurrentSwipeX() {
            return currentSwipeX;
        }
    }

    public interface OnShiftTypeActionListener {
        void onShiftTypeEdit(ShiftTypeEntity shiftType);
        void onShiftTypeDelete(ShiftTypeEntity shiftType);
    }

    public static class SwipeController extends ItemTouchHelper.SimpleCallback {
        private final ShiftTypeAdapter adapter;

        public SwipeController(ShiftTypeAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            this.adapter = adapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // 不需要实现，我们使用自定义的滑动逻辑
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (viewHolder instanceof ViewHolder) {
                ViewHolder holder = (ViewHolder) viewHolder;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // 如果开始新的滑动，关闭其他打开的项目
                    if (isCurrentlyActive && holder != ((ShiftTypeAdapter) recyclerView.getAdapter()).currentOpenedItem) {
                        ((ShiftTypeAdapter) recyclerView.getAdapter()).closeOpenedItem();
                    }
                    float swipeThreshold = -viewHolder.itemView.getWidth() * SWIPE_THRESHOLD;
                    float newX = Math.max(swipeThreshold, dX);
                    holder.onItemSwiped(newX);
                }
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (viewHolder instanceof ViewHolder) {
                ViewHolder holder = (ViewHolder) viewHolder;
                // 如果松手时的位置超过阈值，保持打开状态，否则关闭
                float swipeThreshold = -viewHolder.itemView.getWidth() * SWIPE_THRESHOLD;
                if (holder.getCurrentSwipeX() > swipeThreshold) {
                    holder.animateSwipeReset();
                }
            }
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return SWIPE_THRESHOLD;
        }
    }
}