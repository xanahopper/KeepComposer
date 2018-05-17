package com.gotokeep.keep.composer.target;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTarget;
import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 09:47
 */
public class PreviewRenderTarget extends RenderTarget implements SurfaceTexture.OnFrameAvailableListener {
    private ProgramObject programObject;
    private Surface inputSurface;

    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs, RenderNode renderNode) {
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, renderNode.getTransformMatrix(), 0);
    }

    @Override
    public Surface getInputSurface() {
        return inputSurface;
    }

    @Override
    public void updateFrame(RenderNode renderNode, long presentationTimeUs) {
        Log.d("Composer", "PreviewRenderTarget#updateFrame: " + presentationTimeUs);
        programObject.use();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        renderNode.getOutputTexture().bind(0);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, renderNode.getTransformMatrix(), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void prepare() {
        programObject = new ProgramObject();
    }

    @Override
    public void complete() {

    }

    @Override
    public void release() {
        if (programObject != null) {
            programObject.release();
            programObject = null;
        }
        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (inputSurface != null) {
            inputSurface.release();
        }
        inputSurface = new Surface(surfaceTexture);
    }
}
