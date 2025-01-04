package com.schedule.assistant.util;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class DateUtilTest {
    
    private static final LocalDate NOW = LocalDate.of(2024, 1, 15);
    
    @Before
    public void setup() {
        // 设置固定的当前日期
        DateUtil.setCurrentDate(NOW);
    }
    
    @Test
    public void testFormatDate() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        String formattedDate = DateUtil.formatDate(date);
        assertEquals("2024-01-15", formattedDate);
    }
    
    @Test
    public void testParseDate() {
        String dateStr = "2024-01-15";
        LocalDate date = DateUtil.parseDate(dateStr);
        assertNotNull(date);
        assertEquals(2024, date.getYear());
        assertEquals(1, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }
    
    @Test
    public void testGetYearMonth() {
        String dateStr = "2024-01-15";
        String yearMonth = DateUtil.getYearMonth(dateStr);
        assertEquals("2024-01", yearMonth);
    }
    
    @Test
    public void testIsValidScheduleDate() {
        // 测试当天日期（应该有效）
        String today = DateUtil.formatDate(NOW);
        assertTrue("当天日期应该有效", DateUtil.isValidScheduleDate(today));
        
        // 测试6个月后的日期（应该有效）
        String futureDate = DateUtil.formatDate(NOW.plusMonths(6));
        assertTrue("6个月后的日期应该有效", DateUtil.isValidScheduleDate(futureDate));
        
        // 测试过去的日期（应该无效）
        String pastDate = DateUtil.formatDate(NOW.minusMonths(1));
        assertFalse("过去的日期应该无效", DateUtil.isValidScheduleDate(pastDate));
        
        // 测试超过12个月的日期（应该无效）
        String tooFutureDate = DateUtil.formatDate(NOW.plusMonths(13));
        assertFalse("超过12个月的日期应该无效", DateUtil.isValidScheduleDate(tooFutureDate));
        
        // 测试无效的日期格式
        assertFalse("无效的日期格式应该返回false", DateUtil.isValidScheduleDate("invalid-date"));
    }
    
    @Test
    public void testGetCurrentYearMonth() {
        String expected = String.format("%d-%02d", NOW.getYear(), NOW.getMonthValue());
        assertEquals("2024-01", expected);
    }
} 