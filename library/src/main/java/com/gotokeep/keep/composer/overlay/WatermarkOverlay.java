package com.gotokeep.keep.composer.overlay;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 15:54
 */
public class WatermarkOverlay extends MediaOverlay {
    public WatermarkOverlay(RenderNode mainInputNode) {
        super(mainInputNode);
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
    }

    @Override
    protected void onRelease() {

    }

    @Override
    protected void renderOverlay(ProgramObject overlayProgramObject) {

    }
}
