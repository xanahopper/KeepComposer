package com.gotokeep.keep.composer.target;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 09:47
 */
public class PreviewRenderTarget extends RenderNode {

    public PreviewRenderTarget() {
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture();
    }

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject();
    }

    @Override
    protected void onPrepare() {
        if (inputNodes.size() < 1) {
            throw new RuntimeException("Not enough input.");
        }
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
    }

    @Override
    protected void onRelease() {
    }

    @Override
    protected void onRender(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    protected void bindRenderTextures() {
        RenderNode node = inputNodes.valueAt(0);
        node.getOutputTexture().bind(0);
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        RenderNode node = inputNodes.valueAt(0);
        float transform[] = new float[16];
        node.getTransformMatrix(transform);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, transform, 0);
    }
}
