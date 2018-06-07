package com.gotokeep.keep.social.director.pattern;

import com.gotokeep.keep.social.director.ResourceManager;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-07 15:55
 */
public class PatternLoop extends PatternSelect {
    public PatternLoop(ResourceManager resourceManager) {
        super(resourceManager);
    }

    @Override
    protected int getSourceIndex(int sourceCount, int index) {
        return index % sourceCount;
    }

    @Override
    protected int getStartIndex(int sourceCount, int chapterCount) {
        return 0;
    }
}
