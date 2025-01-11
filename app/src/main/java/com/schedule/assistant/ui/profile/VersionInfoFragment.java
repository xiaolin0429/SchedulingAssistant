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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupVersionInfo();
        setupUpdateHistory();
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
                "0.2.0",
                "2025-01-10",
                getString(R.string.version_history_v020))
                + String.format(
                        getString(R.string.version_history_format),
                        "0.1.0",
                        "2025-01-04",
                        getString(R.string.version_history_v010));

        binding.updateHistoryContent.setText(history);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}