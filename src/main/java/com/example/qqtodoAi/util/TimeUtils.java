package com.example.qqtodoAi.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class TimeUtils {

    private TimeUtils() {}

    private static final String[] PATTERNS = {"yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

    public static LocalDateTime parse(String time) {
        if (time == null || time.isBlank() || "null".equalsIgnoreCase(time)) {
            return null;
        }
        String t = time.trim().replace('T', ' ');
        for (String p : PATTERNS) {
            try {
                if ("yyyy-MM-dd".equals(p)) {
                    return LocalDate.parse(t, DateTimeFormatter.ofPattern(p)).atStartOfDay();
                }
                return LocalDateTime.parse(t, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {
            }
        }
        log.warn("[TimeUtils] 无法解析时间: {}", time);
        return null;
    }
}