package com.gotokeep.keep.director;

import com.gotokeep.keep.composer.filter.FilterFactory;
import com.gotokeep.keep.composer.timeline.MediaItem;
import com.gotokeep.keep.composer.timeline.item.FilterItem;
import com.gotokeep.keep.composer.timeline.item.ImageItem;
import com.gotokeep.keep.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.composer.timeline.item.VideoItem;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.director.data.Chapter;
import com.gotokeep.keep.director.data.Filter;
import com.gotokeep.keep.director.data.MediaData;
import com.gotokeep.keep.director.data.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 14:29
 */
public final class MediaFactory {
    protected static Map<Class<? extends MediaData>, MediaItemCreator<? extends MediaData, ? extends MediaItem>>
        creatorMap = new HashMap<>();

    public static <M extends MediaData, T extends MediaItem> void
        registerCreator(Class<M> dataType, MediaItemCreator<M, T> creator) {
        creatorMap.put(dataType, creator);
    }

    static {
        registerCreator(Chapter.class, item -> {
            String mime = MediaUtil.getMime(item.getSource());
            if (mime.startsWith("image/")) {
                return new ImageItem(item.getSource());
            } else if (mime.startsWith("video/")) {
                return new VideoItem(item.getSource());
            } else {
                return null;
            }
        });

        registerCreator(Filter.class, item -> new FilterItem(item.getName(), item.getParams()));

        registerCreator(Transition.class, item -> new TransitionItem(null, null, item.getDuration(), 0));
    }

    @SuppressWarnings("unchecked")
    public static <M extends MediaData, T extends MediaItem> T
    createMediaItem(M mediaData) {
        if (mediaData == null) {
            return null;
        }
        MediaItemCreator<M, T> creator = (MediaItemCreator<M, T>) creatorMap.get(mediaData.getClass());
        if (creator != null) {
            return creator.createMediaItem(mediaData);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends MediaData, T extends MediaItem> T
    createMediaItem(M mediaData, Class<T> dataType) {
        if (mediaData == null) {
            return null;
        }
        MediaItemCreator<M, T> creator = (MediaItemCreator<M, T>) creatorMap.get(mediaData.getClass());
        if (creator != null) {
            return creator.createMediaItem(mediaData);
        } else {
            return null;
        }
    }

    public interface MediaItemCreator<M extends MediaData, T extends MediaItem> {
        T createMediaItem(M mediaData);
    }
}
