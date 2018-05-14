package com.gotokeep.keep.composer.overlay;

import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.source.MediaSource;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 16:53
 */
public class MediaOverlay extends MediaSource {
    protected MediaOverlay() {
        super(TYPE_OVERLAY);
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return null;
    }

    @Override
    protected ProgramObject createProgramObject() {
        return null;
    }

    @Override
    protected void onPrepare() {

    }

    @Override
    protected void onRelease() {

    }

    @Override
    protected void onRender(ProgramObject programObject, long presentationTimeUs) {

    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }
}
