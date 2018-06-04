package com.gotokeep.keep.social.director;

import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.LayerItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.timeline.item.WatermarkItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.data.model.director.Chapter;
import com.gotokeep.keep.data.model.director.Filter;
import com.gotokeep.keep.data.model.director.MediaData;
import com.gotokeep.keep.data.model.director.Overlay;
import com.gotokeep.keep.data.model.director.Transition;

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
        registerCreator(Chapter.class, (manager, item) -> {
            String mime = MediaUtil.getMime(item.getSource());
            if (mime.startsWith("image/")) {
                return new ImageItem(manager.getCacheFilePath(item.getSource()));
            } else if (mime.startsWith("video/")) {
                return new VideoItem(manager.getCacheFilePath(item.getSource()));
            } else {
                return null;
            }
        });

        registerCreator(Filter.class, (manager, item) -> new FilterItem(item.getName(), item.getParams()));

        registerCreator(Transition.class, (manager, item) -> new TransitionItem(null, null, item.getDuration(), 0));

        registerCreator(Overlay.class, (manager, item) -> {
            if (item.getLayer() != null) {
                LayerItem layerItem = new LayerItem(manager.getCacheFilePath(item.getLayer().getUrl()));
                layerItem.setTimeRangeMs(item.getStartTime(), item.getEndTime());
                layerItem.setScale(item.getScale());
                layerItem.setOffsetX(item.getOffsetX());
                layerItem.setOffsetY(item.getOffsetY());
                layerItem.setRotation(item.getRotation());
                layerItem.setPosition(item.getPosition());
                return layerItem;
            } else {
                return new WatermarkItem(0);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <M extends MediaData, T extends MediaItem> T
    createMediaItem(ResourceManager resourceManager, M mediaData) {
        if (mediaData == null) {
            return null;
        }
        MediaItemCreator<M, T> creator = (MediaItemCreator<M, T>) creatorMap.get(mediaData.getClass());
        if (creator != null) {
            return creator.createMediaItem(resourceManager, mediaData);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <M extends MediaData, T extends MediaItem> T
    createMediaItem(ResourceManager resourceManager, M mediaData, Class<T> dataType) {
        if (mediaData == null) {
            return null;
        }
        MediaItemCreator<M, T> creator = (MediaItemCreator<M, T>) creatorMap.get(mediaData.getClass());
        if (creator != null) {
            return creator.createMediaItem(resourceManager, mediaData);
        } else {
            return null;
        }
    }

    public interface MediaItemCreator<M extends MediaData, T extends MediaItem> {
        T createMediaItem(ResourceManager resourceManager, M mediaData);
    }
}
