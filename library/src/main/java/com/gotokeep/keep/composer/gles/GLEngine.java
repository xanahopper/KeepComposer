package com.gotokeep.keep.composer.gles;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 14:58
 */
public final class GLEngine {
    private int width;
    private int height;
    private Surface outputSurface;

    public void setViewport(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void setupGL(SurfaceTexture outputSurfaceTexture) {
        setupGL( new Surface(outputSurfaceTexture));
    }

    private void setupGL(Surface outputSurface) {
        this.outputSurface = outputSurface;

    }

    public void destroyGL() {

    }
}
