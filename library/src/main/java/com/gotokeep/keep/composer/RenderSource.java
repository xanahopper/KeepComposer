package com.gotokeep.keep.composer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/12 15:36
 */
public abstract class RenderSource extends RenderNode {

    @Override
    public void setInputNode(int inputIndex, RenderNode inputNode) {
        // No input for source
    }
}
