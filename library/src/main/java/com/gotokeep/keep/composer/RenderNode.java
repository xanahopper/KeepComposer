package com.gotokeep.keep.composer;

import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.TimeUtil;

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
    protected long presentationTimeUs;
    protected int canvasWidth;
    protected int canvasHeight;
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

    public long render(long positionUs, long elapsedRealtimeUs) {
        boolean[] shouldRender = renderInputs(positionUs, elapsedRealtimeUs);
        setSelfRenderTarget(positionUs, shouldRender);
        return doRender(programObject, positionUs);
    }

    public void setViewport(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    private void setSelfRenderTarget(long positionUs, boolean[] shouldRender) {
        renderTexture.setRenderTarget(canvasWidth, canvasHeight);
        renderTexture.clear();
        if (programObject != null) {
            programObject.use();
            activeAttribData();
            bindRenderTextures(shouldRender);
            updateRenderUniform(programObject, positionUs);
        }
//        GLES20.glViewport(0, 0, canvasWidth, canvasHeight);
    }

    private boolean[] renderInputs(long positionUs, long elapsedRealtimeUs) {
        boolean shouldRender[] = new boolean[inputNodes.size()];
        for (int i = 0; i < inputNodes.size(); i++) {
            shouldRender[i] = shouldRenderNode(inputNodes.valueAt(i), positionUs);
            if (shouldRender[i]) {
                inputNodes.valueAt(i).render(positionUs, elapsedRealtimeUs);
            }
        }
        for (int i = 0; i < inputNodes.size(); i++) {
            if (shouldRender[i]) {
                inputNodes.valueAt(i).updateRenderFrame();
//                Log.w(TAG, "one of input frame invalid");
            }
        }
        return shouldRender;
    }

    /**
     * after prepared, resource will be initialized in memory. And call {@link #release()} to release.
     */
    public void prepare() {
        prepareInternal();
        prepareInput();
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

        activeAttribData();
    }

    protected void activeAttribData() {
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, texCoordBuffer);
        GLES20.glEnableVertexAttribArray(1);
    }

    private void prepareInput() {
        for (int i = 0; i < inputNodes.size(); i++) {
            RenderNode node = inputNodes.valueAt(i);
            if (node != null && !node.isPrepared()) {
                node.prepare();
            }
        }
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

    public long getPresentationTimeUs() {
        return presentationTimeUs;
    }

    public float[] getTransformMatrix() {
        return renderTexture.getTransitionMatrix();
    }

    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public boolean awaitRenderFrame() {
        frameAvailable = renderTexture.awaitFrameAvailable();
        return frameAvailable;
    }

    public RenderNode getMainInputNode(long presentationTimeUs) {
        return inputNodes.size() > 0 ? inputNodes.valueAt(0) : null;
    }

    public boolean isInRange(long timeMs) {
        return TimeUtil.inRange(timeMs, startTimeMs, endTimeMs);
    }

    protected boolean shouldRenderNode(RenderNode renderNode, long presentationTimeUs) {
        return true;
    }

    protected abstract RenderTexture createRenderTexture();

    protected abstract ProgramObject createProgramObject();

    protected abstract void onPrepare();

    protected abstract void onRelease();

    /**
     * Subclass should override this method to render the frame.
     * @param programObject program object will use
     * @param positionUs current time in microsecond
     * @return time in microsecond of the frame that just rendered
     */
    protected abstract long doRender(ProgramObject programObject, long positionUs);

    protected abstract void bindRenderTextures(boolean[] shouldRender);

    protected abstract void updateRenderUniform(ProgramObject programObject, long presentationTimeUs);

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public void updateRenderFrame() {
        if (renderTexture != null && renderTexture.getSurfaceTexture() != null) {
            renderTexture.getSurfaceTexture().updateTexImage();
        }
    }
}
