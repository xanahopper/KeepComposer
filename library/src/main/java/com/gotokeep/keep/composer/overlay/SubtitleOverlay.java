package com.gotokeep.keep.composer.overlay;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 16:03
 */
public class SubtitleOverlay extends MediaOverlay {
    public SubtitleOverlay(RenderNode mainInputNode) {
        super(mainInputNode);
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
    protected long doRender(ProgramObject programObject, long positionUs) {
        return 0;
    }

    @Override
    protected void bindRenderTextures(boolean[] shouldRender) {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }
}
