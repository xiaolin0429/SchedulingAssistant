package com.schedule.assistant.ui.profile;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.schedule.assistant.R;
import com.schedule.assistant.adapter.FaqAdapter;
import com.schedule.assistant.model.FaqItem;
import com.schedule.assistant.utils.LogCollector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 帮助与反馈页面Fragment
 */
public class HelpFeedbackFragment extends Fragment {
    private static final String TAG = "HelpFeedbackFragment";

    private TextInputEditText feedbackInput;
    private MaterialCheckBox includeLogsCheckbox;
    private TextView logsHelperText;
    private MaterialButton submitButton;
    private String logFilePath;
    private boolean isExportAction = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission is granted
                    try {
                        if (isExportAction) {
                            exportLogs();
                        } else {
                            collectLogs();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to handle logs", e);
                        Toast.makeText(requireContext(),
                                isExportAction ? R.string.logs_export_failed : R.string.logs_collection_failed,
                                Toast.LENGTH_SHORT).show();
                        if (!isExportAction) {
                            includeLogsCheckbox.setChecked(false);
                        }
                    }
                } else {
                    // Permission is denied
                    Toast.makeText(requireContext(),
                            R.string.logs_permission_required,
                            Toast.LENGTH_SHORT).show();
                    if (!isExportAction) {
                        includeLogsCheckbox.setChecked(false);
                    }
                }
                isExportAction = false;
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_feedback, container, false);

        // 设置工具栏
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 初始化FAQ列表
        RecyclerView faqList = view.findViewById(R.id.faq_list);
        faqList.setLayoutManager(new LinearLayoutManager(requireContext()));
        faqList.setAdapter(new FaqAdapter(createFaqList()));

        // 初始化反馈相关视图
        feedbackInput = view.findViewById(R.id.feedback_input);
        submitButton = view.findViewById(R.id.submit_feedback_button);
        submitButton.setOnClickListener(v -> submitFeedback());

        // Initialize views
        includeLogsCheckbox = view.findViewById(R.id.include_logs_checkbox);
        logsHelperText = view.findViewById(R.id.logs_helper_text);
        MaterialButton exportLogsButton = view.findViewById(R.id.export_logs_button);

        // Set up checkbox listener
        includeLogsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            logsHelperText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                isExportAction = false;
                requestLogPermissionAndCollect();
            } else {
                logFilePath = null;
            }
        });

        // Set up export logs button
        exportLogsButton.setOnClickListener(v -> {
            isExportAction = true;
            requestLogPermissionAndCollect();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 清理7天前的日志文件
        LogCollector.cleanOldLogs(requireContext(), 7);
    }

    private List<FaqItem> createFaqList() {
        List<FaqItem> faqItems = new ArrayList<>();
        faqItems.add(new FaqItem(
                getString(R.string.faq_backup_restore),
                getString(R.string.faq_backup_restore_answer)));
        faqItems.add(new FaqItem(
                getString(R.string.faq_theme_setting),
                getString(R.string.faq_theme_setting_answer)));
        faqItems.add(new FaqItem(
                getString(R.string.faq_language_setting),
                getString(R.string.faq_language_setting_answer)));
        faqItems.add(new FaqItem(
                getString(R.string.faq_notification),
                getString(R.string.faq_notification_answer)));
        faqItems.add(new FaqItem(
                getString(R.string.faq_shift_template),
                getString(R.string.faq_shift_template_answer)));
        return faqItems;
    }

    private void requestLogPermissionAndCollect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, request media permissions
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // For Android 12 and below, request storage permissions
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void collectLogs() {
        try {
            logFilePath = LogCollector.collectLogs(requireContext());
            if (logFilePath != null) {
                Toast.makeText(requireContext(), R.string.logs_collection_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                throw new IOException("Failed to collect logs: null path returned");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to collect logs", e);
            Toast.makeText(requireContext(), R.string.logs_collection_failed,
                    Toast.LENGTH_SHORT).show();
            includeLogsCheckbox.setChecked(false);
            logFilePath = null;
        }
    }

    private void exportLogs() {
        try {
            String logPath = LogCollector.collectLogs(requireContext());
            if (logPath != null) {
                File logFile = new File(logPath);
                Uri logUri = FileProvider.getUriForFile(requireContext(),
                        "com.schedule.assistant.fileprovider",
                        logFile);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, logUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent,
                        getString(R.string.export_logs_title)));

                Toast.makeText(requireContext(),
                        R.string.logs_export_success,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to export logs", e);
            Toast.makeText(requireContext(),
                    R.string.logs_export_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void submitFeedback() {
        CharSequence inputText = feedbackInput.getText();
        String feedbackText = inputText != null ? inputText.toString().trim() : "";

        // Validate input
        if (feedbackText.isEmpty()) {
            Toast.makeText(requireContext(), R.string.feedback_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (feedbackText.length() < 10) {
            Toast.makeText(requireContext(), R.string.feedback_too_short, Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable submit button
        submitButton.setEnabled(false);

        // Create email intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "yulin0429@foxmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback: Schedule Assistant");
        intent.putExtra(Intent.EXTRA_TEXT, feedbackText);

        // Attach log file if available
        if (logFilePath != null && includeLogsCheckbox.isChecked()) {
            try {
                File logFile = new File(logFilePath);
                Uri logUri = FileProvider.getUriForFile(requireContext(),
                        "com.schedule.assistant.fileprovider",
                        logFile);
                intent.putExtra(Intent.EXTRA_STREAM, logUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to attach log file", e);
            }
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_feedback)));
            feedbackInput.setText("");
            includeLogsCheckbox.setChecked(false);
            logFilePath = null;
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show();
        } finally {
            submitButton.setEnabled(true);
        }
    }
}