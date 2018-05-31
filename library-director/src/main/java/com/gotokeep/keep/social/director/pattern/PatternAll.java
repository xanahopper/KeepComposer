package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.director.MediaFactory;
import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;
import com.gotokeep.keep.social.director.data.ChapterSet;
import com.gotokeep.keep.social.director.data.DefaultConfig;
import com.gotokeep.keep.social.director.data.DirectorScript;
import com.gotokeep.keep.social.director.data.MetaInfo;
import com.gotokeep.keep.social.director.data.Overlay;
import com.gotokeep.keep.social.director.data.Transition;
import com.gotokeep.keep.social.director.exception.UnsuitableException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:20
 */
public class PatternAll extends BasePattern {
    public PatternAll(ResourceManager resourceManager) {
        super(resourceManager);
    }

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
        Track sourceTrack = new Track(true, 0);
        Track transitionTrack = new Track(true, 1);
        Track filterTrack = new Track(true, 2);
        Track overlayTrack = new Track(true, 3);
        VideoItem header = null;
        VideoItem footer = null;
        List<VideoItem> chapters = new ArrayList<>();
        List<OverlayItem> globalOverlays = new ArrayList<>();

        FilterItem globalFilter = MediaFactory.createMediaItem(resourceManager, meta.getFilter());
        if (globalFilter != null) {
            globalFilter.setTimeRangeMs(0, totalDurationMs);
            filterTrack.addMediaItem(globalFilter);
        }
        if (meta.getOverlay() != null) {
            for (Overlay overlay : meta.getOverlay()) {
                OverlayItem overlayItem = MediaFactory.createMediaItem(resourceManager, overlay, OverlayItem.class);
                globalOverlays.add(overlayItem);
                overlayTrack.addMediaItem(overlayItem);
            }
        }
        AudioItem globalAudio = !TextUtils.isEmpty(meta.getMusic()) ? new AudioItem(getResourcePath(meta.getMusic())) : null;
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
            header = MediaFactory.createMediaItem(resourceManager, chapterSet.getHeader());
            footer = MediaFactory.createMediaItem(resourceManager, chapterSet.getFooter());
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
            VideoItem chapter = new VideoItem(resourceManager.getCacheFilePath(video.getFile()));
            long startTimeMs = i * durationMs;
            long endTimeMs = (i + 1) * durationMs;
            if (globalTransition != null) {
                if (i > 0) {
                    startTimeMs -= globalTransition.getDuration() / 2;
                }
                if (i < videoSources.size() - 1) {
                    endTimeMs += globalTransition.getDuration() / 2;
                }
            }
            long chapterDurationMs = endTimeMs - startTimeMs;
            chapter.setTimeRangeMs(startTimeMs, endTimeMs);
            chapter.setPlaySpeed(getPlaySpeed(video, playSpeed, chapterDurationMs));
            chapters.add(chapter);
            sourceTrack.addMediaItem(chapter);
            if (globalTransition != null && i > 0) {
                TransitionItem transition = new TransitionItem(chapters.get(i - 1), chapter, globalTransition.getDuration(), 0);
                transition.setName(globalTransition.getName());
                transitionTrack.addMediaItem(transition);
            }
        }

        timeline.addMediaTrack(sourceTrack);
        timeline.addMediaTrack(transitionTrack);
        timeline.addMediaTrack(filterTrack);
        timeline.addMediaTrack(overlayTrack);
        if (globalAudio != null) {
            timeline.setAudioItem(globalAudio);
        }
        return timeline;
    }

    private float getPlaySpeed(VideoFragment video, Map<String, Float> playSpeed, long durationMs) {
        float speed = 1f;
        if (video.getTag() != null) {
            for (String tag : video.getTag()) {
                if (playSpeed.containsKey(tag)) {
                    speed = playSpeed.get(tag);
                    break;
                }
            }
        } else {
            speed = (float) MediaUtil.getDuration(resourceManager.getCacheFilePath(video.getFile())) / durationMs;
            if (speed > 3f) {
                speed = 3f;
            }
        }
        return speed;
    }
}
