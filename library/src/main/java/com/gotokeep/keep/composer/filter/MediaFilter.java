package com.gotokeep.keep.composer.filter;

import android.opengl.GLES20;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 17:02
 */
public abstract class MediaFilter extends RenderNode {
    @Override
    public void render(long presentationTimeUs) {
        programObject.use();
        updateProgramUniform(programObject);
        inputNodes.valueAt(0).getOutputTexture().bind(0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(getVertexShader(), getFragmentShader(), getUniformNames());
    }

    @Override
    protected void onPrepare() {
        programObject.use();
        prepareProgramInternal(programObject);
    }

    @Override
    protected void onRelease() {

    }

    protected abstract String getVertexShader();

    protected abstract String getFragmentShader();

    protected abstract String[] getUniformNames();

    protected abstract void prepareProgramInternal(ProgramObject programObject);

    protected abstract void updateProgramUniform(ProgramObject programObject);
}
