package com.gotokeep.keep.composer.timeline;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:12
 */
public final class Timeline {
    private final List<Track> tracks = new ArrayList<>();
    private long endTimeMs;

    public void addMediaTrack(Track track) {
        tracks.add(track);
        if (track.getEndTimeMs() > endTimeMs) {
            endTimeMs = track.getEndTimeMs();
        }
    }

    public SparseArray<List<MediaItem>> queryPresentationTimeItems(long presentationTimeUs) {
        SparseArray<List<MediaItem>> mediaItems = new SparseArray<>();
        ListIterator<Track> iterator = tracks.listIterator();
        while (iterator.hasNext()) {
            Track track = iterator.next();
            mediaItems.put(track.getLayer(), track.queryPresentationTimeItems(presentationTimeUs));
        }
        return mediaItems;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public void prepare(RenderFactory renderFactory) {
        for (Track track : tracks) {
            if (track != null) {
                track.prepare(renderFactory);
            }
        }
    }

    public List<Track> getTracks() {
        return tracks;
    }
}
