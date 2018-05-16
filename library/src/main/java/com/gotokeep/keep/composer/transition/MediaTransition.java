package com.gotokeep.keep.composer.transition;

import android.opengl.GLES20;
import android.support.annotation.CallSuper;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;

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
            "uniform mat4 uStartTransform\n" +
            "uniform mat4 uEndTransform\n" +
            "void main()                  \n" +
            "{                            \n" +
            "    gl_Position = aPosition;  \n" +
            "    vStartTexCoords = (uStartTransform * vec4(aTexCoords, 0, 0)).xy; \n" +
            "    uEndTransform = (uEndTransform * vec4(aTexCoords, 0, 0)).xy; \n" +
            "}  ";
    private static final String TAG = MediaTransition.class.getSimpleName();

    long durationMs;
    RenderNode startNode;
    RenderNode endNode;

    @Override
    public RenderNode getMainInputNode(long presentationTimeUs) {
        for (int i = 0; i < inputNodes.size(); i++) {
            RenderNode node = inputNodes.valueAt(i);
            if (shouldRenderNode(node, presentationTimeUs)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void setInputNode(int inputIndex, RenderNode inputNode) {
        super.setInputNode(inputIndex, inputNode);
        if (inputIndex == INDEX_START) {
            inputNode.setEndTimeMs(inputNode.getEndTimeMs() + durationMs / 2);
        } else if (inputIndex == INDEX_END) {
            inputNode.setStartTimeMs(Math.max(inputNode.getEndTimeMs() + durationMs / 2, 0));
        }
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @CallSuper
    @Override
    protected void onPrepare() {
        startNode = getStartNode();
        endNode = getEndNode();
        if (startNode != null &&!startNode.isPrepared()) {
            startNode.prepare();
        }
        if (endNode != null && !endNode.isPrepared()) {
            endNode.prepare();
        }
        long midTimeMs = startNode != null ? startNode.getEndTimeMs() : 0;
        startTimeMs = Math.max(midTimeMs - durationMs / 2, 0);
        endTimeMs = Math.max(midTimeMs + durationMs / 2, 0);

        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_START_TEXTURE), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_END_TEXTURE), 1);
    }

    RenderNode getEndNode() {
        return inputNodes.get(INDEX_END);
    }

    RenderNode getStartNode() {
        return inputNodes.get(INDEX_START);
    }

    @Override
    protected void onRelease() {
        if (startNode.isPrepared()) {
            startNode.release();
        }
        if (endNode.isPrepared()) {
            endNode.release();
        }
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        long presentationTimeUs = positionUs;
        for (int i = 0; i < inputNodes.size(); i++) {
            RenderNode node = inputNodes.valueAt(i);
            if (node.getPresentationTimeUs() > presentationTimeUs) {
                presentationTimeUs = node.getPresentationTimeUs();
            }
        }
        this.presentationTimeUs = presentationTimeUs;
        return presentationTimeUs;
    }

    @Override
    protected void bindRenderTextures(boolean[] shouldRender) {
        startNode = getStartNode();
        endNode = getEndNode();
        if (shouldRender.length > INDEX_START &&shouldRender[INDEX_START] && startNode != null) {
            startNode.getOutputTexture().bind(0);
        } else {
            RenderTexture.unbind(0);
        }

        if (shouldRender.length > INDEX_END && shouldRender[INDEX_END] && endNode != null) {
            endNode.getOutputTexture().bind(1);
        } else {
            RenderTexture.unbind(0);
        }
    }
}
