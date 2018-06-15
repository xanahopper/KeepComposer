package com.gotokeep.keep.social.composer.filter.effects;

import com.gotokeep.keep.social.composer.filter.MediaFilter;
import com.gotokeep.keep.social.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-11 17:51
 */
public abstract class MediaEffect extends MediaFilter {

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        updateEffectUniformParameters(programObject, presentationTimeUs);
    }

    protected abstract void updateEffectUniformParameters(ProgramObject programObject, long presentationTimeUs);
}
