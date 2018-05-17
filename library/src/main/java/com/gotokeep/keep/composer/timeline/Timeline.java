package com.gotokeep.keep.composer.timeline;

import com.gotokeep.keep.composer.util.TimeUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:12
 */
public final class Timeline {
    List<MediaItem> items = new LinkedList<>();
    long endTimeMs;

    public void addMediaItem(MediaItem item) {
        items.add(item);
        if (item.endTimeMs > endTimeMs) {
            endTimeMs = item.endTimeMs;
        }
    }

    public LinkedList<MediaItem> queryPresentationTimeItems(long presentationTimeUs) {
        LinkedList<MediaItem> list = new LinkedList<>();
        ListIterator<MediaItem> iterator = items.listIterator();
        int layer = 0;
        while (iterator.hasNext()) {
            MediaItem item = iterator.next();
            if (item.inRange(TimeUtil.usToMs(presentationTimeUs))) {
                item.layer = layer++;
                list.add(item);
            }
        }
        return list;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }
}
