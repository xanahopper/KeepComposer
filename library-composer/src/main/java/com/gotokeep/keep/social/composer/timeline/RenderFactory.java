package com.gotokeep.keep.social.composer.timeline;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.filter.MediaFilter;
import com.gotokeep.keep.social.composer.filter.FilterFactory;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.overlay.SubtitleOverlay;
import com.gotokeep.keep.social.composer.overlay.WatermarkOverlay;
import com.gotokeep.keep.social.composer.source.ImageMediaSource;
import com.gotokeep.keep.social.composer.source.VideoMediaSource;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.LayerItem;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.timeline.item.WatermarkItem;
import com.gotokeep.keep.social.composer.transition.MediaTransitionFactory;

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
    private Context context;

    public RenderFactory(Context context, OverlayProvider overlayProvider) {
        this.context = context;
        this.overlayProvider = overlayProvider;
        registerRenderType(VideoItem.class, item -> {
            VideoMediaSource source = new VideoMediaSource(item.getFilePath());
            source.setPlaySpeed(item.getPlaySpeed());
            source.setScaleType(item.getScaleType());
            return source;
        });
        registerRenderType(ImageItem.class, item -> {
            ImageMediaSource source = new ImageMediaSource(item.getFilePath());
            source.setScaleType(item.getScaleType());
            return source;
        });
        registerRenderType(TransitionItem.class, item -> MediaTransitionFactory.getTransition(item.getName(), item.getDurationMs()));
        registerRenderType(LayerItem.class, item -> {
            String filePath = (TextUtils.isEmpty(item.getUrl()) && overlayProvider != null) ? overlayProvider.getLayerImagePath(item.getName()) : item.getUrl();
            MediaOverlay overlay = new LayerOverlay(filePath);
            overlay.initWithMediaItem(item);
            return overlay;
        });
        registerRenderType(TextItem.class, item -> {
            MediaOverlay overlay = new SubtitleOverlay(context);
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

        registerRenderType(FilterItem.class, item -> {
            MediaFilter filter = FilterFactory.getExternalFilter(item.getName());
            if (filter != null) {
                filter.setFilterParameters(item.getParams());
                filter.setStartTimeMs(item.getStartTimeMs());
                filter.setEndTimeMs(item.getEndTimeMs());
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
