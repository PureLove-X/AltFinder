package tech.purelove.altfinder.util;

public final class RelativeTime {

    private RelativeTime() {}

    public static String format(long epochSeconds) {
        long now = System.currentTimeMillis() / 1000;
        long diff = Math.max(0, now - epochSeconds);

        if (diff < 10) return "just now";
        if (diff < 60) return diff + " seconds ago";

        long minutes = diff / 60;
        if (minutes < 60) return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours == 1 ? "" : "s") + " ago";

        long days = hours / 24;
        return days + " day" + (days == 1 ? "" : "s") + " ago";
    }
}
