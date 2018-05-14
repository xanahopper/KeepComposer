package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:10
 */
class FilterItem extends MediaItem {
    protected String name;
    protected MediaItem baseItem;

    public FilterItem(MediaItem baseItem) {
        super(TYPE_DRAW);
        this.baseItem = baseItem;
    }
}
