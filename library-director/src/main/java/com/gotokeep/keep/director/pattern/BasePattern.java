package com.gotokeep.keep.director.pattern;

import android.support.annotation.NonNull;

import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.director.ResourceManager;
import com.gotokeep.keep.director.VideoFragment;
import com.gotokeep.keep.director.data.DirectorScript;
import com.gotokeep.keep.director.exception.UnsuitableException;

import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-28 11:10
 */
public abstract class BasePattern {
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
                resourceManager.cacheFiile(resource);
            }
        }
    }

    public abstract Timeline selectVideos(@NonNull List<VideoFragment> videoSources, DirectorScript script) throws UnsuitableException;
}
