package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.data.model.director.MetaInfo;
import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.director.KeepDirector;
import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;
import com.gotokeep.keep.social.director.exception.UnsuitableException;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-28 11:10
 */
public abstract class BasePattern {
    static final String TAG = BasePattern.class.getSimpleName();
    protected ResourceManager resourceManager;
    protected DirectorScript script;
    protected MetaInfo meta;

    public BasePattern(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    protected boolean isResourceAvailable(String resourcePath) {
        return resourceManager.isResourceCached(resourcePath);
    }

    protected String getResourcePath(String resourcePath) {
        return resourceManager.getCacheFilePath(resourcePath);
    }

    public void verifyAllResource(List<String> resourceList) {
        for (String resource : resourceList) {
            if (!resourceManager.isResourceCached(resource)) {
                resourceManager.cacheFile(resource);
            }
        }
    }

    public Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script, Timeline timeline) throws UnsuitableException {
        meta = script.getMeta();
        if (meta == null) {
            return null;
        }
        if (videoSources.size() < meta.getMinFragment()) {
            throw new UnsuitableException("video source is not suitable for this script.", meta.getMinFragment(), meta.getMaxFragment());
        }
        prepareGlobalInfo(videoSources, script);
        List<MediaItem> sourceItems = null;
        for (int i = 0; i < KeepDirector.DEFAULT_TRACK_COUNT; i++) {
            Track track;
            if (timeline.getTracks().size() < i) {
                track = new Track(true, i);
                timeline.addMediaTrack(track);
            } else {
                track = timeline.getTracks().get(i);
            }
            track.clear();
            switch (i) {
                case KeepDirector.LAYER_SOURCE:
                    sourceItems = generateSourceMediaItems(videoSources, script, track);
                    break;
                case KeepDirector.LAYER_TRANSITION:
                    generateTransitionMediaItems(sourceItems, script, track);
                    break;
                case KeepDirector.LAYER_FILTER:
                    generateFilterMediaItems(sourceItems, script, track);
                    break;
                case KeepDirector.LAYER_OVERLAY:
                    generateOverlayMediaItems(sourceItems, script, track);
                    break;
                default:
                    Log.w(TAG, "Generate unknown/unsupported layer track.");
                    // ignore
            }
        }
        AudioItem globalAudio = !TextUtils.isEmpty(meta.getMusic()) ? new AudioItem(getResourcePath(meta.getMusic())) : null;
        if (globalAudio != null) {
            timeline.setAudioItem(globalAudio);
        } else {
            timeline.setAudioItem(null);
        }
        return timeline;
    }

    protected abstract void prepareGlobalInfo(List<VideoFragment> videoSources, DirectorScript script);

    protected abstract List<MediaItem> generateSourceMediaItems(List<VideoFragment> videoSources, DirectorScript script, Track sourceTrack);

    protected abstract void generateTransitionMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track transitionTrack);

    protected abstract void generateFilterMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track filterTrack);

    protected abstract void generateOverlayMediaItems(List<MediaItem> sourceItems, DirectorScript script, Track overlayTrack);
}
