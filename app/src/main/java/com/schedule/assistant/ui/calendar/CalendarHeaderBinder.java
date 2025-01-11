package com.schedule.assistant.ui.calendar;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.schedule.assistant.R;
import java.time.format.DateTimeFormatter;

public class CalendarHeaderBinder implements MonthHeaderFooterBinder<CalendarHeaderBinder.MonthViewContainer> {
    private final DateTimeFormatter formatter;

    public CalendarHeaderBinder(Context context) {
        String pattern = context.getString(R.string.month_year_format);
        formatter = DateTimeFormatter.ofPattern(pattern);
    }

    @NonNull
    @Override
    public MonthViewContainer create(@NonNull View view) {
        return new MonthViewContainer(view);
    }

    @Override
    public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth month) {
        if (container.textView != null) {
            container.textView.setText(month.getYearMonth().format(formatter));
        }
    }

    public static class MonthViewContainer extends ViewContainer {
        public final TextView textView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.headerTextView);
        }
    }
}