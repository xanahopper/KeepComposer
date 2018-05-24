package com.gotokeep.keep.composer.target;

import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.ComposerEngine;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTarget;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.source.AudioSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 09:47
 */
public class PreviewRenderTarget extends RenderTarget implements SurfaceTexture.OnFrameAvailableListener {
    private ProgramObject programObject;
    private Surface inputSurface;
    private int sampleRate;
    private int buffSize;
    private AudioTrack audioTrack;
    protected static final float[] DEFAULT_VERTEX_DATA = {
            -1f, -1f, 0,
            1f, -1f, 0,
            -1f, 1f, 0,
            1f, 1f, 0};
    static final short[] DEFAULT_TEX_COORDS_DATA = {0, 0, 1, 0, 0, 1, 1, 1};

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer texCoordBuffer;

    @Override
    public Surface getInputSurface() {
        return inputSurface;
    }

    @Override
    public void updateFrame(RenderNode renderNode, long presentationTimeUs, ComposerEngine engine) {
        Log.d("Composer", "PreviewRenderTarget#updateFrame: " + presentationTimeUs);
        programObject.use();
        GLES20.glBindAttribLocation(programObject.getProgramId(), 0, ProgramObject.ATTRIBUTE_POSITION);
        GLES20.glBindAttribLocation(programObject.getProgramId(), 1, ProgramObject.ATTRIBUTE_TEX_COORDS);
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_SHORT, false, 0, texCoordBuffer);
        GLES20.glEnableVertexAttribArray(1);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        renderNode.getOutputTexture().bind(0);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, renderNode.getTransformMatrix(), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        engine.swapBuffers();
    }

    @Override
    public void updateAudioChunk(AudioSource audioSource) {
        if (audioTrack != null) {
            if (audioSource.getRenderOutputStatus() == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                audioTrack.setPlaybackRate(audioSource.getSampleRate());
            } else if (audioSource.getRenderOutputStatus() >= 0) {
                MediaCodec.BufferInfo info = audioSource.getAudioInfo();
                audioTrack.write(audioSource.getChunk(), info.offset, info.offset + info.size);
                audioSource.resetChunk();
            }
        }
    }

    @Override
    public void prepareVideo() {
        programObject = new ProgramObject();
        vertexBuffer = ByteBuffer.allocateDirect(DEFAULT_VERTEX_DATA.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(DEFAULT_VERTEX_DATA).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(DEFAULT_TEX_COORDS_DATA.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        texCoordBuffer.put(DEFAULT_TEX_COORDS_DATA).position(0);
    }

    @Override
    public void prepareAudio(int sampleRate, int channelCount) {
        this.sampleRate = sampleRate;
        buffSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                buffSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    @Override
    public void complete() {

    }

    @Override
    public void release() {
        if (programObject != null) {
            programObject.release();
            programObject = null;
        }
        if (inputSurface != null) {
            inputSurface.release();
            inputSurface = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (inputSurface != null) {
            inputSurface.release();
        }
        inputSurface = new Surface(surfaceTexture);
    }
}
