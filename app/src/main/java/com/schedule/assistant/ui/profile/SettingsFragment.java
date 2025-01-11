package com.schedule.assistant.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.SavedStateHandle;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.slider.Slider;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.schedule.assistant.R;
import com.schedule.assistant.SchedulingAssistantApp;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.dao.UserSettingsDao;
import com.schedule.assistant.data.entity.UserSettings;

/**
 * 设置页面Fragment
 * 用于管理用户设置，包括主题、语言、通知等
 */
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private static final String KEY_SHOULD_POP_BACK = "should_pop_back";
    private UserSettingsDao userSettingsDao;
    private SwitchMaterial notificationSwitch;
    private Slider notificationTimeSlider;
    private MaterialRadioButton themeSystemRadio;
    private MaterialRadioButton themeLightRadio;
    private MaterialRadioButton themeDarkRadio;
    private MaterialRadioButton languageSystemRadio;
    private MaterialRadioButton languageChineseRadio;
    private MaterialRadioButton languageEnglishRadio;
    private SavedStateHandle savedStateHandle;

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedStateHandle = new SavedStateHandle();

        // 添加生命周期观察者来处理Fragment状态变化
        getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                // 在Fragment停止时检查是否需要清理导航栈
                if (isBottomNavigation() && isAdded() && !requireActivity().isChangingConfigurations()) {
                    savedStateHandle.set(KEY_SHOULD_POP_BACK, true);
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                // 在Fragment恢复时检查是否需要返回
                if (savedStateHandle.contains(KEY_SHOULD_POP_BACK) &&
                        savedStateHandle.get(KEY_SHOULD_POP_BACK) == Boolean.TRUE) {
                    savedStateHandle.remove(KEY_SHOULD_POP_BACK);
                    NavHostFragment.findNavController(this)
                            .popBackStack(R.id.profileMainFragment, false);
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // 初始化数据库
        AppDatabase db = AppDatabase.getDatabase(requireContext());
        userSettingsDao = db.userSettingsDao();

        // 初始化视图
        initializeViews(view);
        // 设置监听器
        setupListeners();
        // 加载设置
        loadSettings();

        // 设置返回键处理
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                });

        return view;
    }

    /**
     * 检查是否是通过底部导航栏切换
     */
    private boolean isBottomNavigation() {
        if (!isAdded())
            return false;
        View navView = requireActivity().findViewById(R.id.nav_view);
        return navView != null && navView.getVisibility() == View.VISIBLE;
    }

    /**
     * 初始化视图组件
     * 
     * @param view 根视图
     */
    private void initializeViews(View view) {
        // 初始化Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 初始化通知设置
        notificationSwitch = view.findViewById(R.id.notification_switch);
        notificationTimeSlider = view.findViewById(R.id.notification_time_slider);

        // 初始化主题设置
        themeSystemRadio = view.findViewById(R.id.theme_system);
        themeLightRadio = view.findViewById(R.id.theme_light);
        themeDarkRadio = view.findViewById(R.id.theme_dark);

        // 初始化语言设置
        languageSystemRadio = view.findViewById(R.id.language_system);
        languageChineseRadio = view.findViewById(R.id.language_chinese);
        languageEnglishRadio = view.findViewById(R.id.language_english);
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 通知开关监听器
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationSettings(isChecked);
            notificationTimeSlider.setEnabled(isChecked);
        });

        // 通知时间滑块监听器
        notificationTimeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                saveNotificationTime((int) value);
            }
        });

        // 主题设置监听器
        View.OnClickListener themeListener = v -> {
            int themeMode = 0;
            if (v.getId() == R.id.theme_light) {
                themeMode = 1;
            } else if (v.getId() == R.id.theme_dark) {
                themeMode = 2;
            }
            saveThemeSettings(themeMode);
        };

        themeSystemRadio.setOnClickListener(themeListener);
        themeLightRadio.setOnClickListener(themeListener);
        themeDarkRadio.setOnClickListener(themeListener);

        // 语言设置监听器
        View.OnClickListener languageListener = v -> {
            int languageMode = 0;
            if (v.getId() == R.id.language_chinese) {
                languageMode = 1;
            } else if (v.getId() == R.id.language_english) {
                languageMode = 2;
            }
            saveLanguageSettings(languageMode);
        };

        languageSystemRadio.setOnClickListener(languageListener);
        languageChineseRadio.setOnClickListener(languageListener);
        languageEnglishRadio.setOnClickListener(languageListener);
    }

    /**
     * 加载用户设置
     */
    private void loadSettings() {
        new Thread(() -> {
            try {
                UserSettings settings = userSettingsDao.getUserSettings();
                if (settings == null) {
                    settings = new UserSettings();
                    userSettingsDao.insert(settings);
                }

                UserSettings finalSettings = settings;
                requireActivity().runOnUiThread(() -> {
                    // 设置通知状态
                    notificationSwitch.setChecked(finalSettings.isNotificationEnabled());
                    notificationTimeSlider.setValue(finalSettings.getNotificationAdvanceTime());
                    notificationTimeSlider.setEnabled(finalSettings.isNotificationEnabled());

                    // 设置主题状态
                    switch (finalSettings.getThemeMode()) {
                        case 1:
                            themeLightRadio.setChecked(true);
                            break;
                        case 2:
                            themeDarkRadio.setChecked(true);
                            break;
                        default:
                            themeSystemRadio.setChecked(true);
                            break;
                    }

                    // 设置语言状态
                    switch (finalSettings.getLanguageMode()) {
                        case 1:
                            languageChineseRadio.setChecked(true);
                            break;
                        case 2:
                            languageEnglishRadio.setChecked(true);
                            break;
                        default:
                            languageSystemRadio.setChecked(true);
                            break;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading settings", e);
            }
        }).start();
    }

    /**
     * 保存通知设置
     * 
     * @param enabled 通知是否启用
     */
    private void saveNotificationSettings(boolean enabled) {
        new Thread(() -> {
            try {
                UserSettings settings = userSettingsDao.getUserSettings();
                if (settings != null) {
                    userSettingsDao.updateNotificationEnabled(settings.getId(), enabled);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving notification settings", e);
            }
        }).start();
    }

    /**
     * 保存通知提前时间
     * 
     * @param minutes 提前时间（分钟）
     */
    private void saveNotificationTime(int minutes) {
        new Thread(() -> {
            try {
                UserSettings settings = userSettingsDao.getUserSettings();
                if (settings != null) {
                    userSettingsDao.updateNotificationAdvanceTime(settings.getId(), minutes);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving notification time", e);
            }
        }).start();
    }

    /**
     * 保存主题设置并应用
     * 
     * @param themeMode 主题模式
     */
    private void saveThemeSettings(int themeMode) {
        new Thread(() -> {
            try {
                UserSettings settings = userSettingsDao.getUserSettings();
                if (settings != null) {
                    settings.setThemeMode(themeMode);
                    userSettingsDao.update(settings);
                    requireActivity().runOnUiThread(() -> {
                        ((SchedulingAssistantApp) requireActivity().getApplication()).updateSettings(settings);
                        // 重新创建Activity以应用新主题
                        requireActivity().recreate();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving theme settings", e);
            }
        }).start();
    }

    /**
     * 保存语言设置并应用
     * 
     * @param languageMode 语言模式
     */
    private void saveLanguageSettings(int languageMode) {
        new Thread(() -> {
            try {
                UserSettings settings = userSettingsDao.getUserSettings();
                if (settings != null) {
                    settings.setLanguageMode(languageMode);
                    userSettingsDao.update(settings);
                    requireActivity().runOnUiThread(() -> {
                        ((SchedulingAssistantApp) requireActivity().getApplication()).updateSettings(settings);
                        requireActivity().recreate();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving language settings", e);
            }
        }).start();
    }
}