package com.schedule.assistant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.schedule.assistant.R;
import com.schedule.assistant.model.BackupHistoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 备份历史记录列表适配器
 */
public class BackupHistoryAdapter extends RecyclerView.Adapter<BackupHistoryAdapter.ViewHolder> {
    private final Context context;
    private final List<BackupHistoryItem> backupItems;
    private final OnBackupItemClickListener listener;

    public BackupHistoryAdapter(Context context, OnBackupItemClickListener listener) {
        this.context = context;
        this.backupItems = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_backup_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BackupHistoryItem item = backupItems.get(position);
        holder.backupTime.setText(item.getFormattedBackupTime(
                context.getString(R.string.backup_date_format)));
        holder.backupSize.setText(context.getString(R.string.backup_file_size,
                item.getFormattedFileSize()));

        holder.restoreButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestoreClick(item);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return backupItems.size();
    }

    /**
     * 更新备份历史记录列表
     * 
     * @param items 新的备份历史记录列表
     */
    public void updateItems(List<BackupHistoryItem> items) {
        backupItems.clear();
        backupItems.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * 移除指定的备份历史记录项
     * 
     * @param item 要移除的备份历史记录项
     */
    public void removeItem(BackupHistoryItem item) {
        int position = backupItems.indexOf(item);
        if (position != -1) {
            backupItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView backupTime;
        TextView backupSize;
        MaterialButton restoreButton;
        MaterialButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            backupTime = itemView.findViewById(R.id.backup_time);
            backupSize = itemView.findViewById(R.id.backup_size);
            restoreButton = itemView.findViewById(R.id.restore_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    /**
     * 备份历史记录项点击事件监听器
     */
    public interface OnBackupItemClickListener {
        void onRestoreClick(BackupHistoryItem item);

        void onDeleteClick(BackupHistoryItem item);
    }
}