package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:06
 */
class ImageItem extends MediaItem {
    protected String filePath;

    public ImageItem(int layer) {
        super(TYPE_SOURCE, layer);
    }
}
