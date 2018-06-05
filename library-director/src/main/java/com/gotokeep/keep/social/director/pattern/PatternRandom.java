package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gotokeep.keep.data.model.director.Chapter;
import com.gotokeep.keep.data.model.director.ChapterSet;
import com.gotokeep.keep.data.model.director.DefaultConfig;
import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.MetaInfo;
import com.gotokeep.keep.data.model.director.Transition;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.director.MediaFactory;
import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;
import com.gotokeep.keep.social.director.exception.UnsuitableException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-05 17:10
 */
class PatternRandom extends BasePattern {
    public PatternRandom(ResourceManager resourceManager) {
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

        long totalDurationMs = meta.getDuration();

        Timeline timeline = new SourceTimeline();
        Track sourceTrack = new Track(true, 0);
        Track transitionTrack = new Track(true, 1);
        Track filterTrack = new Track(true, 2);
        Track overlayTrack = new Track(true, 3);
        OverlayItem header = MediaFactory.createResourceItem(resourceManager, "header", script.getHeader(), OverlayItem.class);
        if (header != null) {
            overlayTrack.addMediaItem(header);
        }
        VideoItem footer = MediaFactory.createMediaItem(resourceManager, script.getFooter());
        if (footer != null) {
            footer.setTimeRangeMs(totalDurationMs, totalDurationMs + script.getFooter().getDuration());
            sourceTrack.addMediaItem(footer);
        }
        List<VideoItem> chapters = new ArrayList<>();
        List<OverlayItem> globalOverlays = new ArrayList<>();
        if (meta.getTitle() != null) {
            TextItem titleItem = MediaFactory.createResourceItem(resourceManager, "title", meta.getTitle(), TextItem.class);
            if (titleItem != null) {
                globalOverlays.add(titleItem);
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

        long sourceDurationMs = 0;
        for (VideoFragment fragment : videoSources) {
            fragment.setDurationMs(MediaUtil.getDuration(fragment.getFile()));
            sourceDurationMs += fragment.getDurationMs();
        }

        long lastEndTimeMs = 0;
        if (chapterSet != null) {
            DefaultConfig config = chapterSet.getDefaultConfig();
            globalTransition = config.getTransition();
            if (config.getPlaySpeed() != null) {
                playSpeed = config.getPlaySpeed();
            }
            int chapterCount = chapterSet.getData() != null ? chapterSet.getData().size() : 0;
            int chapterIndex = 0;
            List<Chapter> chapterList = chapterSet.getData();
            while (chapterIndex < chapterCount) {
                Collections.shuffle(videoSources);
                int sourceIndex = 0;
                while (sourceIndex < videoSources.size()) {
                    Chapter chapter = chapterList.get(chapterIndex);
                    if (Chapter.TYPE_CHAPTER.equals(chapter.getType())) {
                        VideoFragment video = videoSources.get(sourceIndex);
                        VideoItem videoItem = MediaFactory.createMediaItem(resourceManager, chapter, VideoItem.class);
                        long startTimeMs = lastEndTimeMs;
                        long endTimeMs = lastEndTimeMs + chapter.getDuration();
                        lastEndTimeMs = endTimeMs;
                        videoItem.setTimeRangeMs(startTimeMs, endTimeMs);
                    } else if (Chapter.TYPE_FOOTAGE.equals(chapter.getType())) {
                        VideoItem videoItem = new VideoItem()
                    }
                }
                for (VideoFragment video : videoSources) {
                    VideoItem chapter = new VideoItem(resourceManager.getCacheFilePath(video.getFile()));
                    Chapter c = chapterList.get(chapterIndex);
                    long startTimeMs = lastEndTimeMs;
                    long endTimeMs = lastEndTimeMs + c.getDuration();
                    lastEndTimeMs = endTimeMs;
                    chapter.setTimeRangeMs(startTimeMs, endTimeMs);

                    if (globalTransition != null) {
                        if (i > 0) {
                            startTimeMs -= globalTransition.getDuration() / 2;
                        }
                        if (i < videoSources.size() - 1) {
                            endTimeMs += globalTransition.getDuration() / 2;
                        }
                    }

                    chapters.add(chapter);
                    chapterIndex += 1;
                    if (chapterIndex >= chapterCount) {
                        break;
                    }
                }
            }
        }

        long durationMs = 0;
        for (int i = 0; i < videoSources.size(); i++) {
            VideoFragment video = videoSources.get(i);
            VideoItem chapter = new VideoItem(resourceManager.getCacheFilePath(video.getFile()));
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

        timeline.addMediaTrack(sourceTrack);
        timeline.addMediaTrack(transitionTrack);
        timeline.addMediaTrack(filterTrack);
        timeline.addMediaTrack(overlayTrack);
        if (globalAudio != null) {
            timeline.setAudioItem(globalAudio);
        }

        return null;
    }
}
