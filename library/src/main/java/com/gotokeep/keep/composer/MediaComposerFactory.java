package com.gotokeep.keep.composer;

import android.os.Handler;

import com.gotokeep.keep.composer.timeline.RenderFactory;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 19:17
 */
public final class MediaComposerFactory {
    public static MediaComposer createMediaComposer(Handler eventHandler) {
        return new MediaComposerImpl(new RenderFactory(), eventHandler);
    }
}
