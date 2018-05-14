package com.gotokeep.keep.composer.gles;

import android.opengl.GLES20;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 18:43
 */
public final class ProgramObject {
    public static final String ATTRIBUTE_POSITION = "aPosition";
    public static final String ATTRIBUTE_TEX_COORDS = "aTexCoords";
    public static final String UNIFORM_TRANSFORM_MATRIX = "uTransformMatrix";
    public static final String UNIFORM_TEXTURE = "uTexture";
    public static final String DEFAULT_UNIFORM_NAMES[] = {
            UNIFORM_TRANSFORM_MATRIX,
            UNIFORM_TEXTURE
    };

    private int programId = -1;
    private String vertexShader;
    private String fragmentShader;

    private static final String DEFAULT_VERTEX_SHADER = "" +
            "attribute vec4 aPosition;    \n" +
            "attribute vec2 aTexCoords; \n" +
            "varying vec2 vTexCoords; \n" +
            "uniform mat4 uTransformMatrix;\n" +
            "void main()                  \n" +
            "{                            \n" +
            "    gl_Position = aPosition;  \n" +
            "    vTexCoords = (uTransformMatrix * vec4(aTexCoords, 0, 0)).xy; \n" +
            "}                            \n";

    private static final String DEFAULT_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform sampler2D uTexture;\n" +
            "varying vec2 vTexCoords;\n" +
            "void main() { \n" +
            "    gl_FragColor = texture2D(uTexture, vTexCoords);\n" +
            "}\n";

    private Map<String, Integer> uniforms;

    public ProgramObject() {
        this(DEFAULT_FRAGMENT_SHADER, DEFAULT_UNIFORM_NAMES);
    }

    public ProgramObject(String fragmentShader, String uniformNames[]) {
        this(DEFAULT_VERTEX_SHADER, fragmentShader, uniformNames);
    }

    public ProgramObject(String vertexShader, String fragmentShader, String uniformNames[]) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        programId = createProgram(vertexShader, fragmentShader);
        initUniformLocations(uniformNames);
    }

    public void use() {
        GLES20.glUseProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }

    private int createProgram(String vertexShader, String fragmentShader) {
        return OpenGlUtils.loadProgram(vertexShader, fragmentShader);
    }

    private void initUniformLocations(String[] uniformNames) {
        uniforms = new TreeMap<>();
        if (uniformNames != null) {
            for (int i = 0; i < uniformNames.length; i++) {
                int loc = GLES20.glGetUniformLocation(programId, uniformNames[i]);
                uniforms.put(uniformNames[i], loc);
            }
        }
    }

    public int getUniformLocation(String name) {
        return uniforms.containsKey(name) ? uniforms.get(name) : -1;
    }

    public void release() {
        GLES20.glDeleteProgram(programId);
        programId = -1;
    }
}
