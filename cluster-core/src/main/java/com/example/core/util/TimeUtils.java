package com.example.core.util;

import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class TimeUtils {

    private static final String DEFAULT_PARTTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String DEFAULT_DATE_PARTTERN = "yyyy-MM-dd";

    private static final ConcurrentHashMap<String, DateTimeFormatter> CACHE = new ConcurrentHashMap<>() {
        {
            put(DEFAULT_DATE_PARTTERN, DateTimeFormatter.ofPattern(DEFAULT_DATE_PARTTERN));
            put(DEFAULT_PARTTERN, DateTimeFormatter.ofPattern(DEFAULT_PARTTERN));
        }
    };

    private TimeUtils() {
        throw new RuntimeException("util class can not be init!");
    }

    private DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter formatter = CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
        return formatter;
    }

    public String format(LocalDateTime time) {
        return format(time, DEFAULT_PARTTERN);
    }

    public String format(LocalDateTime time, String pattern) {
        return getFormatter(pattern).format(time);
    }

    public String format(LocalDate time) {
        return format(time, DEFAULT_DATE_PARTTERN);
    }

    public String format(LocalDate time, String pattern) {
        return getFormatter(pattern).format(time);
    }

    public String format(ZonedDateTime time) {
        return format(time, DEFAULT_PARTTERN);
    }

    public String format(ZonedDateTime time, String pattern) {
        return getFormatter(pattern).format(time);
    }

    public String format(Instant time) {
        return format(time, DEFAULT_PARTTERN);
    }

    public String format(Instant time, String pattern) {
        ZonedDateTime zone = time.atZone(ZoneId.systemDefault());
        return getFormatter(pattern).format(zone);
    }

    public String format(Long timestamp) {
        return format(timestamp, DEFAULT_PARTTERN);
    }

    public String format(Long timestamp, String pattern) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault());
        return format(zonedDateTime, pattern);
    }

    public String format(Duration duration) {
        long millis = duration.toMillis();
        return format(millis);
    }

    public String format(Duration duration, String pattern) {
        long millis = duration.toMillis();
        return format(millis, pattern);
    }

    public String format(Date time) {
        ZonedDateTime zonedDateTime = time.toInstant().atZone(ZoneId.systemDefault());
        return format(zonedDateTime);
    }

    public String format(Date time, String pattern) {
        ZonedDateTime zonedDateTime = time.toInstant().atZone(ZoneId.systemDefault());
        return format(zonedDateTime, pattern);
    }

    public LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public LocalDateTime parse(String timeStr, String pattern) {
        DateTimeFormatter formatter = getFormatter(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, formatter);
        return localDateTime;
    }

}
