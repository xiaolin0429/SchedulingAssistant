package com.schedule.assistant.data.model;

import static org.junit.Assert.*;

import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ShiftTest {
    @Test
    public void testShiftCreation() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ShiftType type = ShiftType.DAY_SHIFT;
        Shift shift = new Shift(date, type);

        assertEquals(date, shift.getDate());
        assertEquals(type, shift.getType());
        assertNull(shift.getStartTime());
        assertNull(shift.getEndTime());
    }

    @Test
    public void testShiftWithTimes() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String startTime = "09:00";
        String endTime = "17:00";
        Shift shift = new Shift(date, ShiftType.DAY_SHIFT, startTime, endTime);

        assertEquals(date, shift.getDate());
        assertEquals(startTime, shift.getStartTime());
        assertEquals(endTime, shift.getEndTime());
    }
} 