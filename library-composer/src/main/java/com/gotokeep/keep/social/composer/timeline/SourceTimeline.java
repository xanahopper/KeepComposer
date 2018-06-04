package com.gotokeep.keep.social.composer.timeline;

import android.util.SparseArray;

import com.gotokeep.keep.social.composer.TimeRange;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:12
 */
public class SourceTimeline implements Timeline {
    private final List<Track> tracks = new ArrayList<>();
    private TimeRange timelineRange = new TimeRange();
    private AudioItem audioItem;

    @Override
    public void addMediaTrack(Track track) {
        tracks.add(track);
        if (track.getEndTimeMs() > timelineRange.endTimeMs) {
            timelineRange.endTimeMs = track.getEndTimeMs();
        }
    }

    @Override
    public void setAudioItem(AudioItem audioItem) {
        this.audioItem = audioItem;
    }

    @Override
    public AudioItem getAudioItem() {
        return audioItem;
    }

    @Override
    public SparseArray<List<MediaItem>> queryPresentationTimeItems(long presentationTimeUs) {
        SparseArray<List<MediaItem>> mediaItems = new SparseArray<>();
        for (Track track : tracks) {
            mediaItems.put(track.getLayer(), track.queryPresentationTimeItems(presentationTimeUs));
        }
        return mediaItems;
    }

    @Override
    public long getStartTimeMs() {
        return timelineRange.startTimeMs;
    }

    @Override
    public long getEndTimeMs() {
        return timelineRange.endTimeMs;
    }

    @Override
    public long getDurationMs() {
        return timelineRange.durationMs();
    }

    @Override
    public void prepare(RenderFactory renderFactory) {
        for (Track track : tracks) {
            if (track != null) {
                track.prepare(renderFactory);
                if (track.getEndTimeMs() > timelineRange.endTimeMs) {
                    timelineRange.endTimeMs = track.getEndTimeMs();
                }
            }
        }

        if (audioItem != null) {
            audioItem.setEndTimeMs(timelineRange.endTimeMs);
        }
    }

    @Override
    public List<Track> getTracks() {
        return tracks;
    }
}
