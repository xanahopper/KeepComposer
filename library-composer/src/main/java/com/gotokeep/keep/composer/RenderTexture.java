package com.gotokeep.keep.composer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.IntDef;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.opengles.GL11Ext;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:40
 */
public final class RenderTexture implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = RenderTexture.class.getSimpleName();

    public static final int TEXTURE_NATIVE = GLES20.GL_TEXTURE_2D;
    public static final int TEXTURE_EXTERNAL = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    public boolean isFrameAvailable() {
        return frameAvailable;
    }

    public void updateTexImage() {
        surfaceTexture.updateTexImage();
        synchronized (frameSyncObj) {
            frameAvailable = false;
        }
    }

    @IntDef({TEXTURE_NATIVE, TEXTURE_EXTERNAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TextureTarget {

    }
    private int framebufferId = 0;

    private int textureTarget = 0;
    private int textureId = 0;
    private SurfaceTexture surfaceTexture;
    private boolean released = false;
    private final Object frameSyncObj = new Object();
    private float transitionMatrix[] = new float[16];

    private boolean frameAvailable = false;
    private final String name;
    public RenderTexture(String name) {
        this.textureTarget = TEXTURE_EXTERNAL;
        this.surfaceTexture = null;
        this.name = name;
    }

    public RenderTexture(@TextureTarget int textureTarget, String name) {
        this.name = name;
        createTexture(textureTarget);
    }

    public void clear() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    private void createTexture(int textureTarget) {
        int texId[] = new int[1];
        GLES20.glGenTextures(1, texId, 0);
        checkGlError("genTexture");
        this.textureTarget = textureTarget;
        this.textureId = texId[0];

        GLES20.glBindTexture(textureTarget, textureId);
        checkGlError("bindTexture");
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(textureTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(textureTarget, 0);
        checkGlError("bindTexture to 0");
        if (textureTarget == TEXTURE_EXTERNAL) {
            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(this);
        }
    }

    public void release() {
        if (surfaceTexture == null || released || textureId == 0) {
            return;
        }
        surfaceTexture.release();
        surfaceTexture = null;

        if (framebufferId > 0) {
            int ids[] = {framebufferId};
            GLES20.glDeleteFramebuffers(1, ids, 0);
            framebufferId = 0;
        }

        if (textureId > 0) {
            int ids[] = {textureId};
            GLES20.glDeleteTextures(1, ids, 0);
            textureId = 0;
        }

        released = true;
    }

    public boolean isReleased() {
        return released;
    }

    public float[] getTransitionMatrix() {
        if (surfaceTexture != null) {
            surfaceTexture.getTransformMatrix(transitionMatrix);
        } else {
            Matrix.setIdentityM(transitionMatrix, 0);
        }
        return transitionMatrix;
    }

    public void notifyNoFrame() {
        synchronized (frameSyncObj) {
            frameAvailable = false;
            frameSyncObj.notifyAll();
        }
    }

    public void bind() {
        bind(-1);
    }

    public void bind(int activeId) {

        if (textureId == 0) {
            return;
        }
        if (activeId >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeId);
            checkGlError("active" + activeId);
        }
        GLES20.glBindTexture(textureTarget, textureId);
        checkGlError("bindTexture(" + getTargetName(textureTarget) + ", " + textureId + ")");
    }

    public void unbind(int activeId) {
        if (activeId >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeId);
            checkGlError("active" + activeId);
        }
        GLES20.glBindTexture(textureTarget, 0);
        checkGlError("unbindTexture(" + textureTarget + ")");
    }

    public boolean awaitFrameAvailable() {
        return awaitFrameAvailable(10);
    }

    public boolean awaitFrameAvailable(int timeoutMs) {
        if (surfaceTexture == null) {
            return true;
        }
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
        return true;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (frameSyncObj) {
//            if (frameAvailable) {
//                Log.w(TAG, "onFrameAvailable: ", new RuntimeException("frameAvailable already set. frame dropped."));;
//            }
            Log.d(TAG, String.format("[%s] onFrameAvailable", name));
            frameAvailable = true;
            frameSyncObj.notifyAll();
        }
    }

    public boolean setRenderTarget(int canvasWidth, int canvasHeight) {
        if (textureId == 0) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            checkGlError("bindFramebuffer");
            return true;
        }
        if (framebufferId <= 0) {
            int ids[] = new int[1];
            GLES20.glGenFramebuffers(1, ids, 0);
            checkGlError("genFramebuffers");
            framebufferId = ids[0];
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
            checkGlError("bindFramebuffer");
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, textureTarget, textureId, 0);
            checkGlError("glFramebufferTexture2D");
            GLES20.glBindTexture(textureTarget, textureId);
            checkGlError("glBindTexture");
            GLES20.glTexImage2D(textureTarget, 0, GLES20.GL_RGBA, canvasWidth, canvasHeight, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            checkGlError("glTexImage2D");
            GLES20.glBindTexture(textureTarget, 0);
            checkGlError("glBindTexture");
        } else {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
            checkGlError("glFramebufferTexture2D");
        }
        GLES20.glViewport(0, 0, canvasWidth, canvasHeight);
        checkGlError("glFramebufferTexture2D");
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

    public Bitmap saveFrame(int width, int height) {
        ByteBuffer buf = ByteBuffer.allocate(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        buf.rewind();

        int pixelCount = width * height;
        int[] colors = new int[pixelCount];
        buf.asIntBuffer().get(colors);
        for (int i = 0; i < pixelCount; i++) {
            int c = colors[i];
            colors[i] = (c & 0xff00ff00) | ((c & 0x00ff0000) >> 16) | ((c & 0x000000ff) << 16);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("/sdcard/frame.png");
            Bitmap bmp = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }

    private String getTargetName(int textureTarget) {
        if (textureTarget == GLES20.GL_TEXTURE_2D) {
            return "TEXTURE_2D";
        } else if (textureTarget == GLES11Ext.GL_TEXTURE_EXTERNAL_OES) {
            return "TEXTURE_EXTERNAL_OES";
        } else {
            return "unknown";
        }
    }
}
