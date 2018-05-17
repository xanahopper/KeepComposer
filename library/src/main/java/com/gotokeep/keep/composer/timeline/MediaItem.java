package com.gotokeep.keep.composer.timeline;

import com.gotokeep.keep.composer.util.TimeUtil;

import java.util.Comparator;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 11:54
 */
public abstract class MediaItem implements Comparable<MediaItem> {
    public static final int TYPE_SOURCE = 0;
    public static final int TYPE_DRAW = 1;
    public static final int TYPE_OVERLAY = 2;
    public static final int TYPE_COMBINE = 3;

    private static Comparator<? super MediaItem> comparator;

    long startTimeMs;
    long endTimeMs;
    float playSpeed;
    int type;
    int layer;

    public MediaItem(int type, int layer) {
        this.type = type;
        this.layer = layer;
    }

    @Override
    public int compareTo(MediaItem mediaItem) {
        return (int) Math.signum(startTimeMs - mediaItem.startTimeMs);
    }

    public static Comparator<? super MediaItem> getTypeComparator() {
        if (comparator == null) {
            comparator = (Comparator<MediaItem>) (t1, t2) -> {
                if (t1.type < t2.type) {
                    return -1;
                } else if (t1.type > t2.type) {
                    return 1;
                } else {
                    return 0;
                }
            };
        }
        return comparator;
    }

    public boolean inRange(long positionMs) {
        return TimeUtil.inRange(positionMs, startTimeMs, endTimeMs);
    }

    public int getLayer() {
        return layer;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
    }
}
