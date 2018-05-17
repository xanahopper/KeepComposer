package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-17 10:35
 */
public class LayerItem extends OverlayItem {

    public LayerItem(MediaItem baseItem, int layer, String name) {
        super(baseItem, layer);
        this.name = name;
    }
}
