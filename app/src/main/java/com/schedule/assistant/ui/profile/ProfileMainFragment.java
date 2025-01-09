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

public class ProfileMainFragment extends Fragment {

    private UserProfileDao userProfileDao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        AppDatabase db = AppDatabase.getDatabase(getActivity());
        userProfileDao = db.userProfileDao();

        MaterialCardView profileCard = view.findViewById(R.id.profile_card);
        profileCard.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_profileMainFragment_to_profileFragment));

        TextView profileName = view.findViewById(R.id.profile_name);
        TextView profileEmail = view.findViewById(R.id.profile_email);

        new Thread(() -> {
            UserProfile userProfile = userProfileDao.getUserProfile();
            if (userProfile != null) {
                getActivity().runOnUiThread(() -> {
                    profileName.setText(userProfile.getName() != null ? userProfile.getName()
                            : getString(R.string.profile_name_not_set));
                    profileEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail()
                            : getString(R.string.profile_email_not_set));
                });
            }
        }).start();

        // 设置其他点击事件
        view.findViewById(R.id.notification_settings).setOnClickListener(v -> {
            // TODO: 导航到通知设置页面
        });

        view.findViewById(R.id.theme_settings).setOnClickListener(v -> {
            // TODO: 导航到主题设置页面
        });

        view.findViewById(R.id.language_settings).setOnClickListener(v -> {
            // TODO: 导航到语言设置页面
        });

        view.findViewById(R.id.data_backup).setOnClickListener(v -> {
            // TODO: 导航到数据备份页面
        });

        view.findViewById(R.id.data_clear).setOnClickListener(v -> {
            // TODO: 导航到数据清理页面
        });

        view.findViewById(R.id.help_feedback).setOnClickListener(v -> {
            // TODO: 导航到帮助与反馈页面
        });

        view.findViewById(R.id.about).setOnClickListener(v -> {
            // TODO: 导航到关于页面
        });

        return view;
    }
}