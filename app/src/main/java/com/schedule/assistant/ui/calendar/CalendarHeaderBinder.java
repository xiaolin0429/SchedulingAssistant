package com.schedule.assistant.ui.calendar;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.kizitonwose.calendarview.model.CalendarMonth;
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;
import com.schedule.assistant.R;
import com.schedule.assistant.utils.LocaleHelper;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CalendarHeaderBinder implements MonthHeaderFooterBinder<CalendarHeaderBinder.MonthViewContainer> {
    private DateTimeFormatter formatter;
    private final Context context;

    public CalendarHeaderBinder(Context context) {
        this.context = context;
        updateFormatter();
    }

    /**
     * 更新日期格式化器
     * 根据当前语言环境设置正确的日期格式
     */
    private void updateFormatter() {
        try {
            String pattern = context.getString(R.string.date_format_year_month);
            // 使用 LocaleHelper 获取正确的 Locale
            Locale currentLocale = LocaleHelper.getCurrentLocale(context);
            formatter = DateTimeFormatter.ofPattern(pattern, currentLocale);
        } catch (Exception e) {
            // 如果获取格式失败，使用默认格式
            formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
        }
    }

    @NonNull
    @Override
    public MonthViewContainer create(@NonNull View view) {
        return new MonthViewContainer(view);
    }

    @Override
    public void bind(@NonNull MonthViewContainer container, @NonNull CalendarMonth month) {
        try {
            // 每次绑定时更新格式化器，以确保使用正确的语言
            updateFormatter();
            if (container.textView != null && month != null) {
                String formattedDate = month.getYearMonth().format(formatter);
                container.textView.setText(formattedDate);
            }
        } catch (Exception e) {
            // 如果发生异常，使用简单的格式显示
            if (container.textView != null && month != null) {
                container.textView.setText(month.getYearMonth().toString());
            }
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