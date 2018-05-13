package com.gotokeep.keep.composer.transition;

import android.support.annotation.CallSuper;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 17:03
 */
public abstract class MediaTransition extends RenderNode {
    public static final int INDEX_START = 0;
    public static final int INDEX_END = 1;

    protected long durationMs;
    private ProgramObject programObject;
    protected RenderNode startNode;
    protected RenderNode endNode;

    @Override
    public void render(long presentationTimeUs) {
        if (shouldRenderStartNode(presentationTimeUs)) {
            startNode.render(presentationTimeUs);
        }
        if (shouldRenderEndNode(presentationTimeUs)) {
            endNode.render(presentationTimeUs);
        }
        updateRenderUniform(programObject, presentationTimeUs);
    }

    @Override
    public void awaitRenderFrame() {
        renderTexture.awaitFrameAvailable();
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @CallSuper
    @Override
    protected void onPrepare() {
        startNode = inputNodes.get(INDEX_START);
        endNode = inputNodes.get(INDEX_END);
        if (!startNode.isPrepared()) {
            startNode.prepare();
        }
        if (!endNode.isPrepared()) {
            endNode.prepare();
        }
        programObject = createProgramObject();
        startTimeMs = startNode.getEndTimeMs() - durationMs / 2;
        endTimeMs = endNode.getStartTimeMs() + durationMs / 2;
    }

    @Override
    protected void onRelease() {

    }

    protected abstract ProgramObject createProgramObject();

    protected abstract void updateRenderUniform(ProgramObject programObject, long presentationTimeUs);

    protected abstract boolean shouldRenderStartNode(long presentationTimeUs);

    protected abstract boolean shouldRenderEndNode(long presentationTimeUs);
}
