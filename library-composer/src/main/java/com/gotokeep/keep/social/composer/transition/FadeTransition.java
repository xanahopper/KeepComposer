package com.gotokeep.keep.social.composer.transition;

import android.opengl.GLES20;

import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

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
    protected void onPreload() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

        startNode = getStartNode();
        endNode = getEndNode();

        long positionUs = TimeUtil.usToMs(presentationTimeUs);
        float alpha = (float) (positionUs - startTimeMs) / durationMs;
        alpha = MediaUtil.clamp(alpha, 0.0f, 1.0f);
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
