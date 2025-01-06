package com.schedule.assistant.ui.stats;

import static org.junit.Assert.*;

import android.app.Application;
import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import com.schedule.assistant.data.repository.ShiftRepository;
import com.schedule.assistant.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class StatsViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private Context context;

    @Mock
    private ShiftRepository shiftRepository;

    private StatsViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private AutoCloseable mockitoCloseable;

    @Before
    public void setup() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        
        // 模拟Application的行为
        when(application.getApplicationContext()).thenReturn(context);
        
        viewModel = new StatsViewModel(application);
        
        // 使用反射设置repository
        Field repositoryField = StatsViewModel.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(viewModel, shiftRepository);
    }

    @Test
    public void testShiftTypeStats() throws InterruptedException {
        // 准备测试数据
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date twoDaysAgo = calendar.getTime();

        List<Shift> shifts = Arrays.asList(
            new Shift(dateFormat.format(today), ShiftType.DAY_SHIFT),
            new Shift(dateFormat.format(yesterday), ShiftType.DAY_SHIFT),
            new Shift(dateFormat.format(twoDaysAgo), ShiftType.NIGHT_SHIFT)
        );
        
        MutableLiveData<List<Shift>> shiftsLiveData = new MutableLiveData<>(shifts);
        when(shiftRepository.getShiftsBetween(any(), any())).thenReturn(shiftsLiveData);
        
        // 选择当前月份
        viewModel.selectMonth(today);
        
        // 等待LiveData更新
        List<Shift> monthShifts = LiveDataTestUtil.getOrAwaitValue(viewModel.getMonthShifts());
        assertNotNull(monthShifts);
        assertEquals(3, monthShifts.size());
        
        // 等待统计数据更新
        Map<ShiftType, Integer> typeCounts = LiveDataTestUtil.getOrAwaitValue(viewModel.getShiftTypeCounts());
        assertNotNull(typeCounts);
        
        // 验证统计结果
        assertEquals(Integer.valueOf(2), typeCounts.get(ShiftType.DAY_SHIFT));
        assertEquals(Integer.valueOf(1), typeCounts.get(ShiftType.NIGHT_SHIFT));
        assertNull(typeCounts.get(ShiftType.REST_DAY));
    }

    @org.junit.After
    public void releaseMocks() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }
} 