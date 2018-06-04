package com.gotokeep.keep.social.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-04 10:55
 */
public final class TimeRange {
    public long startTimeMs;
    public long endTimeMs;

    public long durationMs() {
        return endTimeMs - startTimeMs;
    }
}
