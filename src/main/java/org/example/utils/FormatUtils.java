package org.example.utils;

public final class FormatUtils {
    private static final double MAX_PERCENTAGE = 100.0;
    private static final double PERCENTAGE_PRECISION = 10.0;
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final String FORMAT_KILO = "%.1fK";
    private static final String FORMAT_MEGA = "%.1fM";

    private FormatUtils() {}

    public static double calculateSuccessRate(long completed, long failed) {
        if (completed + failed == 0) return MAX_PERCENTAGE;
        double rate = ((double) completed / (completed + failed)) * MAX_PERCENTAGE;
        return Math.round(rate * PERCENTAGE_PRECISION) / PERCENTAGE_PRECISION;
    }

    public static String formatVectorCount(long count) {
        if (count < THOUSAND) return String.valueOf(count);
        if (count < MILLION) return String.format(FORMAT_KILO, count / (double) THOUSAND);
        return String.format(FORMAT_MEGA, count / (double) MILLION);
    }
}
