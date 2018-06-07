package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.gotokeep.keep.data.model.director.ChapterSet;
import com.gotokeep.keep.data.model.director.DefaultConfig;
import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.MetaInfo;
import com.gotokeep.keep.data.model.director.Transition;
import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.director.KeepDirector;
import com.gotokeep.keep.social.director.MediaFactory;
import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;
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
    public Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script, Timeline timeline) throws UnsuitableException {
        MetaInfo meta = script.getMeta();
        if (meta == null) {
            return null;
        }
        if (videoSources.size() < meta.getMinFragment() || videoSources.size() > meta.getMaxFragment()) {
            throw new UnsuitableException("video source is not suitable for this script.");
        }

        long totalDurationMs = meta.getDuration();

        long sourceDurationMs = 0;
        for (VideoFragment fragment : videoSources) {
            fragment.setDurationMs(MediaUtil.getDuration(fragment.getFile()));
            sourceDurationMs += fragment.getDurationMs();
        }
        long durationMs = 0;
        if (sourceDurationMs > totalDurationMs) {
            durationMs = totalDurationMs / videoSources.size();
        } else {
            totalDurationMs = sourceDurationMs;
        }

        Track sourceTrack = new Track(true, KeepDirector.LAYER_SOURCE);
        Track transitionTrack = new Track(true, KeepDirector.LAYER_TRANSITION);
        Track filterTrack = new Track(true, KeepDirector.LAYER_FILTER);
        Track overlayTrack = new Track(true, KeepDirector.LAYER_OVERLAY);
        OverlayItem header = MediaFactory.createResourceItem(resourceManager, "header", script.getHeader(), OverlayItem.class);
        if (header != null) {
            overlayTrack.addMediaItem(header);
        }
        List<MediaItem> chapters = new ArrayList<>();
        List<OverlayItem> globalOverlays = new ArrayList<>();
        if (meta.getTitle() != null) {
            TextItem titleItem = MediaFactory.createResourceItem(resourceManager, "title", meta.getTitle(), TextItem.class);
            if (titleItem != null) {
                globalOverlays.add(titleItem);
                overlayTrack.addMediaItem(titleItem);
            }
        }
        FilterItem globalFilter = MediaFactory.createMediaItem(resourceManager, meta.getFilter());
        if (globalFilter != null) {
            globalFilter.setTimeRangeMs(0, totalDurationMs);
            filterTrack.addMediaItem(globalFilter);
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
        }

        long lastEndTimeMs = 0;
        for (int i = 0; i < videoSources.size(); i++) {
            VideoFragment video = videoSources.get(i);
            String mime = MediaUtil.getMime(video.getFile());

            MediaItem chapter = null;
            if (mime != null) {
                if (mime.startsWith(MediaUtil.VIDEO_MIME_START)) {
                    chapter = new VideoItem(resourceManager.getCacheFilePath(video.getFile()));
                } else if (mime.startsWith(MediaUtil.IMAGE_MIME_START)) {
                    chapter = new ImageItem(resourceManager.getCacheFilePath(video.getFile()));
                }
            }
            if (chapter == null) {
                Log.w(TAG, "unsupported VideoFragment ");
                continue;
            }
            long startTimeMs = durationMs > 0 ? i * durationMs : lastEndTimeMs;
            long endTimeMs = durationMs > 0 ? (i + 1) * durationMs : lastEndTimeMs + video.getDurationMs();
            lastEndTimeMs = endTimeMs;
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

        VideoItem footer = MediaFactory.createMediaItem(resourceManager, script.getFooter());
        if (footer != null) {
            footer.setTimeRangeMs(lastEndTimeMs, lastEndTimeMs + script.getFooter().getDuration());
            sourceTrack.addMediaItem(footer);
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
