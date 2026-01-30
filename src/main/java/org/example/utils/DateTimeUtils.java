package org.example.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public final class DateTimeUtils {
    public static final String TIME_N_A = "n/a";
    private static final int SEC_PER_MIN = 60;
    private static final int MIN_PER_HOUR = 60;
    private static final int HOUR_PER_DAY = 24;
    private DateTimeUtils() {}
    public static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return TIME_N_A;
        Duration d = Duration.between(dateTime, LocalDateTime.now());
        if (d.getSeconds() < SEC_PER_MIN) return d.getSeconds() + "s ago";
        if (d.toMinutes() < MIN_PER_HOUR) return d.toMinutes() + "m ago";
        if (d.toHours() < HOUR_PER_DAY) return d.toHours() + "h ago";
        return d.toDays() + "d ago";
    }
}
