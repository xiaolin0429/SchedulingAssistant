package com.schedule.assistant.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.adapter.BackupHistoryAdapter;
import com.schedule.assistant.databinding.FragmentBackupBinding;
import com.schedule.assistant.model.BackupHistoryItem;
import com.schedule.assistant.service.DataBackupService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Environment;

/**
 * 数据备份界面
 * 实现数据备份和恢复功能
 */
public class BackupFragment extends Fragment implements BackupHistoryAdapter.OnBackupItemClickListener {
    private FragmentBackupBinding binding;
    private DataBackupService backupService;
    private BackupHistoryAdapter adapter;
    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public BackupFragment() {
        // 初始化权限请求启动器
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startBackup();
                    } else {
                        showStoragePermissionDeniedDialog();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentBackupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        setupBackupService();
        setupRecyclerView();
        setupBackupButton();
        loadBackupHistory();
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(binding.toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setTitle(R.string.backup_restore);
        }
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupBackupService() {
        backupService = new DataBackupService(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new BackupHistoryAdapter(requireContext(), this);
        binding.backupHistoryList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.backupHistoryList.setAdapter(adapter);
    }

    private void setupBackupButton() {
        binding.startBackupButton.setOnClickListener(v -> checkStoragePermissionAndBackup());
        binding.clearAllBackupsButton.setOnClickListener(v -> showClearAllBackupsDialog());
        binding.clearCacheButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_backupFragment_to_clearCacheFragment));
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

    private void checkStoragePermissionAndBackup() {
        if (checkStoragePermission()) {
            startBackup();
        } else {
            requestStoragePermission();
        }
    }

    private void showStoragePermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.storage_permission_required)
                .setMessage(R.string.grant_storage_permission)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    // TODO: 打开应用设置页面
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startBackup() {
        showBackupProgress(true);
        new Thread(() -> {
            String backupPath = backupService.backupData();
            requireActivity().runOnUiThread(() -> {
                showBackupProgress(false);
                if (backupPath != null) {
                    Toast.makeText(requireContext(), R.string.backup_success, Toast.LENGTH_SHORT).show();
                    loadBackupHistory();
                } else {
                    Toast.makeText(requireContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showBackupProgress(boolean show) {
        binding.backupProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.backupStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.backupStatus.setText(R.string.backup_in_progress);
        binding.startBackupButton.setEnabled(!show);
    }

    private void loadBackupHistory() {
        new Thread(() -> {
            List<BackupHistoryItem> items = new ArrayList<>();

            // 检查公共目录
            File publicBackupDir = new File(Environment.getExternalStorageDirectory(),
                    DataBackupService.BACKUP_FOLDER);
            if (publicBackupDir.exists()) {
                File[] publicFiles = publicBackupDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (publicFiles != null) {
                    for (File file : publicFiles) {
                        items.add(new BackupHistoryItem(file));
                    }
                }
            }

            // 检查私有目录
            File privateBackupDir = new File(requireContext().getExternalFilesDir(null),
                    DataBackupService.BACKUP_FOLDER);
            if (privateBackupDir.exists()) {
                File[] privateFiles = privateBackupDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (privateFiles != null) {
                    for (File file : privateFiles) {
                        items.add(new BackupHistoryItem(file));
                    }
                }
            }

            // 按备份时间降序排序
            items.sort((o1, o2) -> Long.compare(o2.getBackupTime(), o1.getBackupTime()));

            requireActivity().runOnUiThread(() -> {
                adapter.updateItems(items);
                binding.backupHistoryCard.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }).start();
    }

    @Override
    public void onRestoreClick(BackupHistoryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.backup_restore)
                .setMessage(R.string.backup_restore_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> startRestore(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startRestore(BackupHistoryItem item) {
        showRestoreProgress(true);
        new Thread(() -> {
            boolean success = backupService.restoreData(item.getBackupFile());
            requireActivity().runOnUiThread(() -> {
                showRestoreProgress(false);
                if (success) {
                    Toast.makeText(requireContext(), R.string.backup_restore_success, Toast.LENGTH_SHORT).show();
                    // 恢复成功后返回上一页
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    Toast.makeText(requireContext(), R.string.backup_restore_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showRestoreProgress(boolean show) {
        binding.backupProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.backupStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.backupStatus.setText(R.string.backup_restore_in_progress);
        binding.startBackupButton.setEnabled(!show);
        binding.backupHistoryList.setEnabled(!show);
    }

    @Override
    public void onDeleteClick(BackupHistoryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.backup_delete)
                .setMessage(R.string.backup_delete_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> deleteBackup(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteBackup(BackupHistoryItem item) {
        if (item.getBackupFile().delete()) {
            adapter.removeItem(item);
            Toast.makeText(requireContext(), R.string.backup_delete_success, Toast.LENGTH_SHORT).show();
            if (adapter.getItemCount() == 0) {
                binding.backupHistoryCard.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(requireContext(), R.string.backup_delete_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearAllBackupsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_all_backups)
                .setMessage(R.string.clear_all_backups_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> clearAllBackups())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void clearAllBackups() {
        new Thread(() -> {
            boolean success = backupService.clearAllBackups();
            requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(requireContext(), R.string.clear_all_backups_success, Toast.LENGTH_SHORT).show();
                    loadBackupHistory(); // 刷新列表
                } else {
                    Toast.makeText(requireContext(), R.string.clear_all_backups_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}