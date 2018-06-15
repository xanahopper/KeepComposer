package com.gotokeep.keep.social.composer.filter.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-11 17:59
 */
public class TriColorEffect extends MediaEffect {
    public static final String UNIFORM_MOVE_DIRECTOR = "uMoveDirector";
    public static final String UNIFORM_MOVE_SCALE_MATRIX = "uMoveScaleMatrix";
    private static final String TRICOLOR_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform vec2 uMoveDirector;\n" +
            "uniform mat4 uMoveScaleMatrix;\n" +
            "varying vec2 vTexCoords;\n" +
            "void main() { \n" +
            "    float r = texture2D(uTexture, uMoveScaleMatrix * (vTexCoords + uMoveDirector)).r;\n" +
            "    float g = texture2D(uTexture, uMoveScaleMatrix * (vTexCoords + 0.5f * uMoveDirector)).g;\n" +
            "    float b = texture2D(uTexture, vTexCoords).b;\n" +
            "    gl_FragColor = vec4(r, g, b, 1f);\n" +
            "}\n";

    public static final String DEFAULT_UNIFORM_NAMES[] = {
            ProgramObject.UNIFORM_TRANSFORM_MATRIX,
            ProgramObject.UNIFORM_TEXCOORD_MATRIX,
            ProgramObject.UNIFORM_TEXTURE,
            UNIFORM_MOVE_DIRECTOR,
            UNIFORM_MOVE_SCALE_MATRIX
    };

    private static final int MAX_LENGTH = 50;
    private static final long REPEAT_TIME = 250;
    private static final long EFFECT_TIME = 100;

    private float moveDirector[] = new float[2];
    private float moveScaleMatrix[] = new float[16];

    @Override
    protected void updateEffectUniformParameters(ProgramObject programObject, long presentationTimeUs) {
        long offsetTimeMs = TimeUtil.usToMs(presentationTimeUs) - startTimeMs;
        long effectTime = offsetTimeMs % REPEAT_TIME;
        int moveLength = 0;
        float scale = 1f;
        if (effectTime < EFFECT_TIME) {
            moveLength = (int) (((float) effectTime / (float) EFFECT_TIME) * MAX_LENGTH);
            scale += (float) effectTime / EFFECT_TIME * 0.1f;
        }
        moveDirector[0] = moveLength / originHeight;
        moveDirector[1] = moveLength / originWidth * 0.5f;

        Matrix.setIdentityM(moveScaleMatrix, 0);
        Matrix.scaleM(moveScaleMatrix, 0, scale, scale, 1f);
        GLES20.glUniform2fv(programObject.getUniformLocation(UNIFORM_MOVE_DIRECTOR), 1, moveDirector, 0);
    }

    @Override
    public void setFilterParameters(Map<String, Object> params) {

    }

    @Override
    protected String getVertexShader() {
        return ProgramObject.DEFAULT_VERTEX_SHADER;
    }

    @Override
    protected String getFragmentShader() {
        return TRICOLOR_FRAGMENT_SHADER;
    }

    @Override
    protected String[] getUniformNames() {
        return DEFAULT_UNIFORM_NAMES;
    }

    @Override
    protected void prepareProgramInternal(ProgramObject programObject) {

    }

    @Override
    protected void onPreload() {

    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {

        return positionUs;
    }

    @Override
    protected void bindRenderTextures() {
        if (inputNodes.size() > 0) {
            inputNodes.get(0).getOutputTexture().bind(0);
        }
    }

    @Override
    protected void unbindRenderTextures() {
        if (inputNodes.size() > 0) {
            inputNodes.get(0).getOutputTexture().unbind(0);
        }
    }
}
