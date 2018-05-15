package com.gotokeep.keep.composer.timeline;

import java.util.Comparator;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 11:54
 */
public class MediaItem implements Comparable<MediaItem> {
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
            comparator = new Comparator<MediaItem>() {
                @Override
                public int compare(MediaItem t1, MediaItem t2) {
                    if (t1.type < t2.type) {
                        return -1;
                    } else if (t1.type > t2.type) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
        }
        return comparator;
    }

    public int getLayer() {
        return layer;
    }
}
