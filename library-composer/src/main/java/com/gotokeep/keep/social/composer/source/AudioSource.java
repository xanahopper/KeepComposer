package com.gotokeep.keep.social.composer.source;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:59
 */
public class AudioSource extends MediaSource {
    @Override
    public long findClosestKeyTime(long presentationTimeMs) {
        return presentationTimeMs;
    }
}
