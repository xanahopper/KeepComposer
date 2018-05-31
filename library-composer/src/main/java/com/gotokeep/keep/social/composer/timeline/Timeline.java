package com.gotokeep.keep.social.composer.timeline;

import android.util.SparseArray;

import com.gotokeep.keep.social.composer.timeline.item.AudioItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:12
 */
public final class Timeline {
    private final List<Track> tracks = new ArrayList<>();
    private long endTimeMs;
    private AudioItem audioItem;

    public void addMediaTrack(Track track) {
        tracks.add(track);
        if (track.getEndTimeMs() > endTimeMs) {
            endTimeMs = track.getEndTimeMs();
        }
    }

    public void setAudioItem(AudioItem audioItem) {
        this.audioItem = audioItem;
    }

    public AudioItem getAudioItem() {
        return audioItem;
    }

    public SparseArray<List<MediaItem>> queryPresentationTimeItems(long presentationTimeUs) {
        SparseArray<List<MediaItem>> mediaItems = new SparseArray<>();
        for (Track track : tracks) {
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
                if (track.getEndTimeMs() > endTimeMs) {
                    endTimeMs = track.getEndTimeMs();
                }
            }
        }

        if (audioItem != null) {
            audioItem.setEndTimeMs(endTimeMs);
        }
    }

    public List<Track> getTracks() {
        return tracks;
    }
}
