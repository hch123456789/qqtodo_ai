package com.example.qqtodoAi.util;

public final class FileUtils {

    private FileUtils() {}

    public static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}