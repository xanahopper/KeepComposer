package com.gotokeep.keep.social.composer.transition;

import android.opengl.GLES20;
import android.support.v4.math.MathUtils;

import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/6/7 18:36
 */
public class BlackTransition extends MediaTransition {
    private static final String UNIFORM_START_ALPHA = "uStartAlpha";
    private static final String UNIFORM_END_ALPHA = "uEndAlpha";
    private static final String BLACK_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uStartTexture; \n" +
            "uniform sampler2D uEndTexture; \n" +
            "uniform float uStartAlpha;\n" +
            "uniform float uEndAlpha;\n" +
            "varying vec2 vStartTexCoords; \n" +
            "varying vec2 vEndTexCoords; \n" +
            "void main()                                  \n" +
            "{                                            \n" +
            "  vec4 startColor = texture2D(uStartTexture, vStartTexCoords);\n" +
            "  vec4 endColor = texture2D(uEndTexture, vEndTexCoords);\n" +
            "  gl_FragColor = startColor * uStartAlpha + endColor * uEndAlpha;\n" +
            "}                                            \n";

    private static final String UNIFORM_NAMES[] = {
            UNIFORM_START_ALPHA,
            UNIFORM_END_ALPHA,
            UNIFORM_START_TEXTURE,
            UNIFORM_END_TEXTURE,
            UNIFORM_START_TRANSFORM,
            uNIFORM_END_TRANSFORM
    };

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(DEFAULT_VERTEX_SHADER, BLACK_SHADER, UNIFORM_NAMES);
    }

    @Override
    protected void onPreload() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

        startNode = getStartNode();
        endNode = getEndNode();

        long positionMs = TimeUtil.usToMs(presentationTimeUs);
        long position = (long) MathUtils.clamp(positionMs - startTimeMs, 0, durationMs);
        float startAlpha = 1f;
        float endAlpha = 0f;
        if (position < durationMs / 2) {
            startAlpha = 1f - ((float) position / (float) durationMs * 2f);
        } else {
            startAlpha = 0f;
            endAlpha = (position - durationMs * 0.5f) / (durationMs * 0.5f);
        }
        GLES20.glUniform1f(programObject.getUniformLocation(UNIFORM_START_ALPHA), startAlpha);
        GLES20.glUniform1f(programObject.getUniformLocation(UNIFORM_END_ALPHA), endAlpha);

        if (startNode != null) {
            GLES20.glUniformMatrix4fv(programObject.getUniformLocation(UNIFORM_START_TRANSFORM), 1, false,
                    startNode.getTexCoordMatrix(), 0);
        }
        if (endNode != null) {
            GLES20.glUniformMatrix4fv(programObject.getUniformLocation(uNIFORM_END_TRANSFORM), 1, false,
                    endNode.getTexCoordMatrix(), 0);
        }
    }
}
