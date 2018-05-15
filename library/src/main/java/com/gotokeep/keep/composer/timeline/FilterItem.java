package com.gotokeep.keep.composer.timeline;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:10
 */
class FilterItem extends MediaItem {
    protected String name;
    protected MediaItem baseItem;
    protected Map<String, Object> params;

    public FilterItem(MediaItem baseItem, int layer, Map<String, Object> params) {
        super(TYPE_DRAW, layer);
        this.baseItem = baseItem;
        this.params = params;
    }
}
