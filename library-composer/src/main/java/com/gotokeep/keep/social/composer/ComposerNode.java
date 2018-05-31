package com.gotokeep.keep.social.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 18:28
 */
public interface ComposerNode {
    void prepare();

    boolean isPrepared();

    void release();

    void render(long presentationTimeUs);

    boolean awaitFrameAvailable();
}
