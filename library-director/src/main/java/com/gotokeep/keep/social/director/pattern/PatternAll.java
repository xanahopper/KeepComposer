package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.Nullable;

import com.gotokeep.keep.data.model.director.ChapterSet;
import com.gotokeep.keep.data.model.director.DefaultConfig;
import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.Transition;
import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.director.MediaFactory;
import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;

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

    private long totalDurationMs;
    private long durationMs = 0;
    private long sourceDurationMs = 0;
    private Map<String, Float> playSpeed = Collections.emptyMap();
    private Transition globalTransition = null;
    private boolean hasFooter = false;

    @Nullable
    private MediaItem createSourceItem(VideoFragment video) {
        MediaItem chapter = null;
        String mime = MediaUtil.getMime(video.getFile());

        if (mime != null) {
            if (mime.startsWith(MediaUtil.VIDEO_MIME_START)) {
                chapter = new VideoItem(resourceManager.getCacheFilePath(video.getFile()));
            } else if (mime.startsWith(MediaUtil.IMAGE_MIME_START)) {
                chapter = new ImageItem(resourceManager.getCacheFilePath(video.getFile()));
            }
        }
        return chapter;
    }

    @Override
    protected void prepareGlobalInfo(List<VideoFragment> videoSources, DirectorScript script) {
        totalDurationMs = meta.getDuration();
        for (VideoFragment fragment : videoSources) {
            fragment.setDurationMs(MediaUtil.getDuration(fragment.getFile()));
            sourceDurationMs += fragment.getDurationMs();
        }
        if (sourceDurationMs > totalDurationMs) {
            durationMs = totalDurationMs / videoSources.size();
        } else {
            durationMs = 0;
            totalDurationMs = sourceDurationMs;
        }

        ChapterSet chapterSet = script.getChapter();

        if (chapterSet != null) {
            DefaultConfig config = chapterSet.getDefaultConfig();
            globalTransition = config.getTransition();
            if (Transition.isAvailable(globalTransition)) {
                if (globalTransition.getDuration() == 0) {
                    globalTransition.setDuration(Transition.DEFAULT_DURATION);
                }
            } else {
                globalTransition = null;
            }
            if (config.getPlaySpeed() != null) {
                playSpeed = config.getPlaySpeed();
            }
        }
    }

    @Override
    protected List<MediaItem> generateSourceMediaItems(List<VideoFragment> videoSources, DirectorScript script, Track sourceTrack) {
        long lastEndTimeMs = 0;
        List<MediaItem> items = new ArrayList<>();
        List<VideoFragment> sources = onPreGenerateSourceMediaItems(videoSources);
        int sourceCount = Math.min(videoSources.size(), meta.getMaxFragment());
        for (int i = 0; i < sourceCount; i++) {
            VideoFragment video = sources.get(i);
            MediaItem sourceItem = createSourceItem(video);
            if (sourceItem == null) {
                continue;
            }
            long startTimeMs = durationMs > 0 ? i * durationMs : lastEndTimeMs;
            long endTimeMs = durationMs > 0 ? (i + 1) * durationMs : lastEndTimeMs + video.getDurationMs();
            long chapterDurationMs = endTimeMs - startTimeMs;
            sourceItem.setTimeRangeMs(startTimeMs, endTimeMs);
            sourceItem.setPlaySpeed(getPlaySpeed(video, playSpeed, chapterDurationMs));
            items.add(sourceItem);
            sourceTrack.addMediaItem(sourceItem);

            lastEndTimeMs = endTimeMs;
            if (Transition.isAvailable(globalTransition)) {
                lastEndTimeMs -= globalTransition.getDuration();
            }
        }

        VideoItem footer = MediaFactory.createMediaItem(resourceManager, script.getFooter());
        if (footer != null) {
            footer.setTimeRangeMs(lastEndTimeMs, lastEndTimeMs + script.getFooter().getDuration());
            sourceTrack.addMediaItem(footer);
            hasFooter = true;
        } else {
            hasFooter = false;
        }
        return items;
    }

    protected List<VideoFragment> onPreGenerateSourceMediaItems(List<VideoFragment> videoSources) {
        return videoSources;
    }

    @Override
    protected void generateTransitionMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track transitionTrack) {
        if (Transition.isAvailable(globalTransition)) {
            int sourceCount = hasFooter ? sourceItems.size() - 1 : sourceItems.size();
            for (int i = 0; i < sourceCount; i++) {
                if (i > 0) {
                    TransitionItem transition = new TransitionItem(sourceItems.get(i - 1), sourceItems.get(i), globalTransition.getDuration(), 0);
                    transition.setName(globalTransition.getName());
                    transitionTrack.addMediaItem(transition);
                }
            }
        }
    }

    @Override
    protected void generateFilterMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track filterTrack) {
        FilterItem globalFilter = MediaFactory.createMediaItem(resourceManager, meta.getFilter());
        if (globalFilter != null) {
            globalFilter.setTimeRangeMs(0, totalDurationMs);
            filterTrack.addMediaItem(globalFilter);
        }
    }

    @Override
    protected void generateOverlayMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track overlayTrack) {
        OverlayItem header = MediaFactory.createResourceItem(resourceManager, "header", script.getHeader(), OverlayItem.class);
        if (header != null) {
            overlayTrack.addMediaItem(header);
        }
        if (meta.getTitle() != null) {
            TextItem titleItem = MediaFactory.createResourceItem(resourceManager, "title", meta.getTitle(), TextItem.class);
            if (titleItem != null) {
                overlayTrack.addMediaItem(titleItem);
            }
        }
    }

    private float getPlaySpeed(VideoFragment video, Map<String, Float> playSpeed, long durationMs) {
        float speed = 1f;
        if (video != null && video.getTag() != null) {
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
