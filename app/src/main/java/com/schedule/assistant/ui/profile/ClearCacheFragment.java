package com.schedule.assistant.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentClearCacheBinding;
import com.schedule.assistant.service.CacheService;

/**
 * 清除缓存界面
 * 实现缓存清理功能
 */
public class ClearCacheFragment extends Fragment {
    private FragmentClearCacheBinding binding;
    private ProgressBar progressBar;
    private TextView cacheSizeText;
    private MaterialButton clearButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClearCacheBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 初始化视图
        progressBar = binding.clearProgress;
        cacheSizeText = binding.cacheSize;
        clearButton = binding.clearCacheButton;

        // 设置工具栏
        binding.toolbar.setTitle(R.string.clear_cache);
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 加载缓存大小
        loadCacheSize();

        // 设置清除按钮点击事件
        clearButton.setOnClickListener(v -> showClearCacheDialog());

        return root;
    }

    private void loadCacheSize() {
        new Thread(() -> {
            String cacheSize = CacheService.formatFileSize(CacheService.getCacheSize(requireContext()));
            requireActivity().runOnUiThread(() -> cacheSizeText.setText(getString(R.string.cache_size, cacheSize)));
        }).start();
    }

    private void showClearCacheDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.clear_cache_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> clearCache())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void clearCache() {
        clearButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            boolean success = CacheService.clearCache(requireContext());
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                clearButton.setEnabled(true);
                Toast.makeText(requireContext(),
                        success ? R.string.clear_cache_success : R.string.clear_cache_failed,
                        Toast.LENGTH_SHORT).show();
                if (success) {
                    loadCacheSize();
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