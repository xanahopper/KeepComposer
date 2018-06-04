package com.gotokeep.keep.social.composer.timeline;

import android.util.SparseArray;

import com.gotokeep.keep.social.composer.TimeRange;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-04 11:52
 */
public class ClippingTimeline implements Timeline {
    private Timeline sourceTimeline;
    private TimeRange clippingRange;

    public ClippingTimeline(Timeline timeline, long startTimeMs, long endTimeMs) {
        this(timeline, new TimeRange(startTimeMs, endTimeMs));
    }

    public ClippingTimeline(Timeline timeline, TimeRange clippingRange) {
        sourceTimeline = timeline;
        this.clippingRange = clippingRange;
    }

    @Override
    public void addMediaTrack(Track track) {
        sourceTimeline.addMediaTrack(track);
    }

    @Override
    public void setAudioItem(AudioItem audioItem) {
        sourceTimeline.setAudioItem(audioItem);
    }

    @Override
    public AudioItem getAudioItem() {
        return sourceTimeline.getAudioItem();
    }

    @Override
    public SparseArray<List<MediaItem>> queryPresentationTimeItems(long presentationTimeUs) {
        return sourceTimeline.queryPresentationTimeItems(presentationTimeUs);
    }

    @Override
    public long getStartTimeMs() {
        return clippingRange.startTimeMs;
    }

    @Override
    public long getEndTimeMs() {
        return clippingRange.endTimeMs;
    }

    @Override
    public long getDurationMs() {
        return clippingRange.durationMs();
    }

    @Override
    public void prepare(RenderFactory renderFactory) {
        sourceTimeline.prepare(renderFactory);
    }

    @Override
    public List<Track> getTracks() {
        return sourceTimeline.getTracks();
    }
}
