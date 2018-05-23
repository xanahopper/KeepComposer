package com.gotokeep.keep.composer.util;

import android.os.SystemClock;

/**
 * Track the progression of media time. From ExoPlayer
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 14:37
 */
public final class MediaClock {

    private boolean started;
    private long baseUs;
    private long baseElapsedMs;

    /**
     * Starts the clock. Does nothing if the clock is already started.
     */
    public void start() {
        if (!started) {
            baseElapsedMs = SystemClock.elapsedRealtime();
            started = true;
        }
    }

    public void stop() {
        if (started) {
            setPositionUs(getPositionUs());
            started = false;
        }
    }

    public void setPositionUs(long positionUs) {
        baseUs = positionUs;
        if (started) {
            baseElapsedMs = SystemClock.elapsedRealtime();
        }
    }

    public long getPositionUs() {
        long positionUs = baseUs;
        if (started) {
            long elapsedSinceBaseMs = SystemClock.elapsedRealtime() - baseElapsedMs;
            positionUs += TimeUtil.msToUs(elapsedSinceBaseMs);
        }
        return positionUs;
    }
}
