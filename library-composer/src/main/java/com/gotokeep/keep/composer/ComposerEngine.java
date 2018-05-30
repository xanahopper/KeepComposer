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

    public ComposerEngine() {

    }

//    public void setup(SurfaceTexture outputSurfaceTexture) {
//        setup(outputSurfaceTexture != null ? new Surface(outputSurfaceTexture) : null);
//    }

    public void setup(Surface outputSurface) {
        Log.d("Composer", "setup@" + Thread.currentThread().getName());
        if (eglCore == null) {
            eglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        }
        EGLSurface originSurface = eglSurface;
        if (outputSurface != null) {
            eglSurface = eglCore.createWindowSurface(outputSurface);
        } else {
            eglSurface = eglCore.createOffscreenSurface(0, 0);
        }
        eglCore.makeCurrent(eglSurface);

        if (originSurface != null && originSurface != eglSurface) {
            eglCore.releaseSurface(originSurface);
        }
    }

    public void setOutputSurface(SurfaceTexture outputSurfaceTexture) {
        setOutputSurface(new Surface(outputSurfaceTexture));
    }

    private void setOutputSurface(Surface surface) {
        if (surface != null) {
            if (eglCore == null) {
                setup(surface);
            } else if (eglSurface == null) {
                eglSurface = eglCore.createWindowSurface(surface);
                eglCore.makeCurrent(eglSurface);
            }
        }
    }

    public void release() {
        if (eglCore != null) {
            eglCore.makeNothingCurrent();
            if (eglSurface != null) {
                eglCore.releaseSurface(eglSurface);
            }
            eglCore.release();
            eglCore = null;
            eglSurface = null;
        }
    }

    public void setViewport(int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);
      //checkGlError("glViewport");
    }

    public void swapBuffers() {
        eglCore.swapBuffers(eglSurface);
    }

    public void checkGlError(String op) {
//        int error;
//        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
//            Log.e(TAG, op + ": glError " + error);
//        }
    }

    public void setPresentationTime(long nsecs) {
        if (eglCore != null) {
            eglCore.setPresentationTime(eglSurface, nsecs);
        }
    }
}
