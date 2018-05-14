package com.gotokeep.keep.composer.timeline;

import com.gotokeep.keep.composer.util.TimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:12
 */
public final class Timeline {
    SortedSet<MediaItem> items = new TreeSet<>();

    public void addMediaItem(MediaItem item) {
        items.add(item);
    }

    public List<MediaItem> queryPresentationTimeItems(long presentationTimeUs) {
        List<MediaItem> list = new LinkedList<>();
        for (MediaItem item : items) {
            if (TimeUtil.inRange(TimeUtil.usToMs(presentationTimeUs), item.startTimeMs, item.endTimeMs)) {
                list.add(item);
            }
        }

        return list;
    }
}
