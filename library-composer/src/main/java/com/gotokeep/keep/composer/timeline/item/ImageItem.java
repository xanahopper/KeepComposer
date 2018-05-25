package com.gotokeep.keep.composer.timeline.item;

import com.gotokeep.keep.composer.timeline.MediaItem;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:06
 */
public class ImageItem extends MediaItem {
    protected String filePath;

    public ImageItem(String filePath) {
        super(TYPE_SOURCE, 0, null);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
