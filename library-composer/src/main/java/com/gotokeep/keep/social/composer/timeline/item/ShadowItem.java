package com.gotokeep.keep.social.composer.timeline.item;

import com.gotokeep.keep.social.composer.timeline.MediaItem;

/**
 * Wait for implementation in OpenGL
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-31 14:57
 */
public class ShadowItem extends MediaItem {
    private int shadowColor;
    private int offsetX;
    private int offsetY;
    private float blur;
    private float spread;

    public ShadowItem(MediaItem baseItem) {
        super(TYPE_COMBINE, 0, baseItem);
    }
}
