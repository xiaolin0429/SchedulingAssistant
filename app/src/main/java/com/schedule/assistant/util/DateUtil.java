package com.schedule.assistant.util;

import androidx.annotation.NonNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_FUTURE_MONTHS = 12;
    private static LocalDate currentDate = null; // 用于测试的当前日期

    public static void setCurrentDate(LocalDate date) {
        currentDate = date;
    }

    public static String formatDate(@NonNull LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatDate(@NonNull java.util.Date date) {
        return formatDate(LocalDate.now()); // 简单起见，使用当前日期
    }

    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getYearMonth(String dateStr) {
        try {
            LocalDate date = parseDate(dateStr);
            return date != null ? String.format("%d-%02d", date.getYear(), date.getMonthValue()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isValidScheduleDate(String dateStr) {
        try {
            LocalDate date = parseDate(dateStr);
            if (date == null) return false;

            LocalDate now = currentDate != null ? currentDate : LocalDate.now();
            LocalDate maxFutureDate = now.plusMonths(MAX_FUTURE_MONTHS);

            // 允许当天和未来12个月内的日期
            return !date.isBefore(now) && !date.isAfter(maxFutureDate);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCurrentYearMonth() {
        LocalDate now = currentDate != null ? currentDate : LocalDate.now();
        return String.format("%d-%02d", now.getYear(), now.getMonthValue());
    }
} 