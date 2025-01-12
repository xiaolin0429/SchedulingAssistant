package com.schedule.assistant.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.schedule.assistant.R;
import com.schedule.assistant.adapter.FaqAdapter;
import com.schedule.assistant.model.FaqItem;
import com.schedule.assistant.utils.EmailUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 帮助与反馈页面Fragment
 */
public class HelpFeedbackFragment extends Fragment {
    private TextInputEditText feedbackInput;

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
        MaterialButton submitButton = view.findViewById(R.id.submit_feedback_button);
        submitButton.setOnClickListener(v -> submitFeedback());

        return view;
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

    private void submitFeedback() {
        CharSequence inputText = feedbackInput.getText();
        String feedback = inputText != null ? inputText.toString().trim() : "";

        if (feedback.isEmpty()) {
            Toast.makeText(requireContext(), R.string.feedback_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        EmailUtils.sendFeedback(requireContext(), feedback, new EmailUtils.EmailCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), R.string.feedback_success, Toast.LENGTH_SHORT).show();
                feedbackInput.setText("");
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), R.string.feedback_failed, Toast.LENGTH_SHORT).show();
                Log.e("HelpFeedbackFragment", "Failed to send feedback: " + error);
            }
        });
    }
}