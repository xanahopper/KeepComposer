package com.gotokeep.keep.composer.transition;

import android.opengl.GLES20;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.gotokeep.keep.composer.RenderNode;
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

    static final String UNIFORM_START_TEXTURE = "uStartTexture";
    static final String UNIFORM_END_TEXTURE = "uEndTexture";
    static final String UNIFORM_START_TRANSFORM = "uStartTransform";
    static final String uNIFORM_END_TRANSFORM = "uEndTransform";

    static final String DEFAULT_VERTEX_SHADER = "" +
            "attribute vec4 aPosition;    \n" +
            "attribute vec2 aTexCoords; \n" +
            "varying vec2 vStartTexCoords; \n" +
            "varying vec2 vEndTexCoords; \n" +
            "uniform mat4 uStartTransform;\n" +
            "uniform mat4 uEndTransform;\n" +
            "void main()                  \n" +
            "{                            \n" +
            "    gl_Position = aPosition;  \n" +
            "    vStartTexCoords = (uStartTransform * vec4(aTexCoords, 0.0, 1.0)).st; \n" +
            "    vEndTexCoords = (uEndTransform * vec4(aTexCoords, 0.0, 1.0)).st; \n" +
            "}  ";
    private static final String TAG = MediaTransition.class.getSimpleName();

    long durationMs;
    RenderNode startNode;
    RenderNode endNode;

    @CallSuper
    @Override
    protected void onPrepare() {
        startNode = getStartNode();
        endNode = getEndNode();
        if (startNode != null && !startNode.isPrepared()) {
            startNode.prepare();
        }
        if (endNode != null && !endNode.isPrepared()) {
            endNode.prepare();
        }

        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_START_TEXTURE), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_END_TEXTURE), 1);
    }

    @Override
    protected void onRelease() {

    }

    RenderNode getEndNode() {
        return inputNodes.size() > INDEX_END ? inputNodes.get(INDEX_END) : null;
    }

    RenderNode getStartNode() {
        return inputNodes.size() > INDEX_START ? inputNodes.get(INDEX_START) : null;
    }

    @Override
    public boolean isFrameAvailable() {
        return false;
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        startNode = getStartNode();
        endNode = getEndNode();
        if (startNode != null) {
            Log.d(TAG, "doRender: startNode.renderTimeUs = " + startNode.getRenderTimeUs() + ", " + positionUs);
        }
        if (endNode != null) {
            Log.d(TAG, "doRender: endNode.renderTimeUs = " + endNode.getRenderTimeUs());
        }
        long startTimeUs = startNode != null ? startNode.getPresentationTimeUs() : Long.MAX_VALUE;
        long endTimeUs = endNode != null ? endNode.getPresentationTimeUs() : Long.MAX_VALUE;
        long timeUs = Math.min(startTimeUs, endTimeUs);
        presentationTimeUs = timeUs != Long.MAX_VALUE ? timeUs : positionUs;
        return presentationTimeUs + TimeUtil.msToUs(startTimeMs);
    }

    @Override
    protected void bindRenderTextures() {
        startNode = getStartNode();
        endNode = getEndNode();
        if (startNode != null) {
            startNode.getOutputTexture().bind(0);
        }

        if (endNode != null) {
            endNode.getOutputTexture().bind(1);
        }
    }

    @Override
    protected void unbindRenderTextures() {
        startNode = getStartNode();
        endNode = getEndNode();
        if (startNode != null) {
            startNode.getOutputTexture().unbind(0);
        }

        if (endNode != null) {
            endNode.getOutputTexture().unbind(1);
        }
    }

    @Override
    protected boolean shouldRenderNode(RenderNode renderNode, long presentationTimeUs) {
        Log.d(TAG, String.format("presentationTime = %d, renderNode.time = %d",
                presentationTimeUs, renderNode.getRenderTimeUs()));
        return presentationTimeUs >= renderNode.getRenderTimeUs();
    }

    @Override
    protected String getName() {
        return "MediaTransition@" + hashCode();
    }
}
