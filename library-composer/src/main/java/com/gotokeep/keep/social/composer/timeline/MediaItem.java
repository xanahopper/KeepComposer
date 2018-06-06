package com.gotokeep.keep.social.composer.timeline;

import android.util.SparseArray;

import com.gotokeep.keep.social.composer.ScaleType;
import com.gotokeep.keep.social.composer.util.TimeUtil;

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

    protected long startTimeMs;
    protected long endTimeMs;
    protected float playSpeed = 1f;
    protected SparseArray<MediaItem> baseItem = new SparseArray<>();
    protected int type;
    protected int layer;
    protected ScaleType scaleType = ScaleType.FIT_CENTER;

    public MediaItem(int type, int layer, MediaItem baseItem) {
        this.type = type;
        this.layer = layer;
        if (baseItem != null) {
            this.baseItem.put(0, baseItem);
        }
    }

    @Override
    public int compareTo(MediaItem mediaItem) {
        return (int) Math.signum(startTimeMs - mediaItem.startTimeMs);
    }

    public static Comparator<? super MediaItem> getTypeComparator() {
        if (comparator == null) {
            comparator = (Comparator<MediaItem>) (t1, t2) -> Integer.compare(t1.layer, t2.layer);
        }
        return comparator;
    }

    public boolean inRange(long positionMs) {
        return TimeUtil.inRange(positionMs, startTimeMs, endTimeMs);
    }

    public SparseArray<MediaItem> getBaseItem() {
        return baseItem;
    }

    public void setBaseItem(int index, MediaItem baseItem) {
        this.baseItem.put(index, baseItem);
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

    public void setTimeRangeMs(long startTimeMs, long endTimeMs) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public long getDurationMs() {
        return Math.max(endTimeMs - startTimeMs, 0);
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
    }

    public boolean isRangeOverlap(MediaItem mediaItem) {
        return (startTimeMs < mediaItem.endTimeMs && endTimeMs > mediaItem.startTimeMs) ||
                (endTimeMs > mediaItem.startTimeMs && startTimeMs < mediaItem.endTimeMs);
    }

    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }
}
