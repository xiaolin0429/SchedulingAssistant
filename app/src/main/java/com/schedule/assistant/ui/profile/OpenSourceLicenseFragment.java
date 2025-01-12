package com.schedule.assistant.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.Navigation;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentOpenSourceLicenseBinding;

/**
 * 开源许可页面Fragment
 * 显示应用使用的开源库及其许可证信息
 */
public class OpenSourceLicenseFragment extends Fragment {
    private FragmentOpenSourceLicenseBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOpenSourceLicenseBinding.inflate(inflater, container, false);
        setupToolbar();
        return binding.getRoot();
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            activity.getSupportActionBar().setTitle(R.string.open_source_licenses);
        }
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLicenseList();
    }

    private void setupLicenseList() {
        // 添加开源库许可信息
        StringBuilder licenses = new StringBuilder();

        // Android Jetpack Libraries
        licenses.append("Android Jetpack Libraries\n")
                .append("Copyright 2018 The Android Open Source Project\n")
                .append("Apache License 2.0\n\n");

        // Material Components for Android
        licenses.append("Material Components for Android\n")
                .append("Copyright 2018 The Android Open Source Project\n")
                .append("Apache License 2.0\n\n");

        // Room Database
        licenses.append("Room Database\n")
                .append("Copyright 2017 The Android Open Source Project\n")
                .append("Apache License 2.0\n\n");

        // Navigation Component
        licenses.append("Navigation Component\n")
                .append("Copyright 2017 The Android Open Source Project\n")
                .append("Apache License 2.0\n\n");

        // ViewModel and LiveData
        licenses.append("ViewModel and LiveData\n")
                .append("Copyright 2017 The Android Open Source Project\n")
                .append("Apache License 2.0\n\n");

        binding.licenseContent.setText(licenses.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}