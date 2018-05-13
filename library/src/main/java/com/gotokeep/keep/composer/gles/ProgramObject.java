package com.gotokeep.keep.composer.gles;

import android.opengl.GLES20;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 18:43
 */
public final class ProgramObject {
    private int programId;
    private String vertexShader;
    private String fragmentShader;

    private static final String DEFAULT_VERTEX_SHADER =
            "";

    private Map<String, Integer> uniforms;

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
        for (int i = 0; i < uniformNames.length; i++) {
            int loc = GLES20.glGetUniformLocation(programId, uniformNames[i]);
            uniforms.put(uniformNames[i], loc);
        }
    }

    public int getUniformLocation(String name) {
        return uniforms.get(name);
    }
}
