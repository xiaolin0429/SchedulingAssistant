package com.schedule.assistant.ui.calendar;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.viewmodel.AutoScheduleViewModel;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import androidx.lifecycle.LifecycleOwner;
import android.os.Handler;
import android.os.Looper;

public class PreviewCalendarDayBinder implements DayBinder<PreviewCalendarDayBinder.DayViewContainer> {
    private final OnDayClickListener onDayClickListener;
    private final AutoScheduleViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;
    private Map<String, Shift> shifts = new HashMap<>();
    private CalendarView calendarView;
    private LocalDate startDate;
    private LocalDate endDate;

    public PreviewCalendarDayBinder(OnDayClickListener onDayClickListener,
            AutoScheduleViewModel viewModel,
            LifecycleOwner lifecycleOwner) {
        this.onDayClickListener = onDayClickListener;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        if (calendarView != null) {
            calendarView.notifyCalendarChanged();
        }
    }

    public void setCalendarView(CalendarView calendarView) {
        this.calendarView = calendarView;
    }

    public void updateShifts(Map<String, Shift> shifts) {
        this.shifts = shifts != null ? shifts : new HashMap<>();
        if (calendarView != null) {
            // 确保在主线程中刷新日历视图
            new Handler(Looper.getMainLooper()).post(() -> calendarView.notifyCalendarChanged());
        }
    }

    @NonNull
    @Override
    public DayViewContainer create(@NonNull View view) {
        return new DayViewContainer(view, onDayClickListener);
    }

    @Override
    public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
        container.day = day;
        TextView dayText = container.dayText;
        TextView shiftText = container.shiftText;

        dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

        if (day.getOwner() == DayOwner.THIS_MONTH) {
            dayText.setVisibility(View.VISIBLE);

            // 检查日期是否在选定范围内
            boolean isInRange = (startDate == null || !day.getDate().isBefore(startDate))
                    && (endDate == null || !day.getDate().isAfter(endDate));

            // 设置文字颜色
            if (day.getDate().equals(LocalDate.now())) {
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.day_shift_color));
                dayText.setTypeface(null, Typeface.BOLD);
            } else {
                int[] attrs = new int[] { android.R.attr.textColorPrimary };
                try (android.content.res.TypedArray ta = container.itemView.getContext()
                        .obtainStyledAttributes(attrs)) {
                    int textColor = ta.getColor(0, container.itemView.getContext().getColor(R.color.black));
                    // 如果不在日期范围内，使用半透明颜色
                    dayText.setTextColor(isInRange ? textColor : (textColor & 0x00FFFFFF | 0x66000000));
                }
                dayText.setTypeface(null, Typeface.NORMAL);
            }

            // 只在选定范围内显示班次信息
            if (isInRange) {
                Shift shift = shifts.get(day.getDate().toString());
                if (shift != null) {
                    shiftText.setVisibility(View.VISIBLE);

                    // 根据shiftTypeId获取对应的ShiftTypeEntity
                    viewModel.getShiftTypeById(shift.getShiftTypeId()).observe(lifecycleOwner, shiftType -> {
                        if (shiftType != null) {
                            shiftText.setText(shiftType.getName());
                            // 创建动态背景
                            GradientDrawable background = new GradientDrawable();
                            background.setColor(shiftType.getColor());
                            background.setCornerRadius(container.itemView.getContext().getResources()
                                    .getDimensionPixelSize(R.dimen.shift_type_corner_radius));
                            // 设置内边距
                            shiftText.setPadding(
                                    container.itemView.getContext().getResources()
                                            .getDimensionPixelSize(R.dimen.shift_type_padding_horizontal),
                                    container.itemView.getContext().getResources()
                                            .getDimensionPixelSize(R.dimen.shift_type_padding_vertical),
                                    container.itemView.getContext().getResources()
                                            .getDimensionPixelSize(R.dimen.shift_type_padding_horizontal),
                                    container.itemView.getContext().getResources()
                                            .getDimensionPixelSize(R.dimen.shift_type_padding_vertical));
                            // 设置背景
                            shiftText.setBackground(background);
                            shiftText.setTextColor(container.itemView.getContext().getColor(R.color.white));
                        }
                    });
                } else {
                    shiftText.setVisibility(View.GONE);
                }
            } else {
                shiftText.setVisibility(View.GONE);
            }
        } else {
            dayText.setVisibility(View.INVISIBLE);
            shiftText.setVisibility(View.GONE);
        }
    }

    public static class DayViewContainer extends ViewContainer {
        public CalendarDay day;
        public final TextView dayText;
        public final TextView shiftText;
        public final View itemView;

        public DayViewContainer(@NonNull View view, OnDayClickListener listener) {
            super(view);
            this.itemView = view;
            this.dayText = view.findViewById(R.id.calendarDayText);
            this.shiftText = view.findViewById(R.id.shiftTypeText);
            view.setOnClickListener(v -> {
                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    listener.onDayClick(day);
                }
            });
        }
    }

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }
}