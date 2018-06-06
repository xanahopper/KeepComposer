package com.gotokeep.keep.social.composer.filter.basic;

import android.opengl.GLES20;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.filter.ExternalFilter;
import com.gotokeep.keep.social.composer.filter.MediaFilter;
import com.gotokeep.keep.social.composer.gles.ProgramObject;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 10:01
 */
public class KeepExternalFilter extends MediaFilter {
    protected ExternalFilter externalFilter;

    public KeepExternalFilter(ExternalFilter externalFilter) {
        this.externalFilter = externalFilter;
    }

    @Override
    public void setFilterParameters(Map<String, Object> params) {

    }

    @Override
    public void setViewport(int width, int height) {
        super.setViewport(width, height);
        externalFilter.onDisplaySizeChanged(width, height);
    }

    @Override
    protected ProgramObject createProgramObject() {
        return null;
    }

    @Override
    protected void onPreload() {

    }

    @Override
    protected String getVertexShader() {
        return null;
    }

    @Override
    protected String getFragmentShader() {
        return null;
    }

    @Override
    protected String[] getUniformNames() {
        return new String[0];
    }

    @Override
    protected void prepareProgramInternal(ProgramObject programObject) {
        externalFilter.init();
        if (inputNodes.size() > 0) {
            RenderNode inputNode = inputNodes.get(0);
            externalFilter.onInputSizeChanged(inputNode.getCanvasWidth(), inputNode.getCanvasHeight());
        }
    }

    @Override
    public void setOriginSize(int width, int height) {
        externalFilter.onInputSizeChanged(width, height);
    }

    @Override
    public void setCanvasSize(int width, int height) {
        externalFilter.onInputSizeChanged(width, height);
    }

    @Override
    protected void onRelease() {
        externalFilter.destroy();
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        if (externalFilter != null) {
            externalFilter.onDrawFrame(inputNodes.get(0).getOutputTexture().getTextureId());
        }
        return inputNodes.get(0).getRenderTimeUs();
    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void unbindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }
}
