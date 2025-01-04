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

public class CalendarHeaderBinder implements MonthHeaderFooterBinder<CalendarHeaderBinder.MonthViewContainer> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE);

    @NonNull
    @Override
    public MonthViewContainer create(@NonNull View view) {
        return new MonthViewContainer(view);
    }

    @Override
    public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth month) {
        container.textView.setText(FORMATTER.format(month.getYearMonth()));
    }

    public static class MonthViewContainer extends ViewContainer {
        public final TextView textView;

        public MonthViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.monthText);
        }
    }
} 