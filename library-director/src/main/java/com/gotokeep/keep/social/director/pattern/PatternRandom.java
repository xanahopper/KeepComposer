package com.gotokeep.keep.social.director.pattern;

import com.gotokeep.keep.social.director.ResourceManager;
import com.gotokeep.keep.social.director.VideoFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-07 15:30
 */
public class PatternRandom extends PatternAll {
    public PatternRandom(ResourceManager resourceManager) {
        super(resourceManager);
    }

    @Override
    protected List<VideoFragment> onPreGenerateSourceMediaItems(List<VideoFragment> videoSources) {
        List<VideoFragment> result = new ArrayList<>(videoSources);
        Collections.shuffle(result);
        return result;
    }
}
