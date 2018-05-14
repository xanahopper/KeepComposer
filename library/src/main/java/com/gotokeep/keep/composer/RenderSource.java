package com.gotokeep.keep.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:36
 */
public abstract class RenderSource implements ComposerNode {
    private RenderTexture renderTexture;
    private boolean prepared = false;

    @Override
    public void prepare() {
        onPrepare();
        prepared = true;
    }

    @Override
    public boolean isPrepared() {
        return prepared;
    }

    @Override
    public void render(long presentationTimeUs) {

    }

    @Override
    public boolean awaitFrameAvailable() {
        return renderTexture.setRenderTarget();
    }

    protected abstract void onPrepare();
}
