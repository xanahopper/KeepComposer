package com.gotokeep.keep.composer;


import android.view.Surface;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 10:03
 */
public abstract class RenderTarget {

    public abstract Surface getInputSurface();

    public abstract void updateFrame(RenderNode renderNode, long presentationTimeUs);

    public abstract void prepare();

    public abstract void complete();

    public abstract void release();
}
