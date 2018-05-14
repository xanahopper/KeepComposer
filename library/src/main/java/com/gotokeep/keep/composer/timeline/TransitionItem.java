package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:08
 */
class TransitionItem extends MediaItem {
    protected long durationMs;
    protected MediaItem startItem;
    protected MediaItem endItem;
    protected String name;

    public TransitionItem(MediaItem startItem, MediaItem endItem) {
        super(TYPE_COMBINE);
        this.startItem = startItem;
        this.endItem = endItem;
    }
}
