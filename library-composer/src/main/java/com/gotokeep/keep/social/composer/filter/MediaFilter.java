package com.gotokeep.keep.social.composer.filter;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.gles.ProgramObject;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 17:02
 */
public abstract class MediaFilter extends RenderNode {

    public abstract void setFilterParameters(Map<String, Object> params);


    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(getVertexShader(), getFragmentShader(), getUniformNames());
    }

    @Override
    protected void onPrepare() {
        if (programObject != null) {
            programObject.use();
        }
        prepareProgramInternal(programObject);
    }

    @Override
    protected void onRelease() {
        if (programObject != null) {
            programObject.release();
            programObject = null;
        }
    }

    protected abstract String getVertexShader();

    protected abstract String getFragmentShader();

    protected abstract String[] getUniformNames();

    protected abstract void prepareProgramInternal(ProgramObject programObject);
}
