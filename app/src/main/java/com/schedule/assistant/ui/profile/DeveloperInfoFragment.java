package com.schedule.assistant.ui.profile;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.schedule.assistant.R;
import com.schedule.assistant.databinding.FragmentDeveloperInfoBinding;

public class DeveloperInfoFragment extends Fragment {
    private static final String TAG = "DeveloperInfoFragment";
    private FragmentDeveloperInfoBinding binding;
    private static final String DEVELOPER_EMAIL = "yulin0429@foxmail.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDeveloperInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置工具栏
        binding.toolbar.setTitle(R.string.developer_info);
        binding.toolbar.setNavigationIcon(R.drawable.ic_back);
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // 设置邮箱按钮点击事件
        binding.emailButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
                startActivity(Intent.createChooser(intent, getString(R.string.contact_email)));
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No email app found", e);
                Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
                Toast.makeText(requireContext(), R.string.no_email_app, Toast.LENGTH_SHORT).show();
            }
        });

        // 设置GitHub按钮点击事件
        binding.githubButton.setOnClickListener(v -> {
            try {
                String url = getString(R.string.github_url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "No browser app found", e);
                Toast.makeText(requireContext(), R.string.no_browser_app, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to open GitHub URL", e);
                Toast.makeText(requireContext(), R.string.no_browser_app, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}