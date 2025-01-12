package com.schedule.assistant.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 缓存服务类
 * 用于管理应用缓存，包括计算缓存大小和清理缓存
 */
public class CacheService {
    private static final String TAG = "CacheService";

    // 需要保护的目录和文件
    private static final String[] PROTECTED_PATHS = {
            "databases",
            "shared_prefs",
            "files"
    };

    // 需要清理的缓存目录
    private static final String[] CACHE_PATHS = {
            "cache",
            "code_cache",
            "app_webview/Cache"
    };

    public static long getCacheSize(Context context) {
        long size = 0;
        // 计算内部缓存大小
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            size += calculateDirSize(cacheDir);
        }

        // 计算外部缓存大小
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null && externalCacheDir.exists()) {
            size += calculateDirSize(externalCacheDir);
        }

        // 计算WebView缓存大小
        File webViewCache = new File(context.getApplicationInfo().dataDir + "/app_webview/Cache");
        if (webViewCache.exists()) {
            size += calculateDirSize(webViewCache);
        }

        return size;
    }

    public static boolean clearCache(Context context) {
        boolean success = true;
        try {
            // 清理内部缓存
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                success &= deleteDir(cacheDir, true);
            }

            // 清理外部缓存
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.exists()) {
                success &= deleteDir(externalCacheDir, true);
            }

            // 清理WebView缓存
            File webViewCache = new File(context.getApplicationInfo().dataDir + "/app_webview/Cache");
            if (webViewCache.exists()) {
                success &= deleteDir(webViewCache, true);
            }

            // 强制回收
            System.gc();

            return success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear cache", e);
            return false;
        }
    }

    private static long calculateDirSize(File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) {
            return 0;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }
        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else {
                size += calculateDirSize(file);
            }
        }
        return size;
    }

    private static boolean deleteDir(File dir, boolean isRoot) {
        if (dir == null || !dir.exists()) {
            return true;
        }

        // 检查是否是受保护的目录
        String dirPath = dir.getAbsolutePath();
        for (String protectedPath : PROTECTED_PATHS) {
            if (dirPath.contains(protectedPath)) {
                Log.d(TAG, "Skipping protected directory: " + dirPath);
                return true;
            }
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file, false);
                } else {
                    file.delete();
                }
            }
        }

        // 如果不是根缓存目录，则删除目录本身
        return isRoot || dir.delete();
    }

    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}