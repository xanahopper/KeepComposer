package com.gotokeep.keep.social.composer.transition;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 16:49
 */
public final class MediaTransitionFactory {
    public static MediaTransition getTransition(String name, long durationMs) {
        MediaTransition transition = new BlackTransition();
        transition.durationMs = durationMs;
        return transition;
    }
}
