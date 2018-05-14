package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:07
 */
class OverlayItem extends MediaItem {
    protected int offsetX;
    protected int offsetY;
    protected String position;
    protected float rotation;
    protected float scale;
    protected int type;
    protected MediaItem baseItem;

    public OverlayItem(MediaItem baseItem) {
        super(TYPE_OVERLAY);
        this.baseItem = baseItem;
    }
}
