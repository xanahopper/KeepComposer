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
    public static final int KEY_MAIN = 0;
    protected SparseArray<RenderNode> inputNodes = new SparseArray<>();
    protected RenderTexture renderTexture;
    protected ProgramObject programObject;

    private boolean prepared = false;
    protected long startTimeMs;
    protected long endTimeMs;
    protected long presentationTimeUs;
    protected int canvasWidth;
    protected int canvasHeight;
    protected int originWidth;
    protected int originHeight;
    protected boolean frameAvailable = false;

    protected static final float[] DEFAULT_VERTEX_DATA = {
            -1f, -1f, 0,
            1f, -1f, 0,
            -1f, 1f, 0,
            1f, 1f, 0};
    static final short[] DEFAULT_TEX_COORDS_DATA = {0, 0, 1, 0, 0, 1, 1, 1};

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer texCoordBuffer;

    public void setInputNode(int inputIndex, RenderNode inputNode) {
        inputNodes.put(inputIndex, inputNode);
    }

    public RenderNode getMainInputNode(long presentationTimeUs) {
        return inputNodes.size() > 0 ? inputNodes.valueAt(KEY_MAIN) : null;
    }

    public void setMainInputNode(RenderNode node) {
        if (node != null) {
            inputNodes.put(KEY_MAIN, node);
        } else {
            inputNodes.delete(KEY_MAIN);
        }
    }

    public RenderTexture getOutputTexture() {
        return renderTexture;
    }

    public long render(long positionUs, long elapsedRealtimeUs) {
        boolean allRendered = renderInputs(positionUs, elapsedRealtimeUs);
        if (needRenderSelf()) {
            setSelfRenderTarget(positionUs);
        }
        return doRender(programObject, positionUs);
    }

    public void setViewport(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public void setOriginSize(int width, int height) {
        originWidth = width;
        originHeight = height;
    }

    private void setSelfRenderTarget(long positionUs) {
        renderTexture.setRenderTarget(canvasWidth, canvasHeight);
        renderTexture.clear();
        if (programObject != null) {
            programObject.use();
            activeAttribData();
            bindRenderTextures();
            updateRenderUniform(programObject, positionUs);
        }
    }

    private boolean renderInputs(long positionUs, long elapsedRealtimeUs) {
        boolean shouldRender[] = new boolean[inputNodes.size()];
        boolean allRendered = true;
        for (int i = 0; i < inputNodes.size(); i++) {
            shouldRender[i] = shouldRenderNode(inputNodes.valueAt(i), positionUs);
            if (shouldRender[i]) {
                inputNodes.valueAt(i).render(positionUs, elapsedRealtimeUs);
            }
        }
        for (int i = 0; i < inputNodes.size(); i++) {
            if (shouldRender[i]) {
                allRendered &= inputNodes.valueAt(i).updateRenderFrame();
            }
        }
        return allRendered;
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
        if (programObject != null) {
            programObject.use();

            GLES20.glBindAttribLocation(programObject.getProgramId(), 0, ProgramObject.ATTRIBUTE_POSITION);
            GLES20.glBindAttribLocation(programObject.getProgramId(), 1, ProgramObject.ATTRIBUTE_TEX_COORDS);

            activeAttribData();
        }
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

    protected boolean needRenderSelf() {
        return true;
    }

    /**
     * Subclass should override this method to render the frame.
     * @param programObject program object will use
     * @param positionUs current time in microsecond
     * @return time in microsecond of the frame that just rendered
     */
    protected abstract long doRender(ProgramObject programObject, long positionUs);

    protected abstract void bindRenderTextures();

    protected abstract void updateRenderUniform(ProgramObject programObject, long presentationTimeUs);

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public boolean updateRenderFrame() {
        if (renderTexture != null && renderTexture.isFrameAvailable()) {
            renderTexture.updateTexImage();
            return true;
        }
        return false;
    }
}
