package com.schedule.assistant;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testBottomNavigationVisibility() {
        // 检查底部导航栏是否可见
        Espresso.onView(ViewMatchers.withId(R.id.nav_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationToShiftTemplateFragment() {
        // 点击班次模板导航项
        Espresso.onView(ViewMatchers.withId(R.id.navigation_shift_template))
                .perform(ViewActions.click());

        // 验证班次模板Fragment是否显示
        Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationToStatsFragment() {
        // 点击统计导航项
        Espresso.onView(ViewMatchers.withId(R.id.navigation_stats))
                .perform(ViewActions.click());

        // 验证统计Fragment是否显示
        Espresso.onView(ViewMatchers.withId(R.id.pieChart))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationToAlarmFragment() {
        // 点击闹钟导航项
        Espresso.onView(ViewMatchers.withId(R.id.navigation_alarm))
                .perform(ViewActions.click());

        // 验证闹钟Fragment是否显示
        // TODO: 添加闹钟Fragment的视图验证
    }

    @Test
    public void testNavigationToProfileFragment() {
        // 点击我的导航项
        Espresso.onView(ViewMatchers.withId(R.id.navigation_profile))
                .perform(ViewActions.click());

        // 验证我的Fragment是否显示
        // TODO: 添加我的Fragment的视图验证
    }

    @Test
    public void testHomeFragmentCalendarDisplay() {
        // 验证日历视图是否显示
        Espresso.onView(ViewMatchers.withId(R.id.calendarView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
} 