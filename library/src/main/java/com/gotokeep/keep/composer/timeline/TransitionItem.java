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

    public TransitionItem(MediaItem startItem, MediaItem endItem, int layer) {
        super(TYPE_COMBINE, layer);
        this.startItem = startItem;
        this.endItem = endItem;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
