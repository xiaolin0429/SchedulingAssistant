package com.schedule.assistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.service.DataBackupService;
import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
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

        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.schedule.assistant.ACTION_SHOW_RESTORE_DIALOG");
        ContextCompat.registerReceiver(this, restoreDialogReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(restoreDialogReceiver);
    }

    private final BroadcastReceiver restoreDialogReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String backupFile = intent.getStringExtra("backup_file");
            if (backupFile != null) {
                showRestoreDialog(backupFile);
            }
        }
    };

    private void showRestoreDialog(String backupFile) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.backup_restore)
                .setMessage(R.string.backup_restore_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // 启动恢复过程
                    DataBackupService backupService = new DataBackupService(this);
                    new Thread(() -> {
                        try {
                            backupService.restoreData(new File(backupFile));
                            runOnUiThread(() -> Toast.makeText(this, R.string.backup_restore_success,
                                    Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            Log.e(TAG, "Error restoring data", e);
                            runOnUiThread(() -> Toast.makeText(this, R.string.backup_restore_failed,
                                    Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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