package com.schedule.assistant.ui.home;

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
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HomeViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private ShiftRepository repository;

    private HomeViewModel viewModel;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewModel = new HomeViewModel(application);
    }

    @Test
    public void testSelectDate() {
        Date date = new Date();
        MutableLiveData<Shift> shiftLiveData = new MutableLiveData<>();
        when(repository.getShiftByDate(any())).thenReturn(shiftLiveData);

        viewModel.selectDate(date);

        assertNotNull(viewModel.getSelectedDate().getValue());
        assertEquals(date, viewModel.getSelectedDate().getValue());
    }

    @Test
    public void testLoadMonthShifts() {
        Date month = new Date();
        List<Shift> shifts = Arrays.asList(
            new Shift(new Date(), ShiftType.DAY_SHIFT),
            new Shift(new Date(), ShiftType.NIGHT_SHIFT)
        );
        MutableLiveData<List<Shift>> shiftsLiveData = new MutableLiveData<>();
        shiftsLiveData.setValue(shifts);
        when(repository.getShiftsBetween(any(), any())).thenReturn(shiftsLiveData);

        viewModel.loadMonthShifts(month);

        assertNotNull(viewModel.getMonthShifts());
        assertEquals(shifts, viewModel.getMonthShifts().getValue());
    }
} 