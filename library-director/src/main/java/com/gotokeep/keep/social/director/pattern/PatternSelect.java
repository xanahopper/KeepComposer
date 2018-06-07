package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.gotokeep.keep.data.model.director.Chapter;
import com.gotokeep.keep.data.model.director.ChapterSet;
import com.gotokeep.keep.data.model.director.DefaultConfig;
import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.Filter;
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
abstract class PatternSelect extends BasePattern {
    public PatternSelect(ResourceManager resourceManager) {
        super(resourceManager);
    }

    private long totalDurationMs;
    private long sourceDurationMs;
    private Map<String, Float> playSpeed = Collections.emptyMap();
    private Transition globalTransition = null;
    private boolean hasFooter = false;
    private List<Chapter> chapters;

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
        totalDurationMs = 0;
        sourceDurationMs = 0;

        ChapterSet chapterSet = script.getChapter();
        if (chapterSet != null) {
            chapters = chapterSet.getData();
            if (chapters != null) {
                for (Chapter chapter : chapters) {
                    totalDurationMs += chapter.getDuration();
                }
            }
        }
        for (VideoFragment fragment : videoSources) {
            fragment.setDurationMs(MediaUtil.getDuration(fragment.getFile()));
            sourceDurationMs += fragment.getDurationMs();
        }


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
        List<MediaItem> items = new ArrayList<>();
        if (chapters == null) {
            return items;
        }
        long lastEndTimeMs = 0;
        int startIndex = getStartIndex(videoSources.size(), chapters.size());
        for (int i = startIndex; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            MediaItem sourceItem;
            if (!TextUtils.isEmpty(chapter.getSource())) {
                sourceItem = MediaFactory.createMediaItem(resourceManager, chapter);
            } else {
                int sourceIndex = getSourceIndex(videoSources.size(), i);
                if (sourceIndex < 0 || sourceIndex >= videoSources.size()) {
                    break;
                }
                VideoFragment video = videoSources.get(sourceIndex);
                sourceItem = createSourceItem(video);
            }
            if (sourceItem == null) {
                break;
            }
            long startTimeMs = lastEndTimeMs;
            long endTimeMs = lastEndTimeMs + sourceItem.getDurationMs();
            sourceItem.setTimeRangeMs(startTimeMs, endTimeMs);
            sourceItem.setPlaySpeed(chapter.getPlaySpeed());
            items.add(sourceItem);
            sourceTrack.addMediaItem(sourceItem);

            Transition transition = null;
            if (Transition.isAvailable(chapter.getTransition())) {
                transition = chapter.getTransition();
                if (transition.getDuration() == 0) {
                    transition.setDuration(Transition.DEFAULT_DURATION);
                }
            } else if (Transition.isAvailable(globalTransition)) {
                transition = globalTransition;
            }
            lastEndTimeMs = endTimeMs - (transition != null ? transition.getDuration() : 0);
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

    @Override
    protected void generateTransitionMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track transitionTrack) {
        int sourceCount = hasFooter ? sourceItems.size() - 1 : sourceItems.size();
        for (int i = 0; i < sourceCount; i++) {
            if (i > 0 && i < chapters.size()) {
                Transition transition = null;
                if (Transition.isAvailable(chapters.get(i - 1).getTransition())) {
                    transition = chapters.get(i - 1).getTransition();
                    if (transition.getDuration() == 0) {
                        transition.setDuration(Transition.DEFAULT_DURATION);
                    }
                } else if (Transition.isAvailable(globalTransition)) {
                    transition = globalTransition;
                }
                if (transition != null) {
                    TransitionItem transitionItem = new TransitionItem(sourceItems.get(i - 1), sourceItems.get(i), transition.getDuration(), 0);
                    transition.setName(transition.getName());
                    transitionTrack.addMediaItem(transitionItem);
                }
            }
        }
    }

    @Override
    protected void generateFilterMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track filterTrack) {
//        FilterItem globalFilter = MediaFactory.createMediaItem(resourceManager, );
        Filter globalFilter = meta.getFilter();
        int sourceCount = hasFooter ? sourceItems.size() - 1 : sourceItems.size();
        FilterItem lastFilter = null;
        for (int i = 0; i < sourceCount; i++) {
            Chapter chapter = chapters.get(i);
            Filter filter = globalFilter;
            if (Filter.isAvailable(chapter.getFilter())) {
                filter = chapter.getFilter();
            }
            MediaItem sourceItem = sourceItems.get(i);
            if (Filter.isAvailable(filter)) {
                if (lastFilter == null) {
                    lastFilter = MediaFactory.createMediaItem(resourceManager, filter);
                    lastFilter.setTimeRangeMs(sourceItem.getStartTimeMs(), sourceItem.getEndTimeMs());
                } else if (lastFilter.getName().equals(filter.getName())) {
                    lastFilter.setEndTimeMs(sourceItem.getEndTimeMs());
                } else {
                    filterTrack.addMediaItem(lastFilter);
                    lastFilter = MediaFactory.createMediaItem(resourceManager, filter);
                    lastFilter.setTimeRangeMs(sourceItem.getStartTimeMs(), sourceItem.getEndTimeMs());
                }
            } else {
                if (lastFilter != null) {
                    filterTrack.addMediaItem(lastFilter);
                    lastFilter = null;
                }
            }
        }
        if (filterTrack.getMediaItemCount() == 0 && lastFilter != null) {
            filterTrack.addMediaItem(lastFilter);
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

    protected abstract int getSourceIndex(int sourceCount, int index);

    protected abstract int getStartIndex(int sourceCount, int chapterCount);
}
