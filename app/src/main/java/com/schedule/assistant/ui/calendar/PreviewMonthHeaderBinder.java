package com.schedule.assistant.ui.calendar;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.schedule.assistant.R;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import android.content.Context;
import android.widget.LinearLayout;

public class PreviewMonthHeaderBinder implements MonthHeaderFooterBinder<PreviewMonthHeaderBinder.MonthViewContainer> {
    private DateTimeFormatter getMonthFormatter(Context context) {
        String pattern = context.getString(R.string.date_format_year_month);
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault());
    }

    @NonNull
    @Override
    public MonthViewContainer create(@NonNull View view) {
        return new MonthViewContainer(view);
    }

    @Override
    public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth month) {
        container.textView.setText(getMonthFormatter(container.itemView.getContext()).format(month.getYearMonth()));

        // 设置星期标签
        Context context = container.itemView.getContext();
        ((TextView) container.daysOfWeekContainer.getChildAt(0)).setText(context.getString(R.string.calendar_monday));
        ((TextView) container.daysOfWeekContainer.getChildAt(1)).setText(context.getString(R.string.calendar_tuesday));
        ((TextView) container.daysOfWeekContainer.getChildAt(2))
                .setText(context.getString(R.string.calendar_wednesday));
        ((TextView) container.daysOfWeekContainer.getChildAt(3)).setText(context.getString(R.string.calendar_thursday));
        ((TextView) container.daysOfWeekContainer.getChildAt(4)).setText(context.getString(R.string.calendar_friday));
        ((TextView) container.daysOfWeekContainer.getChildAt(5)).setText(context.getString(R.string.calendar_saturday));
        ((TextView) container.daysOfWeekContainer.getChildAt(6)).setText(context.getString(R.string.calendar_sunday));
    }

    public static class MonthViewContainer extends ViewContainer {
        public final TextView textView;
        public final LinearLayout daysOfWeekContainer;
        public final View itemView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            this.itemView = view;
            textView = view.findViewById(R.id.headerTextView);
            daysOfWeekContainer = view.findViewById(R.id.daysOfWeekContainer);
        }
    }
}