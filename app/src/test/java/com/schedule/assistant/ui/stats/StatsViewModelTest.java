package com.schedule.assistant.ui.stats;

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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StatsViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private ShiftRepository repository;

    private StatsViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewModel = new StatsViewModel(application);
    }

    @Test
    public void testSelectMonth() {
        Date month = new Date();
        List<Shift> shifts = Arrays.asList(
            new Shift(new Date(), ShiftType.DAY_SHIFT),
            new Shift(new Date(), ShiftType.DAY_SHIFT),
            new Shift(new Date(), ShiftType.NIGHT_SHIFT)
        );
        MutableLiveData<List<Shift>> shiftsLiveData = new MutableLiveData<>();
        shiftsLiveData.setValue(shifts);
        when(repository.getShiftsBetween(any(), any())).thenReturn(shiftsLiveData);

        viewModel.selectMonth(month);

        assertNotNull(viewModel.getSelectedMonth().getValue());
        assertEquals(month, viewModel.getSelectedMonth().getValue());
        assertNotNull(viewModel.getMonthShifts().getValue());
        assertEquals(shifts, viewModel.getMonthShifts().getValue());

        Map<ShiftType, Integer> typeCounts = viewModel.getShiftTypeCounts().getValue();
        assertNotNull(typeCounts);
        assertEquals(2, typeCounts.get(ShiftType.DAY_SHIFT).intValue());
        assertEquals(1, typeCounts.get(ShiftType.NIGHT_SHIFT).intValue());
    }
} 