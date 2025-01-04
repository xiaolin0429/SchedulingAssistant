package com.schedule.assistant.util;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.schedule.assistant.R;
import com.schedule.assistant.data.AppDatabase;
import com.schedule.assistant.data.entity.Shift;
import com.schedule.assistant.data.entity.ShiftType;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.Collections;
import androidx.lifecycle.LiveData;

public class NoteManager {
    private static final int MAX_NOTE_LENGTH = 500;
    private static final int DEFAULT_RECENT_NOTES_LIMIT = 20;
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final AppDatabase database;
    private final Executor executor;
    private final Context context;

    public interface NoteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface LoadNoteCallback {
        void onNoteLoaded(String note);
        void onError(Exception e);
    }

    public interface LoadNotesCallback {
        void onNotesLoaded(List<Shift> shifts);
        void onError(Exception e);
    }

    public NoteManager(@NonNull Context context, @NonNull Executor executor) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (executor == null) {
            throw new IllegalArgumentException("Executor cannot be null");
        }
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getDatabase(context);
        this.executor = executor;
    }

    public void saveNote(@NonNull String date, @Nullable String note, @NonNull NoteCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (date == null) {
            callback.onError(new IllegalArgumentException("Date cannot be null"));
            return;
        }

        if (!DateUtil.isValidScheduleDate(date)) {
            callback.onError(new IllegalArgumentException(context.getString(R.string.invalid_date)));
            return;
        }

        if (TextUtils.isEmpty(note)) {
            callback.onError(new IllegalArgumentException(context.getString(R.string.empty_note)));
            return;
        }

        if (note.length() > MAX_NOTE_LENGTH) {
            callback.onError(new IllegalArgumentException(context.getString(R.string.note_too_long)));
            return;
        }

        executeWithRetry(() -> {
            try {
                Shift shift = database.shiftDao().getShiftByDateDirect(date);
                long updateTime = System.currentTimeMillis();

                if (shift == null) {
                    shift = new Shift(date, ShiftType.REST_DAY);
                    shift.setNote(note);
                    shift.setUpdateTime(updateTime);
                    database.shiftDao().insert(shift);
                } else {
                    shift.setNote(note);
                    shift.setUpdateTime(updateTime);
                    database.shiftDao().update(shift);
                }
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void loadNote(@NonNull String date, @NonNull LoadNoteCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (date == null) {
            callback.onError(new IllegalArgumentException("Date cannot be null"));
            return;
        }

        if (!DateUtil.isValidScheduleDate(date)) {
            callback.onError(new IllegalArgumentException(context.getString(R.string.invalid_date)));
            return;
        }

        executeWithRetry(() -> {
            try {
                Shift shift = database.shiftDao().getShiftByDateDirect(date);
                String note = shift != null ? shift.getNote() : "";
                callback.onNoteLoaded(note);
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getRecentNotes(@NonNull LoadNotesCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        
        executeWithRetry(() -> {
            try {
                LiveData<List<Shift>> shiftsLiveData = database.shiftDao().getShiftsWithNotes();
                while (shiftsLiveData.getValue() == null) {
                    Thread.sleep(100);
                }
                callback.onNotesLoaded(shiftsLiveData.getValue());
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private void executeWithRetry(Runnable task) {
        executeWithRetry(task, 0);
    }

    private void executeWithRetry(Runnable task, int retryCount) {
        executor.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        executeWithRetry(task, retryCount + 1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw e;
                }
            }
        });
    }

    private void saveNote(String date, String note) {
        if (database != null) {
            long updateTime = System.currentTimeMillis();
            try {
                Shift shift = database.shiftDao().getShiftByDateDirect(date);
                if (shift == null) {
                    shift = new Shift(date, ShiftType.REST_DAY);
                    shift.setNote(note);
                    shift.setUpdateTime(updateTime);
                    database.shiftDao().insert(shift);
                } else {
                    database.shiftDao().updateNote(shift.getId(), note);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Shift> getShiftsWithNotes() {
        if (database != null) {
            try {
                LiveData<List<Shift>> shiftsLiveData = database.shiftDao().getShiftsWithNotes();
                // 由于这是在后台线程中执行的，我们可以安全地等待结果
                while (shiftsLiveData.getValue() == null) {
                    Thread.sleep(100);
                }
                return shiftsLiveData.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
} 