package com.gotokeep.keep.composer.target;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import com.gotokeep.keep.composer.ExportConfiguration;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTarget;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.source.AudioSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 10:41
 */
public class MuxerRenderTarget extends RenderTarget {
    private static final String RENDER_MIME = "video/avc";

    private MediaCodec encoder;
    private MediaFormat encodeFormat;
    private MediaCodec.BufferInfo encodeInfo;
    private int muxerTrackIndex = -1;
    private MediaMuxer muxer;
    private Surface encodeInputSurface;

    private ExportConfiguration exportConfiguration;
    private String exportPath;
    private ProgramObject programObject;

    protected static final float[] DEFAULT_VERTEX_DATA = {
            -1f, -1f, 0,
            1f, -1f, 0,
            -1f, 1f, 0,
            1f, 1f, 0};
    static final short[] DEFAULT_TEX_COORDS_DATA = {0, 0, 1, 0, 0, 1, 1, 1};

    protected FloatBuffer vertexBuffer;
    protected ShortBuffer texCoordBuffer;

    public MuxerRenderTarget(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;

        try {
            this.encoder = MediaCodec.createEncoderByType(RENDER_MIME);
            this.encodeInputSurface = encoder.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Surface getInputSurface() {
        return encodeInputSurface;
    }

    @Override
    public void updateFrame(RenderNode renderNode, long presentationTimeUs) {

    }

    @Override
    public void updateAudioChunk(AudioSource audioSource) {

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
    public void prepareAudio(int sampleRate) {

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
    }
}
