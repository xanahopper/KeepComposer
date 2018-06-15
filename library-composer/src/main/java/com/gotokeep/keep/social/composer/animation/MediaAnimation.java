package com.gotokeep.keep.social.composer.animation;

import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.gles.ProgramObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-14 10:38
 */
public class MediaAnimation extends RenderNode {
    public static final String UNIFORM_ANIMATION_MATRIX = "uAnimationMatrix";
    public static final String UNIFORM_ANIMATION_ALPHA = "uAnimationAlpha";
    public static final String ANIMATION_VERTEX_SHADER = "" +
            "attribute vec4 aPosition;    \n" +
            "attribute vec2 aTexCoords; \n" +
            "varying vec2 vTexCoords; \n" +
            "uniform mat4 uTexCoords;\n" +
            "uniform mat4 uTransformMatrix;\n" +
            "uniform mat4 uAnimationMatrix;\n" +
            "void main()                  \n" +
            "{                            \n" +
            "    gl_Position = uAnimationMatrix * uTransformMatrix * aPosition;  \n" +
            "    vTexCoords = (uTexCoords * vec4(aTexCoords, 0.0, 1.0)).st; \n" +
            "}                            \n";

    public static final String ANIMATION_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uTexture;\n" +
            "uniform float uAnimationAlpha;\n" +
            "varying vec2 vTexCoords;\n" +
            "void main() { \n" +
            "    vec4 color = texture2D(uTexture, vTexCoords);\n" +
            "    gl_FragColor = vec4(color.rgb, color.a * uAnimationAlpha);\n" +
            "}\n";

    public static final String ANIMATION_UNIFORM_NAMES[] = {
            UNIFORM_ANIMATION_MATRIX,
            UNIFORM_ANIMATION_ALPHA,
            ProgramObject.UNIFORM_TRANSFORM_MATRIX,
            ProgramObject.UNIFORM_TEXCOORD_MATRIX,
            ProgramObject.UNIFORM_TEXTURE
    };

    private List<AnimationKey> animationKeys = new ArrayList<>();

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(ANIMATION_VERTEX_SHADER, ANIMATION_FRAGMENT_SHADER, ANIMATION_UNIFORM_NAMES);
    }

    @Override
    protected void onPreload() {

    }

    @Override
    protected void onPrepare() {

    }

    @Override
    protected void onRelease() {

    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        return 0;
    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void unbindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }
}
