package com.gotokeep.keep.director;

import com.gotokeep.keep.composer.timeline.MediaItem;
import com.gotokeep.keep.director.data.MediaData;

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
