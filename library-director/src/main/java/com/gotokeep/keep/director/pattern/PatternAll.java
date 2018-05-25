package com.gotokeep.keep.director.pattern;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gotokeep.keep.composer.timeline.item.AudioItem;
import com.gotokeep.keep.composer.timeline.item.FilterItem;
import com.gotokeep.keep.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.Track;
import com.gotokeep.keep.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.composer.timeline.item.VideoItem;
import com.gotokeep.keep.director.MediaFactory;
import com.gotokeep.keep.director.SelectPattern;
import com.gotokeep.keep.director.VideoFragment;
import com.gotokeep.keep.director.data.ChapterSet;
import com.gotokeep.keep.director.data.DefaultConfig;
import com.gotokeep.keep.director.data.DirectorScript;
import com.gotokeep.keep.director.data.MetaInfo;
import com.gotokeep.keep.director.data.Overlay;
import com.gotokeep.keep.director.data.Transition;
import com.gotokeep.keep.director.exception.UnsuitableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:20
 */
public class PatternAll implements SelectPattern {
    @Override
    public Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script) throws UnsuitableException {
        MetaInfo meta = script.getMeta();
        if (meta == null) {
            return null;
        }
        if (videoSources.size() < meta.getMinFragment() || videoSources.size() > meta.getMaxFragment()) {
            throw new UnsuitableException("video source is not suitable for this script.");
        }

        long sourceDurationMs = meta.getDuration();
        long totalDurationMs = sourceDurationMs;

        Timeline timeline = new Timeline();
        Track sourceTrack = new Track(false, 0);
        Track transitionTrack = new Track(false, 1);
        Track filterTrack = new Track(false, 2);
        Track overlayTrack = new Track(true, 3);
        VideoItem header = null;
        VideoItem footer = null;
        List<VideoItem> chapters = new ArrayList<>();
        List<OverlayItem> globalOverlays = new ArrayList<>();

        FilterItem globalFilter = MediaFactory.createMediaItem(meta.getFilter());
        if (globalFilter != null) {
            globalFilter.setTimeRangeMs(0, totalDurationMs);
        }
        if (meta.getOverlay() != null) {
            for (Overlay overlay : meta.getOverlay()) {
                globalOverlays.add(MediaFactory.createMediaItem(overlay, OverlayItem.class));
            }
        }
        AudioItem globalAudio = !TextUtils.isEmpty(meta.getMusic()) ? new AudioItem(meta.getMusic()) : null;
        timeline.setAudioItem(globalAudio);

        Transition globalTransition = null;
        ChapterSet chapterSet = script.getChapter();
        Map<String, Float> playSpeed = Collections.emptyMap();

        if (chapterSet != null) {
            DefaultConfig config = chapterSet.getDefaultConfig();
            globalTransition = config.getTransition();
            if (config.getPlaySpeed() != null) {
                playSpeed = config.getPlaySpeed();
            }
            header = MediaFactory.createMediaItem(chapterSet.getHeader());
            footer = MediaFactory.createMediaItem(chapterSet.getFooter());
            if (header != null) {
                sourceDurationMs -= chapterSet.getHeader().getDuration();
                header.setTimeRangeMs(0, chapterSet.getHeader().getDuration());
                sourceTrack.addMediaItem(header);
            }
            if (footer != null) {
                sourceDurationMs -= chapterSet.getFooter().getDuration();
                footer.setTimeRangeMs(totalDurationMs - chapterSet.getFooter().getDuration(), totalDurationMs);
                sourceTrack.addMediaItem(footer);
            }
        }
        long durationMs = sourceDurationMs / videoSources.size();
        for (int i = 0; i < videoSources.size(); i++) {
            VideoFragment video = videoSources.get(i);
            VideoItem chapter = new VideoItem(video.getFile());
            chapter.setTimeRangeMs(i * durationMs, (i + 1) * durationMs);
            chapter.setPlaySpeed(getPlaySpeed(video, playSpeed));
            chapters.add(chapter);
            sourceTrack.addMediaItem(chapter);
            if (globalTransition != null && i > 0) {
                TransitionItem transition = new TransitionItem(chapters.get(i - 1), chapters.get(i), globalTransition.getDuration(), 0);
                transitionTrack.addMediaItem(transition);
            }
        }

        return timeline;
    }

    private float getPlaySpeed(VideoFragment video, Map<String, Float> playSpeed) {
        float speed = 1f;
        for (String tag : video.getTag()) {
            if (playSpeed.containsKey(tag)) {
                speed = playSpeed.get(tag);
                break;
            }
        }
        return speed;
    }
}
