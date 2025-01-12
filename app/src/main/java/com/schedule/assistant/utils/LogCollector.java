package com.schedule.assistant.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志收集工具类
 */
public class LogCollector {
    private static final String TAG = "LogCollector";
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE_PREFIX = "app_log_";
    private static final String LOG_FILE_SUFFIX = ".txt";

    /**
     * 收集应用日志
     * 
     * @param context 上下文
     * @return 日志文件路径，如果收集失败则返回null
     */
    public static String collectLogs(Context context) {
        String logFilePath = null;
        Process process = null;
        BufferedReader reader = null;
        FileWriter writer = null;

        try {
            // 创建日志文件
            File logDir = new File(context.getExternalFilesDir(null), LOG_FOLDER);
            if (!logDir.exists() && !logDir.mkdirs()) {
                Log.e(TAG, "Failed to create log directory");
                return null;
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File logFile = new File(logDir, LOG_FILE_PREFIX + timestamp + LOG_FILE_SUFFIX);
            writer = new FileWriter(logFile);

            // 写入基本设备信息
            writer.write("Device Information:\n");
            writer.write("Brand: " + Build.BRAND + "\n");
            writer.write("Model: " + Build.MODEL + "\n");
            writer.write("Android Version: " + Build.VERSION.RELEASE + "\n");
            writer.write("SDK Level: " + Build.VERSION.SDK_INT + "\n\n");
            writer.write("Log Information:\n");

            // 收集logcat日志
            process = Runtime.getRuntime().exec("logcat -d");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }

            logFilePath = logFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to collect logs", e);
        } finally {
            // 关闭资源
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
                if (process != null)
                    process.destroy();
            } catch (IOException e) {
                Log.e(TAG, "Failed to close resources", e);
            }
        }

        return logFilePath;
    }

    /**
     * 清理旧日志文件
     * 
     * @param context    上下文
     * @param maxAgeDays 保留的最大天数
     */
    public static void cleanOldLogs(Context context, int maxAgeDays) {
        File logDir = new File(context.getExternalFilesDir(null), LOG_FOLDER);
        if (!logDir.exists())
            return;

        long maxAgeMillis = maxAgeDays * 24 * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        File[] files = logDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (now - file.lastModified() > maxAgeMillis) {
                    if (!file.delete()) {
                        Log.w(TAG, "Failed to delete old log file: " + file.getName());
                    }
                }
            }
        }
    }
}