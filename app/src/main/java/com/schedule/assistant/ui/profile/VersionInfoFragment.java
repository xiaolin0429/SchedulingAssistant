package com.schedule.assistant.ui.profile;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import com.schedule.assistant.databinding.FragmentVersionInfoBinding;

public class VersionInfoFragment extends Fragment {
    private static final String TAG = "VersionInfoFragment";
    private FragmentVersionInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentVersionInfoBinding.inflate(inflater, container, false);
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
            activity.getSupportActionBar().setTitle(R.string.version_info);
        }
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVersionInfo();
        setupUpdateHistory();
        setupClickListeners();
    }

    private void setupVersionInfo() {
        try {
            PackageManager packageManager = requireContext().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(requireContext().getPackageName(), 0);

            String versionName = String.format(getString(R.string.version_name_format), packageInfo.versionName);
            String versionCode = String.format(getString(R.string.version_code_format), packageInfo.versionCode);

            binding.versionName.setText(String.format("%s\n%s", versionName, versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting package info", e);
        }
    }

    private void setupUpdateHistory() {
        String history = String.format(
                getString(R.string.version_history_format),
                "1.0.9",
                "2025-01-12",
                getString(R.string.version_history_v030))
                + String.format(
                        getString(R.string.version_history_format),
                        "1.0.8",
                        "2025-01-10",
                        getString(R.string.version_history_v020));

        binding.updateHistoryContent.setText(history);
    }

    private void setupClickListeners() {
        binding.openSourceLicensesButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_versionInfoFragment_to_openSourceLicenseFragment));

        binding.developerInfoButton.setOnClickListener(v -> Navigation.findNavController(v)
                .navigate(R.id.action_versionInfoFragment_to_developerInfoFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}