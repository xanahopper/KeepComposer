package com.gotokeep.keep.composer;

import android.content.Context;
import android.os.Handler;

import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.RenderFactory;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 19:17
 */
public final class MediaComposerFactory {
    public static MediaComposer createMediaComposer(Context context, OverlayProvider overlayProvider, Handler eventHandler) {
        return new MediaComposerImpl(new RenderFactory(context, overlayProvider), eventHandler);
    }
}
