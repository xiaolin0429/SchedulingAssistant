package com.schedule.assistant.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import android.os.Bundle;
import com.schedule.assistant.R;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.UserProfile;
import com.schedule.assistant.data.dao.UserProfileDao;
import android.util.Patterns;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import android.app.Activity;
import androidx.activity.OnBackPressedCallback;

import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PREFS_NAME = "user_profile_prefs";
    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";
    private ImageView profileImage;
    private TextInputEditText profileName;
    private TextInputEditText profileEmail;
    private UserProfileDao userProfileDao;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                        profileImage.setTag(uri);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        Button saveButton = view.findViewById(R.id.save_button);

        AppDatabase db = AppDatabase.getDatabase(getActivity());
        userProfileDao = db.userProfileDao();

        loadProfileInfo();

        profileImage.setOnClickListener(v -> openImageChooser());
        saveButton.setOnClickListener(v -> saveProfileInfo());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isEnabled()) {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });
    }

    private void loadProfileInfo() {
        new Thread(() -> {
            UserProfile userProfile = userProfileDao.getUserProfile();
            if (userProfile != null) {
                requireActivity().runOnUiThread(() -> {
                    profileName.setText(userProfile.getName());
                    profileEmail.setText(userProfile.getEmail());
                    if (userProfile.getImageUri() != null) {
                        Uri imageUri = Uri.parse(userProfile.getImageUri());
                        profileImage.setImageURI(imageUri);
                    }
                });
            }
        }).start();
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void saveProfileInfo() {
        String name = Objects.requireNonNull(profileName.getText()).toString();
        String email = Objects.requireNonNull(profileEmail.getText()).toString();

        if (name.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getActivity(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = (Uri) profileImage.getTag();
        String imageUriString = imageUri != null ? imageUri.toString() : "";

        if (getActivity() == null) {
            return;
        }

        new Thread(() -> {
            UserProfile userProfile = userProfileDao.getUserProfile();
            if (userProfile == null) {
                userProfile = new UserProfile();
                userProfile.setName(name);
                userProfile.setImageUri(imageUriString);
                userProfile.setEmail(email);
                userProfileDao.insert(userProfile);
            } else {
                userProfileDao.updateProfile(userProfile.getId(), name, imageUriString, email);
            }

            if (getActivity() != null) {
                getActivity()
                        .runOnUiThread(() -> Toast.makeText(getActivity(), "Profile saved", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}