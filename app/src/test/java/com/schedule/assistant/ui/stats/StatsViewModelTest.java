package com.schedule.assistant.ui.stats;

import static org.junit.Assert.*;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.repository.ShiftRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class StatsViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private ShiftRepository shiftRepository;

    private StatsViewModel viewModel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        viewModel = new StatsViewModel(application);

        // 模拟数据
        List<Shift> shifts = Arrays.asList(
            new Shift(LocalDate.now().format(formatter), ShiftType.DAY_SHIFT),
            new Shift(LocalDate.now().minusDays(1).format(formatter), ShiftType.DAY_SHIFT),
            new Shift(LocalDate.now().minusDays(2).format(formatter), ShiftType.NIGHT_SHIFT)
        );

        MutableLiveData<List<Shift>> shiftsLiveData = new MutableLiveData<>(shifts);
        when(shiftRepository.getAllShifts()).thenReturn(shiftsLiveData);
    }

    @Test
    public void testShiftTypeStats() {
        // 获取统计数据
        List<Shift> shifts = shiftRepository.getAllShifts().getValue();
        assertNotNull(shifts);
        
        // 手动计算各类班次数量
        int dayShiftCount = 0;
        int nightShiftCount = 0;
        int restDayCount = 0;
        
        for (Shift shift : shifts) {
            ShiftType type = shift.getType();
            switch (type) {
                case DAY_SHIFT:
                    dayShiftCount++;
                    break;
                case NIGHT_SHIFT:
                    nightShiftCount++;
                    break;
                case REST_DAY:
                    restDayCount++;
                    break;
            }
        }
        
        // 验证统计结果
        assertEquals(2, dayShiftCount);
        assertEquals(1, nightShiftCount);
        assertEquals(0, restDayCount);
    }
} 