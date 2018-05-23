package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:11
 */
public class AudioItem extends MediaItem {
    protected String filePath;

    public AudioItem(String filePath) {
        super(TYPE_SOURCE, 0, null);
        this.filePath = filePath;
    }
}