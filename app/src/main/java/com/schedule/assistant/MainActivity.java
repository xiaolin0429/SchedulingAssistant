package com.schedule.assistant;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void attachBaseContext(@NonNull Context newBase) {
        Locale locale = SchedulingAssistantApp.getCurrentLocale();
        if (locale == null) {
            super.attachBaseContext(newBase);
            return;
        }

        Context context = createConfigurationContext(newBase, locale);
        super.attachBaseContext(context);
    }

    private Context createConfigurationContext(Context context, Locale locale) {
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(config);
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            Locale locale = SchedulingAssistantApp.getCurrentLocale();
            if (locale != null) {
                overrideConfiguration.setLocales(new LocaleList(locale));
            }
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigation();
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // 设置底部导航栏点击监听
            bottomNavigationView.setOnItemSelectedListener(item -> {
                // 在切换到新的导航项之前，清除当前导航栈
                int currentDestinationId = navController.getCurrentDestination() != null
                        ? navController.getCurrentDestination().getId()
                        : 0;

                if (currentDestinationId != item.getItemId()) {
                    // 清除导航栈到根目标
                    navController.popBackStack(R.id.navigation_home, false);
                    // 导航到选中的目标
                    navController.navigate(item.getItemId());
                }
                return true;
            });
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Locale locale = SchedulingAssistantApp.getCurrentLocale();
        if (locale != null) {
            Configuration configuration = new Configuration(newConfig);
            configuration.setLocales(new LocaleList(locale));
            applyOverrideConfiguration(configuration);
            recreate();
        }
    }
}