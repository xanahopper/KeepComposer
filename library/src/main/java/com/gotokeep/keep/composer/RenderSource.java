package com.gotokeep.keep.composer;

import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:36
 */
public abstract class RenderSource extends RenderNode {
    @Override
    public void setInputNode(int inputIndex, RenderNode inputNode) {
        // nop
    }

    @Override
    public RenderNode getMainInputNode(long presentationTimeUs) {
        return TimeUtil.inRange(TimeUtil.usToMs(presentationTimeUs), startTimeMs, endTimeMs) ? this : null;
    }
}
