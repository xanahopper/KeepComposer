package com.gotokeep.keep.composer;

import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.gles.EglCore;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:07
 */
public final class ComposerEngine {
    private static final String TAG = ComposerEngine.class.getSimpleName();
    private EglCore eglCore;
    private EGLSurface eglSurface;

    private int width;
    private int height;
    private int framebufferId;

    public ComposerEngine() {

    }

    public void setup(SurfaceTexture outputSurfaceTexture) {
        setup(new Surface(outputSurfaceTexture));
    }

    public void setup(Surface outputSurface) {
        Log.d("Composer", "setup@" + Thread.currentThread().getName());
        release();
        eglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        eglSurface = eglCore.createWindowSurface(outputSurface);
        eglCore.makeCurrent(eglSurface);
    }

    public void release() {
        if (eglCore != null) {
            eglCore.makeNothingCurrent();
            eglCore.releaseSurface(eglSurface);
            eglCore.release();
            eglCore = null;
            eglSurface = null;
        }
    }

    public void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);
        checkGlError("glViewport");
    }

    public void swapBuffers() {
        eglCore.swapBuffers(eglSurface);
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }
}
