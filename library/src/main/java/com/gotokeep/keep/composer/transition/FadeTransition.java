package com.gotokeep.keep.composer.transition;

import android.opengl.GLES20;

import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 19:43
 */
public class FadeTransition extends MediaTransition {
    private static final String FADE_SHADER = "";
    private static final String UNIFORM_NAMES[] = {
            "alpha",
            "startTexture",
            "endTexture",
            "startTransform",
            "endTransform"
    };
    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(FADE_SHADER, UNIFORM_NAMES);
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        float alpha = (float) (TimeUtil.usToMs(presentationTimeUs) - startTimeMs) / durationMs;

        GLES20.glUniform1f(programObject.getUniformLocation("alpha"), alpha);
    }

    @Override
    protected boolean shouldRenderStartNode(long presentationTimeUs) {
        return presentationTimeUs >= TimeUtil.msToUs(startTimeMs) &&
                presentationTimeUs <= TimeUtil.msToUs(endTimeMs);
    }

    @Override
    protected boolean shouldRenderEndNode(long presentationTimeUs) {
        return presentationTimeUs >= TimeUtil.msToUs(startTimeMs) &&
                presentationTimeUs <= TimeUtil.msToUs(endTimeMs);
    }
}
