package com.gotokeep.keep.composer.timeline;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.filter.MediaFilter;
import com.gotokeep.keep.composer.filter.MediaFilterFactory;
import com.gotokeep.keep.composer.overlay.LayerOverlay;
import com.gotokeep.keep.composer.overlay.MediaOverlay;
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
    public interface RenderCreator<T extends MediaItem, R extends RenderNode> {
        R createRenderNode(T mediaItem);
    }

    private final Map<Class<? extends MediaItem>,
            RenderCreator<? extends MediaItem, ? extends RenderNode>> renderCreatorMap = new HashMap<>();
    private Map<MediaItem, RenderNode> renderNodeCache = new HashMap<>();

    public RenderFactory() {
        registerRenderType(VideoItem.class, item -> new VideoMediaSource(item.filePath));
        registerRenderType(ImageItem.class, item -> new ImageMediaSource(item.filePath));
        registerRenderType(TransitionItem.class, item -> {
            MediaTransition transition = MediaTransitionFactory.getTransition(item.name);
            if (transition != null) {
                RenderNode startNode = createRenderNode(item.startItem);
                RenderNode endNode = createRenderNode(item.endItem);
                transition.setInputNode(MediaTransition.INDEX_START, startNode);
                transition.setInputNode(MediaTransition.INDEX_END, endNode);
            }
            return transition;
        });
        registerRenderType(OverlayItem.class, item -> {
            RenderNode baseNode = createRenderNode(item.baseItem);
            MediaOverlay overlay = null;
            if (OverlayItem.TYPE_LAYER.equals(item.type)) {
                overlay = new LayerOverlay(baseNode);
            } else if (OverlayItem.TYPE_WATERMARK.equals(item.type)) {
                overlay = new WatermarkOverlay(baseNode);
            } else if (OverlayItem.TYPE_SUBTITLE.equals(item.type)) {
                overlay = new SubtitleOverlay(baseNode);
            }
            if (overlay != null) {
                overlay.initWithMediaItem(item);
            }
            return overlay;
        });
        registerRenderType(FilterItem.class, item -> {
            MediaFilter filter = MediaFilterFactory.getFilter(item.name);
            if (filter != null) {
                RenderNode baseNode = createRenderNode(item.baseItem);
                filter.setInputNode(MediaFilter.KEY_MAIN, baseNode);
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
            RenderCreator<MediaItem, RenderNode> creator = (RenderCreator<MediaItem, RenderNode>) renderCreatorMap.get(mediaItem.getClass());
            if (creator != null) {
                node = creator.createRenderNode(mediaItem);
                if (node != null) {
                    node.setStartTimeMs(mediaItem.startTimeMs);
                    node.setEndTimeMs(mediaItem.endTimeMs);
                }
            }
            renderNodeCache.put(mediaItem, node);
        } else {
            node = renderNodeCache.get(mediaItem);
        }

        return node;
    }
}
