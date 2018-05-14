package com.gotokeep.keep.composer.timeline;

import com.gotokeep.keep.composer.RenderNode;
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
    private Map<MediaItem, RenderNode> renderNodeCache = new HashMap<>();

    public RenderNode createRenderNode(MediaItem mediaItem) {
        RenderNode node = null;
        if (!renderNodeCache.containsKey(mediaItem)) {
            if (mediaItem instanceof VideoItem) {
                node = new VideoMediaSource(((VideoItem) mediaItem).filePath);
            } else if (mediaItem instanceof ImageItem) {
                node = new ImageMediaSource(((ImageItem) mediaItem).filePath);
            } else if (mediaItem instanceof FilterItem) {
                node = null;
            } else if (mediaItem instanceof TransitionItem) {
                node = MediaTransitionFactory.getTransition(((TransitionItem) mediaItem).name);
                RenderNode startNode = createRenderNode(((TransitionItem) mediaItem).startItem);
                RenderNode endNode = createRenderNode(((TransitionItem) mediaItem).endItem);
                node.setInputNode(MediaTransition.INDEX_START, startNode);
                node.setInputNode(MediaTransition.INDEX_END, endNode);
            } else if (mediaItem instanceof OverlayItem) {
                node = null;
            } else if (mediaItem instanceof AudioItem) {
                node = null;
            }
            renderNodeCache.put(mediaItem, node);
        }

        return node;
    }
}
