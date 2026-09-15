/*
 * Decompiled with CFR 0.152.
 */
package com.fortunepillars.utils;

public class TimeUtil {
    private TimeUtil() {
    }

    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static String formatTimeVerbose(int seconds) {
        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
        int mins = seconds / 60;
        int secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        sb.append(mins).append(" minute").append(mins != 1 ? "s" : "");
        if (secs > 0) {
            sb.append(" ").append(secs).append(" second").append(secs != 1 ? "s" : "");
        }
        return sb.toString();
    }

    public static String formatDuration(long millis) {
        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        if (days > 0L) {
            return days + "d " + hours % 24L + "h";
        }
        if (hours > 0L) {
            return hours + "h " + minutes % 60L + "m";
        }
        if (minutes > 0L) {
            return minutes + "m " + seconds % 60L + "s";
        }
        return seconds + "s";
    }

    public static int parseTime(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }
        input = input.toLowerCase().trim();
        int total = 0;
        StringBuilder number = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
                continue;
            }
            if (number.length() <= 0) continue;
            int value = Integer.parseInt(number.toString());
            switch (c) {
                case 'd': {
                    total += value * 86400;
                    break;
                }
                case 'h': {
                    total += value * 3600;
                    break;
                }
                case 'm': {
                    total += value * 60;
                    break;
                }
                case 's': {
                    total += value;
                }
            }
            number = new StringBuilder();
        }
        if (number.length() > 0) {
            total += Integer.parseInt(number.toString());
        }
        return total;
    }

    public static String getRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000L;
        if (seconds < 60L) {
            return "just now";
        }
        if (seconds < 3600L) {
            return seconds / 60L + " minutes ago";
        }
        if (seconds < 86400L) {
            return seconds / 3600L + " hours ago";
        }
        if (seconds < 604800L) {
            return seconds / 86400L + " days ago";
        }
        return seconds / 604800L + " weeks ago";
    }
}

