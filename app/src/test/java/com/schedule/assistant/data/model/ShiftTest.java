package com.schedule.assistant.data.model;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

public class ShiftTest {
    @Test
    public void testShiftCreation() {
        Date date = new Date();
        ShiftType type = ShiftType.DAY_SHIFT;
        Shift shift = new Shift(date, type);
        
        assertEquals(date, shift.getDate());
        assertEquals(type, shift.getType());
        assertNull(shift.getNote());
    }
    
    @Test
    public void testShiftNote() {
        Shift shift = new Shift(new Date(), ShiftType.NIGHT_SHIFT);
        String note = "Test note";
        shift.setNote(note);
        
        assertEquals(note, shift.getNote());
    }
    
    @Test
    public void testShiftId() {
        Shift shift = new Shift(new Date(), ShiftType.REST_DAY);
        long id = 1L;
        shift.setId(id);
        
        assertEquals(id, shift.getId());
    }
} 