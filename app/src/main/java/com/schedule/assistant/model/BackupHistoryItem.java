package com.schedule.assistant.model;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 备份历史记录项
 * 用于在列表中显示备份文件信息
 */
public class BackupHistoryItem {
    private final File backupFile;
    private final String fileName;
    private final long fileSize;
    private final long backupTime;

    public BackupHistoryItem(File backupFile) {
        this.backupFile = backupFile;
        this.fileName = backupFile.getName();
        this.fileSize = backupFile.length();
        this.backupTime = backupFile.lastModified();
    }

    public File getBackupFile() {
        return backupFile;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getBackupTime() {
        return backupTime;
    }

    /**
     * 获取格式化的备份时间
     * 
     * @param format 日期格式字符串
     * @return 格式化后的日期字符串
     */
    public String getFormattedBackupTime(String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(new Date(backupTime));
    }

    /**
     * 获取格式化的文件大小
     * 
     * @return 格式化后的文件大小字符串（如：1.5 MB）
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", fileSize / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.1f MB", fileSize / (1024.0 * 1024));
        }
    }
}