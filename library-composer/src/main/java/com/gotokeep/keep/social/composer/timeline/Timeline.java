package com.gotokeep.keep.social.composer.timeline;

import android.util.SparseArray;

import com.gotokeep.keep.social.composer.timeline.item.AudioItem;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-04 12:00
 */
public interface Timeline {
    void addMediaTrack(Track track);

    void setAudioItem(AudioItem audioItem);

    AudioItem getAudioItem();

    SparseArray<List<MediaItem>> queryPresentationTimeItems(long presentationTimeUs);

    long getStartTimeMs();

    long getEndTimeMs();

    long getDurationMs();

    void prepare(RenderFactory renderFactory);

    List<Track> getTracks();
}
