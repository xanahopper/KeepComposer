package com.gotokeep.keep.composer;

import android.opengl.GLES20;

import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 渲染节点将输入进行处理，绘制于内部的 {@link RenderTexture} 中以供下一个渲染节点使用
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:28
 */
public abstract class RenderNode {
    private static final String TAG = RenderNode.class.getSimpleName();
    protected List<RenderNode> inputNodes = new ArrayList<>();
    protected RenderTexture renderTexture;
    protected ProgramObject programObject;

    private boolean prepared = false;
    protected long startTimeMs;
    protected long endTimeMs;
    protected long presentationTimeUs;
    protected long renderTimeUs;
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

    public void addInputNode(RenderNode inputNode) {
        inputNodes.add(inputNode);
    }

    public RenderTexture getOutputTexture() {
        return renderTexture;
    }

    public long acquireFrame(long positionUs) {
        long renderTimeUs = 0;
        if (!isFrameAvailable()) {
            renderTimeUs = render(positionUs);
        }
        updateRenderFrame(positionUs);
        this.renderTimeUs = renderTimeUs;
        return renderTimeUs;
    }

    public long render(long positionUs) {
        renderInputs(positionUs);
        if (needRenderSelf()) {
            setSelfRenderTarget(positionUs);
        }
        return doRender(programObject, positionUs);
    }

    public boolean isFrameAvailable() {
        return renderTexture.isFrameAvailable();
    }

    protected void renderInputs(long positionUs) {
        for (int i = 0; i < inputNodes.size(); i++) {
            RenderNode node = inputNodes.get(i);
            if (shouldRenderNode(node, positionUs)) {
                node.acquireFrame(positionUs);
            }
        }
    }

    protected boolean needRenderSelf() {
        return true;
    }

    protected void setSelfRenderTarget(long positionUs) {
        renderTexture.setRenderTarget(canvasWidth, canvasHeight);
        renderTexture.clear();
        if (programObject != null) {
            programObject.use();
            activeAttribData();
            bindRenderTextures();
            updateRenderUniform(programObject, positionUs);
        }
    }

    public void setViewport(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public void setOriginSize(int width, int height) {
        originWidth = width;
        originHeight = height;
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
        renderTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE);
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
            RenderNode node = inputNodes.get(i);
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

    public void setStartTimeMs(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public void setEndTimeMs(long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public long updateRenderFrame(long positionUs) {
        if (renderTexture != null && renderTexture.isFrameAvailable()) {
            renderTexture.updateTexImage();
            return getPresentationTimeUs();
        }
        return positionUs;
    }

    public boolean isInRange(long timeMs) {
        return TimeUtil.inRange(timeMs, startTimeMs, endTimeMs);
    }

    protected boolean shouldRenderNode(RenderNode renderNode, long presentationTimeUs) {
        return true;
    }

    protected abstract ProgramObject createProgramObject();

    protected abstract void onPrepare();

    protected abstract void onRelease();

    /**
     * Subclass should override this method to render the frame.
     *
     * @param programObject program object will use
     * @param positionUs    current time in microsecond
     * @return time in microsecond of the frame that just rendered
     */
    protected abstract long doRender(ProgramObject programObject, long positionUs);

    protected abstract void bindRenderTextures();

    protected abstract void updateRenderUniform(ProgramObject programObject, long presentationTimeUs);

    public long getRenderTimeUs() {
        return renderTimeUs;
    }
}
