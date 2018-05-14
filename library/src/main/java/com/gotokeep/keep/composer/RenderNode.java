package com.gotokeep.keep.composer;

import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import com.gotokeep.keep.composer.gles.ProgramObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:28
 */
public abstract class RenderNode {

    private static final String TAG = RenderNode.class.getSimpleName();
    protected SparseArray<RenderNode> inputNodes = new SparseArray<>();
    protected RenderTexture renderTexture;
    protected ProgramObject programObject;

    private boolean prepared = false;
    protected long startTimeMs;
    protected long endTimeMs;
    protected boolean frameAvailable = false;

    private static final float[] DEFAULT_VERTEX_DATA = {
            -1f, -1f, 0,
            1f, -1f, 0,
            -1f, 1f, 0,
            1f, 1f, 0};
    private static final short[] DEFAULT_TEX_COORDS_DATA = {0, 0, 1, 0, 0, 1, 1, 1};

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer texCoordBuffer;

    public void setInputNode(int inputIndex, RenderNode inputNode) {
        inputNodes.put(inputIndex, inputNode);
    }

    public RenderTexture getOutputTexture() {
        return renderTexture;
    }

    public void render(long presentationTimeUs) {
        for (int i = 0; i < inputNodes.size(); i++) {
            inputNodes.valueAt(i).render(presentationTimeUs);
        }
        for (int i = 0; i < inputNodes.size(); i++) {
            if (!inputNodes.valueAt(i).awaitRenderFrame()) {
                Log.w(TAG, "one of input frame invalid");
            }
        }
        if (programObject != null) {
            programObject.use();
            bindRenderTextures();
            updateRenderUniform(programObject, presentationTimeUs);
        }
        renderTexture.setRenderTarget();
        onRender(programObject, presentationTimeUs);
    }

    /**
     * after prepared, resource will be initialized in memory. And call {@link #release()} to release.
     */
    public void prepare() {
        prepareInternal();

        onPrepare();
        prepared = true;
    }

    public void release() {
        onRelease();

        if (renderTexture != null) {
            renderTexture.release();
            renderTexture = null;
        }
        if (programObject != null) {
            programObject.release();
            programObject = null;
        }
        prepared = false;
    }

    private void prepareInternal() {
        renderTexture = createRenderTexture();
        vertexBuffer = ByteBuffer.allocateDirect(DEFAULT_VERTEX_DATA.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(DEFAULT_VERTEX_DATA).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(DEFAULT_TEX_COORDS_DATA.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        texCoordBuffer.put(DEFAULT_TEX_COORDS_DATA).position(0);

        programObject = createProgramObject();
        programObject.use();

        GLES20.glBindAttribLocation(programObject.getProgramId(), 0, ProgramObject.ATTRIBUTE_POSITION);
        GLES20.glBindAttribLocation(programObject.getProgramId(), 1, ProgramObject.ATTRIBUTE_TEX_COORDS);

        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, texCoordBuffer);
        GLES20.glEnableVertexAttribArray(1);
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

    public void getTransformMatrix(float matrix[]) {
        renderTexture.getSurfaceTexture().getTransformMatrix(matrix);
    }

    public boolean awaitRenderFrame() {
        frameAvailable = renderTexture.awaitFrameAvailable();
        return frameAvailable;
    }

    protected abstract RenderTexture createRenderTexture();

    protected abstract ProgramObject createProgramObject();

    protected abstract void onPrepare();

    protected abstract void onRelease();

    protected abstract void onRender(ProgramObject programObject, long presentationTimeUs);

    protected abstract void bindRenderTextures();

    protected abstract void updateRenderUniform(ProgramObject programObject, long presentationTimeUs);
}
