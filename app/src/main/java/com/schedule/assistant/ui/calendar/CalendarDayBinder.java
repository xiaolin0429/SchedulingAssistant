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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import androidx.lifecycle.LifecycleOwner;
import com.schedule.assistant.ui.home.HomeViewModel;

public class CalendarDayBinder implements DayBinder<CalendarDayBinder.DayViewContainer> {
    private final OnDayClickListener listener;
    private LocalDate selectedDate = null;
    private LocalDate previousSelectedDate = null;
    private Map<String, Shift> shifts = new HashMap<>();
    private CalendarView calendarView;
    private final HomeViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public CalendarDayBinder(OnDayClickListener listener, HomeViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.listener = listener;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setCalendarView(CalendarView calendarView) {
        this.calendarView = calendarView;
    }

    @NonNull
    @Override
    public DayViewContainer create(@NonNull View view) {
        return new DayViewContainer(view, listener);
    }

    @Override
    public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
        container.day = day;
        TextView dayText = container.dayText;
        TextView shiftText = container.shiftText;

        dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

        if (day.getOwner() == DayOwner.THIS_MONTH) {
            dayText.setVisibility(View.VISIBLE);
            shiftText.setVisibility(View.VISIBLE);

            // 设置选中状态的背景和文字颜色
            if (day.getDate().equals(selectedDate)) {
                dayText.setBackgroundResource(R.drawable.selected_day_background);
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.white));
                dayText.setTypeface(null, Typeface.BOLD);
            } else if (day.getDate().equals(LocalDate.now())) {
                dayText.setBackgroundResource(0);
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.day_shift_color));
                dayText.setTypeface(null, Typeface.BOLD);
            } else {
                dayText.setBackgroundResource(0);
                int[] attrs = new int[] { android.R.attr.textColorPrimary };
                try (android.content.res.TypedArray ta = container.itemView.getContext()
                        .obtainStyledAttributes(attrs)) {
                    dayText.setTextColor(ta.getColor(0, container.itemView.getContext().getColor(R.color.black)));
                }
                dayText.setTypeface(null, Typeface.NORMAL);
            }

            // 设置班次信息
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
            dayText.setVisibility(View.INVISIBLE);
            shiftText.setVisibility(View.GONE);
        }
    }

    public void setSelectedDate(LocalDate date) {
        if (previousSelectedDate != null) {
            calendarView.notifyDateChanged(previousSelectedDate);
        }
        previousSelectedDate = selectedDate;
        selectedDate = date;
        if (previousSelectedDate != null) {
            calendarView.notifyDateChanged(previousSelectedDate);
        }
        if (selectedDate != null) {
            calendarView.notifyDateChanged(selectedDate);
        }
    }

    public void updateShifts(Map<String, Shift> shifts) {
        this.shifts = shifts != null ? shifts : new HashMap<>();
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