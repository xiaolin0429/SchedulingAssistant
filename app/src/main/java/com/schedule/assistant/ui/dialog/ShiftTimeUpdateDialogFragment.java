package com.schedule.assistant.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import com.schedule.assistant.R;

public class ShiftTimeUpdateDialogFragment extends DialogFragment {
    private OnShiftTimeUpdateListener listener;

    public interface OnShiftTimeUpdateListener {
        void onUpdateConfirmed(boolean shouldUpdateExisting);
    }

    public void setOnShiftTimeUpdateListener(OnShiftTimeUpdateListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.shift_time_update_title)
                .setMessage(R.string.shift_time_update_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (listener != null) {
                        listener.onUpdateConfirmed(true);
                    }
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    if (listener != null) {
                        listener.onUpdateConfirmed(false);
                    }
                })
                .create();
    }
}