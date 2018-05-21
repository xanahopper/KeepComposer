package com.gotokeep.keep.composer.overlay;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.timeline.OverlayItem;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 16:53
 */
public abstract class MediaOverlay extends RenderNode {
    protected static final int KEY_MAIN = 0;

    public static final int POSITION_LEFT = 0x00;
    public static final int POSITION_RIGHT = 0x01;
    public static final int POSITION_CENTER_HORIZONTAL = 0x02;
    public static final int POSITION_HORIZONTAL_MASK = 0x02;

    public static final int POSITION_VERTICAL_OFFSET = 2;
    public static final int POSITION_VERTICAL_MASK = 0x02 << POSITION_VERTICAL_OFFSET;
    public static final int POSITION_TOP = 0x00;
    public static final int POSITION_BOTTOM = 0x01 << POSITION_VERTICAL_OFFSET;
    public static final int POSITION_CENTER_VERTICAL = 0x02 << POSITION_VERTICAL_OFFSET;

    public static final int POSITION_CENTER = POSITION_CENTER_HORIZONTAL & POSITION_CENTER_VERTICAL;

    public static final String UNIFORM_OVERLAY_TRANSFORM = "uOverlayTransform";
    public static final String OVERLAY_UNIFORM_NAMES[] = {
            ProgramObject.UNIFORM_TRANSFORM_MATRIX,
            ProgramObject.UNIFORM_TEXTURE,
            UNIFORM_OVERLAY_TRANSFORM
    };

    private static final String OVERLAY_VERTEX_SHADER = "" +
            "attribute vec4 aPosition;    \n" +
            "attribute vec2 aTexCoords; \n" +
            "varying vec2 vTexCoords; \n" +
            "uniform mat4 uOverlayTransform;\n" +
            "uniform mat4 uTransformMatrix;\n" +
            "void main()                  \n" +
            "{                            \n" +
            "    gl_Position = uOverlayTransform * aPosition;  \n" +
            "    vTexCoords = (uTransformMatrix * vec4(aTexCoords, 0.0, 1.0)).st; \n" +
            "}                            \n";

    private int offsetX;
    private int offsetY;
    private float rotation;
    private float scale = 1f;
    private int position = 0;

    protected int width;
    protected int height;
    protected float overlayTransform[];
    protected ProgramObject overlayProgramObject;

    private static ProgramObject createOverlayProgramObject() {
        return new ProgramObject(OVERLAY_VERTEX_SHADER, ProgramObject.DEFAULT_FRAGMENT_SHADER, OVERLAY_UNIFORM_NAMES);
    }

    public void initWithMediaItem(OverlayItem item) {
        offsetX = item.getOffsetX();
        offsetY = item.getOffsetY();
        rotation = item.getRotation();
        scale = item.getScale();
    }

    @Override
    protected ProgramObject createProgramObject() {
        return ProgramObject.getDefaultProgram();
    }

    @CallSuper
    @Override
    protected void onPrepare() {
        overlayTransform = new float[16];
        overlayProgramObject = createOverlayProgramObject();
        overlayProgramObject.use();
        GLES20.glBindAttribLocation(programObject.getProgramId(), 0, ProgramObject.ATTRIBUTE_POSITION);
        GLES20.glBindAttribLocation(programObject.getProgramId(), 1, ProgramObject.ATTRIBUTE_TEX_COORDS);

        activeAttribData();
    }

    @Override
    protected void bindRenderTextures() {
        if (inputNodes.size() > 0) {
            inputNodes.get(0).getOutputTexture().bind(0);
        }
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        // draw source
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        renderOverlay(overlayProgramObject);
        return inputNodes.size() > 0 ? inputNodes.get(0).getRenderTimeUs() : 0;
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        if (inputNodes.size() > 0) {
            RenderNode node = inputNodes.get(0);
            GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                    1, false, node.getTransformMatrix(), 0);
        }
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
    }

    protected void updateLayerMatrix() {
        float sx = (float) width / originWidth * scale;
        float sy = (float) height / originHeight * scale;
        float left;
        float top;
        if ((position & POSITION_RIGHT) != 0) {
            left = 1f - sx;
        } else if ((position & POSITION_CENTER_HORIZONTAL) != 0) {
            left = 0;
        } else {
            left = sx - 1f;
        }
        if ((position & POSITION_BOTTOM) != 0) {
            top = sy - 1f;
        } else if ((position & POSITION_CENTER_VERTICAL) != 0) {
            top = 0;
        } else {
            top = 1f - sy;
        }
        left += (float) offsetX / originWidth;
        top -= (float) offsetY / originHeight;

        Matrix.setIdentityM(overlayTransform, 0);
        Matrix.translateM(overlayTransform, 0, left, top, 0f);
        Matrix.scaleM(overlayTransform, 0, sx, sy, 1.0f);
        Matrix.rotateM(overlayTransform, 0, rotation, 0f, 0f, 1f);

        GLES20.glUniformMatrix4fv(overlayProgramObject.getUniformLocation(UNIFORM_OVERLAY_TRANSFORM),
                1, false, overlayTransform, 0);
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    protected abstract void renderOverlay(ProgramObject overlayProgramObject);
}
