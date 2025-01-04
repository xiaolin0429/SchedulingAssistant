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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private AutoCloseable mockitoCloseable;

    @Before
    public void setup() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        viewModel = new HomeViewModel(application);
    }

    @Test
    public void testSelectDate() {
        LocalDate date = LocalDate.now();
        MutableLiveData<Shift> shiftLiveData = new MutableLiveData<>();
        when(repository.getShiftByDate(date.format(formatter))).thenReturn(shiftLiveData);

        viewModel.selectDate(date);

        assertNotNull(viewModel.getSelectedShift().getValue());
        verify(repository).getShiftByDate(date.format(formatter));
    }

    @Test
    public void testLoadMonthShifts() {
        LocalDate today = LocalDate.now();
        List<Shift> shifts = Arrays.asList(
            new Shift(today.format(formatter), ShiftType.DAY_SHIFT),
            new Shift(today.plusDays(1).format(formatter), ShiftType.NIGHT_SHIFT)
        );
        MutableLiveData<List<Shift>> shiftsLiveData = new MutableLiveData<>();
        shiftsLiveData.setValue(shifts);
        when(repository.getShiftsBetween(any(), any())).thenReturn(shiftsLiveData);

        YearMonth currentMonth = YearMonth.from(today);
        viewModel.loadMonthShifts(currentMonth);

        assertNotNull(viewModel.getMonthShifts().getValue());
        assertEquals(shifts, viewModel.getMonthShifts().getValue());
    }

    @org.junit.After
    public void releaseMocks() throws Exception {
        if (mockitoCloseable != null) {
            mockitoCloseable.close();
        }
    }
} 