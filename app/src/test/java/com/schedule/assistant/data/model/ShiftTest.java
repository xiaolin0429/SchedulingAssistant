package com.schedule.assistant.data.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.entity.Shift;

public class ShiftTest {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Test
    public void testShiftCreation() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        ShiftType type = ShiftType.DAY_SHIFT;
        Shift shift = new Shift(date, type);
        
        assertEquals(date, shift.getDate());
        assertEquals(type, shift.getShiftType());
        assertNull(shift.getNote());
    }
    
    @Test
    public void testShiftNote() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        Shift shift = new Shift(date, ShiftType.NIGHT_SHIFT);
        String note = "Test note";
        shift.setNote(note);
        
        assertEquals(note, shift.getNote());
    }
    
    @Test
    public void testShiftId() {
        String date = LocalDate.now().format(DATE_FORMATTER);
        Shift shift = new Shift(date, ShiftType.REST);
        long updateTime = System.currentTimeMillis();
        shift.setUpdateTime(updateTime);
        
        assertEquals(updateTime, shift.getUpdateTime());
    }
} 