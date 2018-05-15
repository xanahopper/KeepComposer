package com.gotokeep.keep.composer.overlay;

import android.graphics.Matrix;
import android.opengl.GLES20;

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
    private static final int KEY_MAIN = 0;

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

    private int offsetX;
    private int offsetY;
    private float rotation;
    private float scale = 1f;
    private int position = 0;

    protected int width;
    protected int height;
    protected Matrix layerMatrix = new Matrix();

    MediaOverlay(RenderNode mainInputNode) {
        setInputNode(KEY_MAIN, mainInputNode);
    }

    public void initWithMediaItem(OverlayItem item) {
        offsetX = item.getOffsetX();
        offsetY = item.getOffsetY();
        rotation = item.getRotation();
        scale = item.getScale();
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    protected void updateLayerMatrix() {
        int left = 0;
        int top = 0;
        if ((position & POSITION_RIGHT) != 0) {
            left = canvasWidth - width;
        } else if ((position & POSITION_CENTER_HORIZONTAL) != 0) {
            left = (canvasWidth - width) / 2;
        }
        if ((position & POSITION_BOTTOM) != 0) {
            top = canvasHeight - height;
        } else if ((position & POSITION_CENTER_VERTICAL) != 0) {
            top = (canvasHeight - height) / 2;
        }
        left += offsetX;
        top += offsetY;

        layerMatrix.reset();
        layerMatrix.setTranslate(left, top);
        layerMatrix.postRotate(rotation);
        layerMatrix.postScale(scale, scale);

        float st[] = new float[16];
        layerMatrix.getValues(st);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, st, 0);

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
}
