package com.gotokeep.keep.composer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:40
 */
public final class RenderTexture implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = RenderTexture.class.getSimpleName();

    public static final int TEXTURE_NATIVE = GLES20.GL_TEXTURE_2D;
    public static final int TEXTURE_EXTERNAL = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    @IntDef({TEXTURE_NATIVE, TEXTURE_EXTERNAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextureTarget {
    }

    private int framebufferId = 0;
    private int textureTarget;
    private int textureId;
    private SurfaceTexture surfaceTexture;
    private boolean released = false;

    private final Object frameSyncObj = new Object();
    private boolean frameAvailable = false;

    public RenderTexture(@TextureTarget int textureTarget) {
        createTexture(textureTarget);
    }

    private void createTexture(int textureTarget) {
        int texId[] = new int[1];
        GLES20.glGenTextures(1, texId, 0);
        this.textureTarget = textureTarget;
        this.textureId = texId[0];

        GLES20.glBindTexture(textureTarget, textureId);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
    }

    public void release() {
        if (surfaceTexture == null || released) {
            return;
        }
        surfaceTexture.release();
        surfaceTexture = null;

        released = true;
    }

    public boolean isReleased() {
        return released;
    }

    public void notifyNoFrame() {
        synchronized (frameSyncObj) {
            frameAvailable = true;
            frameSyncObj.notifyAll();
        }
    }

    public void bind() {
        bind(-1);
    }

    public void bind(int activeId) {
        if (activeId >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeId);
        }
        GLES20.glBindTexture(textureTarget, textureId);
    }

    public void awaitFrameAvailable() {
        awaitFrameAvailable(10);
    }

    public void awaitFrameAvailable(int timeoutMs) {
        synchronized (frameSyncObj) {
            while (!frameAvailable) {
                try {
                    frameSyncObj.wait(timeoutMs);
                } catch (InterruptedException e) {
                    // shouldn't happen
                    Log.w(TAG, "awaitFrameAvailable with interrupted, it shouldn't happen", e);
                }
            }
            frameAvailable = false;
        }
        surfaceTexture.updateTexImage();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (frameSyncObj) {
            if (frameAvailable) {
                throw new RuntimeException("frameAvailable already set. frame dropped.");
            }
            frameAvailable = true;
            frameSyncObj.notifyAll();
        }
    }

    public boolean setRenderTarget() {
        if (framebufferId <= 0) {
            int ids[] = new int[1];
            GLES20.glGenFramebuffers(1, ids, 0);
            framebufferId = ids[0];
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, textureTarget, textureId, 0);
        return GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) == GLES20.GL_FRAMEBUFFER_COMPLETE;
    }

    public static void resetRenderTarget() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int getTextureTarget() {
        return textureTarget;
    }

    public int getTextureId() {
        return textureId;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }
}
