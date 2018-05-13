package com.gotokeep.keep.composer.util;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 13:39
 */
public final class TimeUtil {
    public static final int MILLISECOND = 1000;
    public static final int BILLION_US = 1000_000;

    public static long secToMs(int sec) {
        return sec * MILLISECOND;
    }

    public static long secToUs(int sec) {
        return sec * BILLION_US;
    }

    public static long msToUs(long timeMs) {
        return timeMs * MILLISECOND;
    }

    public static long usToMs(long timeUs) {
        return Math.round((float) timeUs / MILLISECOND);
    }

    public static long usToSec(long timeUs) {
        return Math.round((float) timeUs / BILLION_US);
    }
}
