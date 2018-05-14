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
    private static final String UNIFORM_ALPHA = "uAlpha";
    private static final String FADE_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uStartTexture; \n" +
            "uniform sampler2D uEndTexture; \n" +
            "uniform float uAlpha;\n" +
            "varying vec2 vStartTexCoords; \n" +
            "varying vec2 vEndTexCoords; \n" +
            "void main()                                  \n" +
            "{                                            \n" +
            "  vec4 startColor = texture2D(uStartTexture, vStartTexCoords);\n" +
            "  vec4 endColor = texture2D(uEndTexture, vEndTexCoords);\n" +
            "  gl_FragColor = mix(startColor, endColor, uAlpha);\n" +
            "}                                            \n";

    private static final String UNIFORM_NAMES[] = {
            UNIFORM_ALPHA,
            UNIFORM_START_TEXTURE,
            UNIFORM_END_TEXTURE,
            UNIFORM_START_TRANSFORM,
            uNIFORM_END_TRANSFORM
    };

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(DEFAULT_VERTEX_SHADER, FADE_SHADER, UNIFORM_NAMES);
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        float alpha = (float) (TimeUtil.usToMs(presentationTimeUs) - startTimeMs) / durationMs;
        GLES20.glUniform1f(programObject.getUniformLocation(UNIFORM_ALPHA), alpha);


        float transform[] = new float[16];
        startNode.getTransformMatrix(transform);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(UNIFORM_START_TRANSFORM), 1, false,
                transform, 0);
        endNode.getTransformMatrix(transform);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(uNIFORM_END_TRANSFORM), 1, false,
                transform, 0);
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
