package com.schedule.assistant.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.card.MaterialCardView;
import com.schedule.assistant.R;
import android.widget.TextView;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.dao.UserProfileDao;
import android.app.Activity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.os.Build;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import android.os.Environment;

public class ProfileMainFragment extends Fragment {

    private UserProfileDao userProfileDao;
    private TextView profileName;
    private TextView profileEmail;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_profileMainFragment_to_backupFragment);
                    } else {
                        showPermissionDialog();
                    }
                });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从系统设置页面返回时，检查权限是否已授予
        if (checkStoragePermission() && getView() != null) {
            View dataManagementView = getView().findViewById(R.id.data_management);
            if (dataManagementView != null && dataManagementView.getTag() instanceof Boolean &&
                    (Boolean) dataManagementView.getTag()) {
                dataManagementView.setTag(false);
                Navigation.findNavController(dataManagementView)
                        .navigate(R.id.action_profileMainFragment_to_backupFragment);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        AppDatabase db = AppDatabase.getDatabase(requireContext());
        userProfileDao = db.userProfileDao();

        MaterialCardView profileCard = view.findViewById(R.id.profile_card);
        profileCard.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_profileFragment));

        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);

        loadUserProfile();

        view.findViewById(R.id.app_settings).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_settingsFragment));

        view.findViewById(R.id.data_management).setOnClickListener(v -> {
            if (checkStoragePermission()) {
                Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_backupFragment);
            } else {
                v.setTag(true);
                requestStoragePermission();
            }
        });

        view.findViewById(R.id.help_feedback).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_helpFeedbackFragment));

        view.findViewById(R.id.about).setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_versionInfoFragment));

        return view;
    }

    private void loadUserProfile() {
        new Thread(() -> {
            UserProfile userProfile = userProfileDao.getUserProfile();
            Activity activity = getActivity();
            if (activity != null && !activity.isFinishing() && userProfile != null) {
                activity.runOnUiThread(() -> updateProfileUI(userProfile));
            }
        }).start();
    }

    private void showPermissionDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.storage_permission_required)
                .setMessage(R.string.permission_denied_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNeutralButton(R.string.request_permission, (dialog, which) -> requestStoragePermission())
                .show();
    }

    private void updateProfileUI(UserProfile userProfile) {
        if (profileName != null && profileEmail != null) {
            profileName.setText(
                    userProfile.getName() != null ? userProfile.getName() : getString(R.string.profile_name_not_set));
            profileEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail()
                    : getString(R.string.profile_email_not_set));
        }
    }
}