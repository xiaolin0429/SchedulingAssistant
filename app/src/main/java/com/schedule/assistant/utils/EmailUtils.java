package com.schedule.assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class EmailUtils {
    private static final String TAG = "EmailUtils";
    private static final String DEVELOPER_EMAIL = "yulin0429@foxmail.com";
    private static final String EMAIL_SUBJECT = "SchedulingAssistant Feedback";

    public interface EmailCallback {
        void onSuccess();

        void onFailure(String error);
    }

    public static void sendFeedback(Context context, String feedback, EmailCallback callback) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { DEVELOPER_EMAIL });
            intent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
            intent.putExtra(Intent.EXTRA_TEXT, feedback);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(intent, "Send feedback via email"));
                callback.onSuccess();
            } else {
                callback.onFailure("No email client found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending email", e);
            callback.onFailure(e.getMessage());
        }
    }
}