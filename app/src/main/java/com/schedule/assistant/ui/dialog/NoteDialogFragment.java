package com.schedule.assistant.ui.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.schedule.assistant.R;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.databinding.DialogNoteBinding;
import com.schedule.assistant.viewmodel.ShiftViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NoteDialogFragment extends BottomSheetDialogFragment {
    private DialogNoteBinding binding;
    private ShiftViewModel viewModel;
    private Shift currentShift;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_NOTE_LENGTH = 200;

    public static NoteDialogFragment newInstance(LocalDate date, @Nullable String currentNote) {
        NoteDialogFragment fragment = new NoteDialogFragment();
        Bundle args = new Bundle();
        args.putString("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (currentNote != null) {
            args.putString("note", currentNote);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ShiftViewModel.class);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = DialogNoteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        if (getArguments() != null && getArguments().containsKey("note")) {
            binding.noteEditText.setText(getArguments().getString("note"));
        }
    }

    private void setupListeners() {
        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.confirmButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String note = binding.noteEditText.getText().toString().trim();
        
        if (note.length() > MAX_NOTE_LENGTH) {
            binding.noteLayout.setError(getString(R.string.error_note_too_long));
            return;
        }

        if (getArguments() != null && getArguments().containsKey("date")) {
            String date = getArguments().getString("date");
            viewModel.updateNote(date, note);
        }

        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 