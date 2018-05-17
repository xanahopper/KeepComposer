package com.gotokeep.keep.composer.timeline;

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
        super(TYPE_COMBINE, layer);
        this.startItem = startItem;
        this.endItem = endItem;
        setDurationMs(durationMs);
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
        if (startItem.endTimeMs == endItem.startTimeMs) {
            this.startTimeMs = startItem.endTimeMs - durationMs / 2;
            this.endTimeMs = endItem.startTimeMs + durationMs / 2;
            startItem.endTimeMs = endTimeMs;
            endItem.startTimeMs = startTimeMs;
        } else if (startItem.endTimeMs > endItem.startTimeMs && startItem.endTimeMs - endItem.startTimeMs == durationMs) {
            this.startTimeMs = endItem.startTimeMs;
            this.endTimeMs = startItem.endTimeMs;
        } else {
            startTimeMs = 0;
            endTimeMs = 0;
        }
    }
}
