package com.gotokeep.keep.social.composer.transition;

import android.text.TextUtils;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 16:49
 */
public final class MediaTransitionFactory {
    public static final String FADE = "fade";
    public static final String BLACK = "black";
    public static final String WHITE = "white";

    public static MediaTransition getTransition(String name, long durationMs) {
        MediaTransition transition = null;
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        switch (name) {
            case FADE:
                transition = new FadeTransition();
                break;
            case BLACK:
                transition = new BlackTransition();
                break;
            case WHITE:
                transition = new WhiteTransition();
                break;
        }
        if (transition != null) {
            transition.durationMs = durationMs;
        }
        return transition;
    }
}
