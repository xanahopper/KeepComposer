package com.gotokeep.keep.social.composer.timeline;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 16:08
 */
public class Track {
    private static Comparator<? super Track> comparator;

    private final List<MediaItem> items = new LinkedList<>();
    private final boolean canOverlap;
    private int layer;
    private long endTimeMs;

    public Track(boolean canOverlap, int layer) {
        this.canOverlap = canOverlap;
        this.layer = layer;
    }

    public static Comparator<? super Track> getTypeComparator() {
        if (comparator == null) {
            comparator = (Comparator<Track>) (t1, t2) -> Integer.compare(t1.layer, t2.layer);
        }
        return comparator;
    }

    public void addMediaItem(MediaItem item) throws IllegalArgumentException {
        if (!canOverlap) {
            for (MediaItem mediaItem : items) {
                if (item.isRangeOverlap(mediaItem)) {
                    throw new IllegalArgumentException("Media time range cannot overlap.");
                }
            }
        }
        item.layer = layer;
        items.add(item);
        if (item.endTimeMs > endTimeMs) {
            endTimeMs = item.endTimeMs;
        }
    }

    public List<MediaItem> queryPresentationTimeItems(long presentationTimeUs) {
        List<MediaItem> list = new ArrayList<>();
        for (MediaItem item : items) {
            if (item.inRange(TimeUtil.usToMs(presentationTimeUs))) {
                list.add(item);
            }
        }
        return list;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void prepare(RenderFactory renderFactory) {
        Collections.sort(items, MediaItem::compareTo);
        for (MediaItem item : items) {
            if (item.getEndTimeMs() > endTimeMs) {
                endTimeMs = item.getEndTimeMs();
            }
            RenderNode node = renderFactory.createRenderNode(item);
            if (node != null) {
                node.preload();
                if (!node.isPrepared()) {
                    node.prepare();
                }
            }
        }
    }

    public List<MediaItem> getItems() {
        return items;
    }
}
