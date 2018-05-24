package com.gotokeep.keep.composer.filter.basic;

import com.gotokeep.keep.composer.filter.ExternalFilter;
import com.gotokeep.keep.composer.filter.MediaFilter;
import com.gotokeep.keep.composer.gles.ProgramObject;

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
    protected ProgramObject createProgramObject() {
        return null;
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
