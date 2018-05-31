package com.gotokeep.keep.social.composer.source;

import com.gotokeep.keep.social.composer.RenderNode;

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
}