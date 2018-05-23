package com.gotokeep.keep.composer.timeline;

import android.util.Log;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.filter.MediaFilter;
import com.gotokeep.keep.composer.filter.MediaFilterFactory;
import com.gotokeep.keep.composer.overlay.LayerOverlay;
import com.gotokeep.keep.composer.overlay.MediaOverlay;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.overlay.SubtitleOverlay;
import com.gotokeep.keep.composer.overlay.WatermarkOverlay;
import com.gotokeep.keep.composer.source.ImageMediaSource;
import com.gotokeep.keep.composer.source.VideoMediaSource;
import com.gotokeep.keep.composer.transition.MediaTransition;
import com.gotokeep.keep.composer.transition.MediaTransitionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 16:44
 */
public class RenderFactory {
    private static final String TAG = RenderFactory.class.getSimpleName();

    public interface RenderCreator<T extends MediaItem, R extends RenderNode> {
        R createRenderNode(T mediaItem);
    }

    private final Map<Class<? extends MediaItem>,
            RenderCreator<? extends MediaItem, ? extends RenderNode>> renderCreatorMap = new HashMap<>();
    private Map<MediaItem, RenderNode> renderNodeCache = new HashMap<>();
    private OverlayProvider overlayProvider;

    public RenderFactory(OverlayProvider overlayProvider) {
        this.overlayProvider = overlayProvider;
        registerRenderType(VideoItem.class, item -> {
            VideoMediaSource source = new VideoMediaSource(item.filePath);
            source.setPlaySpeed(item.playSpeed);
            return source;
        });
        registerRenderType(ImageItem.class, item -> new ImageMediaSource(item.filePath));
        registerRenderType(TransitionItem.class, item -> MediaTransitionFactory.getTransition(item.name, item.durationMs));
        registerRenderType(LayerItem.class, item -> {
            MediaOverlay overlay = new LayerOverlay(overlayProvider.getLayerImagePath(item.name));
            overlay.initWithMediaItem(item);
            return overlay;
        });
        registerRenderType(WatermarkItem.class, item -> {
            MediaOverlay overlay = WatermarkOverlay.newBuilder()
                    .setImageLeft(item.getImageLeft())
                    .setImageTop(item.getImageTop())
                    .setImageScale(1f)
                    .setText(item.getText())
                    .setTextLeft(item.getTextHorizontalPoint())
                    .setTextTop(item.getTextTop())
                    .setTextColor(item.getTextColor())
                    .setTextSize(item.getTextSize())
                    .build();
            overlay.initWithMediaItem(item);
            return overlay;
        });
//        registerRenderType(OverlayItem.class, item -> {
//            MediaOverlay overlay = null;
//            if (OverlayItem.TYPE_LAYER.equals(item.type)) {
//                overlay = new LayerOverlay(overlayProvider.getLayerImagePath(item.name));
//            } else if (OverlayItem.TYPE_WATERMARK.equals(item.type)) {
//                overlay = new WatermarkOverlay();
//            } else if (OverlayItem.TYPE_SUBTITLE.equals(item.type)) {
//                overlay = new SubtitleOverlay();
//            }
//            if (overlay != null) {
//                overlay.initWithMediaItem(item);
//            }
//            return overlay;
//        });
        registerRenderType(FilterItem.class, item -> {
            MediaFilter filter = MediaFilterFactory.getFilter(item.name);
            if (filter != null) {
                filter.setFilterParameters(item.params);
            }
            return filter;
        });
        registerRenderType(AudioItem.class, item -> null);
    }

    <T extends MediaItem, R extends RenderNode> void
        registerRenderType(Class<T> itemType, RenderCreator<T, R> creator) {
        renderCreatorMap.put(itemType, creator);
    }

    @SuppressWarnings("unchecked")
    public RenderNode createRenderNode(MediaItem mediaItem) {
        RenderNode node = null;
        if (!renderNodeCache.containsKey(mediaItem)) {
            Class<? extends MediaItem> clazz = mediaItem.getClass();
            RenderCreator<MediaItem, RenderNode> creator = (RenderCreator<MediaItem, RenderNode>) renderCreatorMap.get(clazz);
            if (creator != null) {
                node = creator.createRenderNode(mediaItem);
                if (node != null) {
                    node.setStartTimeMs(mediaItem.startTimeMs);
                    node.setEndTimeMs(mediaItem.endTimeMs);
                }
            }
            if (node == null) {
                Log.w(TAG, "createRenderNode: a null node put in cache.");
            }
            renderNodeCache.put(mediaItem, node);
        } else {
            node = renderNodeCache.get(mediaItem);
        }

        return node;
    }
}