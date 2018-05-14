package com.gotokeep.keep.composer.transition;

import android.opengl.GLES20;
import android.support.annotation.CallSuper;
import android.util.Log;

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

    protected long durationMs;
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
        startNode.awaitRenderFrame();
        endNode.awaitRenderFrame();
        super.render(presentationTimeUs);
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

        startTimeMs = startNode.getEndTimeMs() - durationMs / 2;
        endTimeMs = endNode.getStartTimeMs() + durationMs / 2;

        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_START_TEXTURE), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(UNIFORM_END_TEXTURE), 1);
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
    protected void onRender(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    protected void bindRenderTextures() {
        startNode.getOutputTexture().bind(0);
        endNode.getOutputTexture().bind(1);
    }

    protected abstract boolean shouldRenderStartNode(long presentationTimeUs);

    protected abstract boolean shouldRenderEndNode(long presentationTimeUs);
}
