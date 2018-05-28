package com.gotokeep.keep.composer.timeline.item;

import com.gotokeep.keep.composer.timeline.MediaItem;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:08
 */
public class TransitionItem extends MediaItem {
    protected long durationMs;
    protected MediaItem startItem;
    protected MediaItem endItem;
    protected String name;

    public TransitionItem(MediaItem startItem, MediaItem endItem, long durationMs, int layer) {
        super(TYPE_COMBINE, layer, null);
        this.startItem = startItem;
        this.endItem = endItem;
        baseItem.put(0, startItem);
        baseItem.put(1, endItem);
        setDurationMs(durationMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
        if (startItem.getEndTimeMs() == endItem.getStartTimeMs()) {
            this.startTimeMs = startItem.getEndTimeMs() - durationMs / 2;
            this.endTimeMs = endItem.getStartTimeMs() + durationMs / 2;
            startItem.setEndTimeMs(endTimeMs);
            endItem.setStartTimeMs(startTimeMs);
        } else if (startItem.getEndTimeMs() > endItem.getStartTimeMs() && startItem.getEndTimeMs() - endItem.getStartTimeMs() == durationMs) {
            this.startTimeMs = endItem.getStartTimeMs();
            this.endTimeMs = startItem.getEndTimeMs();
        } else {
            startTimeMs = 0;
            endTimeMs = 0;
        }
    }

    public MediaItem getStartItem() {
        return startItem;
    }

    public void setStartItem(MediaItem startItem) {
        this.startItem = startItem;
    }

    public MediaItem getEndItem() {
        return endItem;
    }

    public void setEndItem(MediaItem endItem) {
        this.endItem = endItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
