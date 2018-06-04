package com.gotokeep.keep.social.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-04 10:55
 */
public final class TimeRange {
    public long startTimeMs;
    public long endTimeMs;

    public TimeRange() {
        this(0, 0);
    }

    public TimeRange(long startTimeMs, long endTimeMs) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public long durationMs() {
        return endTimeMs - startTimeMs;
    }
}
