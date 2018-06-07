package com.gotokeep.keep.social.composer.transition;

import android.opengl.GLES20;
import android.support.v4.math.MathUtils;

import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/6/7 18:36
 */
public class WhiteTransition extends MediaTransition {
    private static final String UNIFORM_ALPHA = "uAlpha";
    private static final String UNIFORM_CHOOSE = "uChoose";
    private static final String BLACK_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uStartTexture; \n" +
            "uniform sampler2D uEndTexture; \n" +
            "uniform float uAlpha;\n" +
            "uniform float uChoose;\n" +
            "varying vec2 vStartTexCoords; \n" +
            "varying vec2 vEndTexCoords; \n" +
            "void main()                                  \n" +
            "{                                            \n" +
            "  vec4 startColor = texture2D(uStartTexture, vStartTexCoords);\n" +
            "  vec4 endColor = texture2D(uEndTexture, vEndTexCoords);\n" +
            "  vec4 white = vec4(1.0f, 1.0f, 1.0f, 1.0f);\n" +
            "  gl_FragColor = mix(mix(startColor, endColor, uChoose), " +
            "                     white, uAlpha);\n" +
            "}                                            \n";

    private static final String UNIFORM_NAMES[] = {
            UNIFORM_ALPHA,
            UNIFORM_CHOOSE,
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
        float choose = position < durationMs / 2 ? 0f : 1f;
        float alpha = 1f - Math.abs(position - durationMs *0.5f) / (durationMs * 0.5f);
        GLES20.glUniform1f(programObject.getUniformLocation(UNIFORM_CHOOSE), choose);
        GLES20.glUniform1f(programObject.getUniformLocation(UNIFORM_ALPHA), alpha);

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
