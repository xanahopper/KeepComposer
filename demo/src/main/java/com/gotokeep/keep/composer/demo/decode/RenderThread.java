package com.gotokeep.keep.composer.demo.decode;

import android.graphics.SurfaceTexture;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Pair;
import android.view.Surface;

import com.gotokeep.keep.social.composer.gles.EglCore;
import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.gles.RenderTexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 11:41
 */
public class RenderThread implements Handler.Callback {

    public static final int MSG_RENDER_COMPLETE = 0;

    private static final String EXTERNAL_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "varying vec2 vTexCoords;\n" +
            "void main() { \n" +
            "    gl_FragColor = texture2D(uTexture, vTexCoords);\n" +
            "}\n";

    private static final int MSG_INIT_CONTEXT = 609;
    private static final int MSG_RENDER_DECODED_TEXTURE = 773;

    private HandlerThread internalThread;
    private Handler handler;
    private Handler eventHandler;

    private Surface outputSurface;
    private SurfaceTexture outputSurfaceTexture;
    private EglCore eglCore;
    private EGLSurface eglSurface;

    private ProgramObject programObject;
    protected static final float[] DEFAULT_VERTEX_DATA = {
            -1f, -1f, 0,
            1f, -1f, 0,
            -1f, 1f, 0,
            1f, 1f, 0};
    static final short[] DEFAULT_TEX_COORDS_DATA = {0, 0, 1, 0, 0, 1, 1, 1};

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer texCoordBuffer;

    public RenderThread(Handler eventHandler, SurfaceTexture outputSurfaceTexture) {
        this.eventHandler = eventHandler;
        this.outputSurfaceTexture = outputSurfaceTexture;
        internalThread = new HandlerThread("GLRenderThread");
        internalThread.start();
        handler = new Handler(internalThread.getLooper(), this);
        initContext();
    }

    private void initContext() {
        handler.sendEmptyMessage(MSG_INIT_CONTEXT);
    }

    public void renderDecodedTexture(DecodeThread decodeThread, FrameInfo info) {
        handler.obtainMessage(MSG_RENDER_DECODED_TEXTURE, new Pair<>(decodeThread, info)).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT_CONTEXT:
                initContextInternal();
                return true;
            case MSG_RENDER_DECODED_TEXTURE:
                Pair<DecodeThread, FrameInfo> pair = (Pair<DecodeThread, FrameInfo>) msg.obj;
                DecodeThread decodeThread = (DecodeThread) pair.first;
                FrameInfo info = pair.second;
                renderDecodeTextureInternal(decodeThread.getDecodeTexture(), decodeThread.getRenderTexture(), info);
        }
        return false;
    }

    private void initContextInternal() {
        eglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        outputSurface = new Surface(outputSurfaceTexture);
        eglSurface = eglCore.createWindowSurface(outputSurface);
        eglCore.makeCurrent(eglSurface);

        programObject = new ProgramObject(EXTERNAL_FRAGMENT_SHADER, ProgramObject.DEFAULT_UNIFORM_NAMES);
        vertexBuffer = ByteBuffer.allocateDirect(DEFAULT_VERTEX_DATA.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(DEFAULT_VERTEX_DATA).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(DEFAULT_TEX_COORDS_DATA.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        texCoordBuffer.put(DEFAULT_TEX_COORDS_DATA).position(0);
        programObject.use();

        GLES20.glBindAttribLocation(programObject.getProgramId(), 0, ProgramObject.ATTRIBUTE_POSITION);
        //checkGlError("glBindAttribLocation");
        GLES20.glBindAttribLocation(programObject.getProgramId(), 1, ProgramObject.ATTRIBUTE_TEX_COORDS);
        //checkGlError("glBindAttribLocation");
        activeAttribData();
    }

    private void renderDecodeTextureInternal(RenderTexture decodeTexture, RenderTexture renderTexture, FrameInfo info) {
        info.updateTexImage.startTime = SystemClock.elapsedRealtimeNanos();
        decodeTexture.updateTexImage();
        renderTexture.setRenderTarget(960, 540);
        programObject.use();
        decodeTexture.bind(0);

        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, decodeTexture.getTransitionMatrix(), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        decodeTexture.unbind(0);

        eglCore.swapBuffers(eglSurface);
        info.updateTexImage.endTime = SystemClock.elapsedRealtimeNanos();
        eventHandler.obtainMessage(MSG_RENDER_COMPLETE, info).sendToTarget();
    }


    private void drawTexture(RenderTexture decodeTexture) {

    }

    protected void activeAttribData() {
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        //checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(0);
        //checkGlError("glEnableVertexAttribArray");
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, texCoordBuffer);
        //checkGlError("glVertexAttribPointer");
        GLES20.glEnableVertexAttribArray(1);
        //checkGlError("glEnableVertexAttribArray");
    }
}
