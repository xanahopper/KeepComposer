package com.gotokeep.keep.social.director.pattern;

import android.support.annotation.NonNull;

import com.gotokeep.keep.data.model.director.DirectorScript;
import com.gotokeep.keep.social.composer.timeline.Timeline;
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

    public abstract Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script, Timeline timeline) throws UnsuitableException;
}
