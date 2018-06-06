package com.gotokeep.keep.social.composer.source;

import android.util.Log;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.ScaleType;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.ScaleUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 13:30
 */
public abstract class MediaSource extends RenderNode {
    public static final int TYPE_VIDEO = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_GENERATE = 2;

    public static final int DURATION_INFINITE = -1;

    private int mediaType;
    int width;
    int height;
    int rotation;
    long durationMs;
    float playSpeed = 1f;
    boolean ended = false;
    ScaleType scaleType = ScaleType.FIT_CENTER;

    protected MediaSource(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public void addInputNode(RenderNode inputNode) {

    }

    public boolean isEnded() {
        return ended;
    }

    public float getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(float playSpeed) {
        this.playSpeed = playSpeed;
    }

    public int getMediaType() {
        return mediaType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRotation() {
        return rotation;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
        updateScaleMatrix();
    }

    @Override
    public void setViewport(int width, int height) {
        super.setViewport(width, height);
        updateScaleMatrix();
    }

    @Override
    public void setOriginSize(int width, int height) {
        super.setOriginSize(width, height);
        updateScaleMatrix();
    }

    protected void updateScaleMatrix() {
        ScaleUtil.getScaleMatrix(scaleType, scaleMatrix, originWidth, originHeight, width, height);
        Log.d("MediaSourceMatrix", "canvasSize: " + canvasWidth + ", " + canvasHeight);
        Log.d("MediaSourceMatrix", "originSize: " + originWidth + ", " + originHeight);
        Log.d("MediaSourceMatrix", "size: " + width + ", " + height);
        Log.d("MediaSourceMatrix", "updateScaleMatrix: \n" + MediaUtil.matrixToString(scaleMatrix));
    }
}
