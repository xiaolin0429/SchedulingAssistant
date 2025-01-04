package com.schedule.assistant.ui.calendar;

import android.graphics.Typeface;
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
import com.schedule.assistant.data.entity.ShiftType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CalendarDayBinder implements DayBinder<CalendarDayBinder.DayViewContainer> {
    private final OnDayClickListener listener;
    private LocalDate selectedDate = null;
    private Map<String, Shift> shifts = new HashMap<>();

    public CalendarDayBinder(OnDayClickListener listener) {
        this.listener = listener;
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
            // 设置选中状态的背景和文字颜色
            if (day.getDate().equals(selectedDate)) {
                dayText.setBackgroundResource(R.drawable.selected_day_background);
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.white));
                dayText.setTypeface(null, Typeface.BOLD);
            } else if (day.getDate().equals(LocalDate.now())) {
                dayText.setBackgroundResource(0);
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.green));
                dayText.setTypeface(null, Typeface.BOLD);
            } else {
                dayText.setBackgroundResource(0);
                dayText.setTextColor(container.itemView.getContext().getColor(R.color.black));
                dayText.setTypeface(null, Typeface.NORMAL);
            }
            
            // 设置班次信息
            Shift shift = shifts.get(day.getDate().toString());
            if (shift != null) {
                shiftText.setVisibility(View.VISIBLE);
                shiftText.setText(container.itemView.getContext().getString(shift.getShiftType().getNameResId()));
                int backgroundColor;
                switch (shift.getShiftType()) {
                    case DAY_SHIFT:
                        backgroundColor = R.color.day_shift;
                        break;
                    case NIGHT_SHIFT:
                        backgroundColor = R.color.night_shift;
                        break;
                    case REST:
                        backgroundColor = R.color.rest_day;
                        break;
                    default:
                        backgroundColor = R.color.gray;
                        break;
                }
                shiftText.setBackgroundResource(backgroundColor);
            } else {
                shiftText.setVisibility(View.GONE);
            }
        } else {
            dayText.setVisibility(View.INVISIBLE);
            shiftText.setVisibility(View.GONE);
        }
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    public void updateShifts(Map<String, Shift> shifts) {
        this.shifts = shifts;
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