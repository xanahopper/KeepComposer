package com.gotokeep.keep.composer.source;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderRequest;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 13:30
 */
public abstract class MediaSource extends RenderNode {
    public static final int TYPE_DYNAMIC = 0;
    public static final int TYPE_STATIC = 1;

    public static final int DURATION_INFINITE = -1;

    private int mediaType;
    int width;
    int height;
    int rotation;
    long durationMs;
    float playSpeed = 1f;
    boolean ended = false;
    protected final Object requestSyncObj = new Object();
    protected AtomicBoolean requestUpdated = new AtomicBoolean(true);
    protected RenderRequest renderRequest;
    protected Semaphore decodeSem = new Semaphore(1);

    protected MediaSource(int mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public void addInputNode(RenderNode inputNode) {

    }

    public void updateRenderRequest(RenderRequest renderRequest) {
        synchronized (requestSyncObj) {
            this.renderRequest = renderRequest;
            requestUpdated.set(false);
            decodeSem.release();
        }
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
