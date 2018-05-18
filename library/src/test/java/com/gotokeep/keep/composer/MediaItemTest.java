package com.gotokeep.keep.composer;

import com.gotokeep.keep.composer.timeline.LayerItem;
import com.gotokeep.keep.composer.timeline.MediaItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class MediaItemTest {
    @Test
    public void sortComparatorTest() {
        List<MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(new LayerItem(2, ""));
        mediaItems.add(new LayerItem(1, ""));
        mediaItems.add(new LayerItem(3, ""));
        mediaItems.add(new LayerItem(0, ""));
        Collections.sort(mediaItems, MediaItem.getTypeComparator());
        assertEquals(0, mediaItems.get(0).getLayer());
        assertEquals(1, mediaItems.get(1).getLayer());
    }
}