// FILE 31: VideoValidator.java
package com.eduplatform.video.util;

public class VideoValidator {

    public static boolean isValidVideoFormat(String fileName) {
        String[] validFormats = {"mp4", "mov", "webm", "flv", "avi"};
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        for (String format : validFormats) {
            if (format.equals(ext)) return true;
        }
        return false;
    }

    public static boolean isValidSubtitleFormat(String fileName) {
        return fileName.endsWith(".vtt") || fileName.endsWith(".srt");
    }

    public static long getMaxFileSize() {
        return 5 * 1024 * 1024 * 1024; // 5GB
    }
}