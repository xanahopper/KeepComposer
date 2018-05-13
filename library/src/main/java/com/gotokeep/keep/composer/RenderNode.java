package com.gotokeep.keep.composer;

import android.util.SparseArray;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:28
 */
public abstract class RenderNode {
    protected SparseArray<RenderNode> inputNodes = new SparseArray<>();
    protected RenderTexture renderTexture;
    private boolean prepared = false;
    protected long startTimeMs;
    protected long endTimeMs;

    public void setInputNode(int inputIndex, RenderNode inputNode) {
        inputNodes.put(inputIndex, inputNode);
    }

    public RenderTexture getOutputTexture() {
        return renderTexture;
    }

    /**
     * after prepared, resource will be initialized in memory. And call {@link #release()} to release.
     */
    public void prepare() {
        renderTexture = createRenderTexture();
        onPrepare();
        prepared = true;
    }

    public void release() {
        onRelease();

        if (renderTexture != null) {
            renderTexture.release();
            renderTexture = null;
        }
        prepared = false;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public abstract void render(long presentationTimeUs);

    public abstract void awaitRenderFrame();

    protected abstract RenderTexture createRenderTexture();

    protected abstract void onPrepare();

    protected abstract void onRelease();
}
