package com.gotokeep.keep.social.director;

import android.graphics.Color;
import android.text.TextUtils;

import com.gotokeep.keep.data.model.director.Chapter;
import com.gotokeep.keep.data.model.director.Filter;
import com.gotokeep.keep.data.model.director.MediaData;
import com.gotokeep.keep.data.model.director.Resource;
import com.gotokeep.keep.data.model.director.Transition;
import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.LayerItem;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

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

    protected static Map<String, ResourceMediaCreator<? extends MediaItem>> resourceCreatorMap = new HashMap<>();

    public static <M extends MediaData, T extends MediaItem> void
    registerCreator(Class<M> dataType, MediaItemCreator<M, T> creator) {
        creatorMap.put(dataType, creator);
    }

    public static <T extends MediaItem> void registerCreator(String typeName, ResourceMediaCreator<T> creator) {
        resourceCreatorMap.put(typeName, creator);
    }

    static {
        registerCreator(Chapter.class, (manager, item) -> {
            if (TextUtils.isEmpty(item.getSource())) {
                return null;
            }
            String mime = MediaUtil.getMime(item.getSource());
            MediaItem chapterItem = null;
            if (mime.startsWith("image/")) {
                chapterItem = new ImageItem(manager.getCacheFilePath(item.getSource()));
                item.setDuration(TimeUtil.secToMs(2));
            } else if (mime.startsWith("video/")) {
                chapterItem = new VideoItem(manager.getCacheFilePath(item.getSource()));
                item.setDuration(MediaUtil.getDuration(manager.getCacheFilePath(item.getSource())));
            }
            return chapterItem;
        });

        registerCreator(Filter.class, (manager, item) -> new FilterItem(item.getName(), item.getParams()));

        registerCreator(Transition.class, (manager, item) -> new TransitionItem(null, null, item.getDuration(), 0));

        registerCreator("title", (resourceManager, resource, mediaType) -> {
            if (TextUtils.isEmpty(resource.getType()) || "title".equals(resource.getType())) {
                TextItem textItem = new TextItem(0, resource.getValue());
                textItem.setPosition("center");
                textItem.setTextSize(64);
                textItem.setTextColor(Color.WHITE);
                textItem.setShadowColor(0x7F444444);
                textItem.setTimeRangeMs(0, TimeUtil.secToMs(2));
                return textItem;
            } else {
                return null;
            }
        });

        registerCreator("header", (resourceManager, resource, mediaType) -> {
            if (TextUtils.isEmpty(resource.getType()) || "layer".equals(resource.getType())) {
                LayerItem layerItem = new LayerItem(0, resource.getName());
                layerItem.setTimeRangeMs(0, TimeUtil.secToMs(2));
                layerItem.setPosition("center");
                return layerItem;
            } else {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <M extends Resource, T extends MediaItem> T
    createResourceItem(ResourceManager resourceManager, String type, M resource, Class<T> mediaType) {
        if (resource == null || mediaType == null) {
            return null;
        }
        ResourceMediaCreator<T> creator = (ResourceMediaCreator<T>) resourceCreatorMap.get(type);
        if (creator != null) {
            return creator.createMediaItem(resourceManager, resource, mediaType);
        } else {
            return null;
        }
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

    public interface ResourceMediaCreator<T extends MediaItem> {
        T createMediaItem(ResourceManager resourceManager, Resource resource, Class<T> mediaType);
    }
}
