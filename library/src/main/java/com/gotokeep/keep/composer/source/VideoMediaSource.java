package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Surface;

import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.exception.UnsupportedFormatException;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:08
 */
public class VideoMediaSource extends MediaSource implements Handler.Callback {
    private static final String VIDEO_MIME_START = "video/";
    private static final int TIMEOUT_US = 1000;
    private static final int MSG_RENDER = 0;

    private static final String EXTERNAL_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES uTexture;\n" +
            "varying vec2 vTexCoords;\n" +
            "void main() { \n" +
            "    gl_FragColor = texture2D(uTexture, vTexCoords);\n" +
            "}\n";

    private String filePath;
    private MediaExtractor extractor;
    private int trackIndex = -1;
    private String mime = "";

    private HandlerThread decodeThread;
    private Handler decodeHandler;
    private MediaFormat format;
    private MediaCodec decoder;
    private MediaCodec.BufferInfo decodeInfo = new MediaCodec.BufferInfo();
    private RenderTexture decodeTexture;
    private Surface decodeSurface;

    private long sampleTimeUs = 0;
    private int sampleFlags = 0;

    public VideoMediaSource(String filePath) {
        super(TYPE_VIDEO);
        this.filePath = filePath;
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject(EXTERNAL_FRAGMENT_SHADER, ProgramObject.DEFAULT_UNIFORM_NAMES);
    }

    @Override
    public void onPrepare() {
        // this called from GL(engine) thread
        if (decoder != null || extractor != null) {
            throw new IllegalStateException("VideoMediaSource already prepared.");
        }
        try {
            prepareExtractorAndInfo();
            prepareDecoder();
            prepareThread();
        } catch (IOException e) {
            throw new RuntimeException("VideoMediaSource prepare failed.", e);
        }
    }

    private void renderInternal(long presentationTimeUs) {
        long actualTimeUs = (long) ((presentationTimeUs - TimeUtil.msToUs(startTimeMs)) * playSpeed);
        if (actualTimeUs > TimeUtil.msToUs(durationMs)) {
            ended = true;
        }
        boolean encoded = false;
        while (!encoded && !ended && this.presentationTimeUs < actualTimeUs) {
            int inputIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputIndex >= 0) {
                ByteBuffer buffer = MediaUtil.getInputBuffer(decoder, inputIndex);
                buffer.clear();
                int bufferSize = extractor.readSampleData(buffer, 0);
                sampleTimeUs = extractor.getSampleTime();
                sampleFlags = extractor.getSampleFlags();
                decoder.queueInputBuffer(inputIndex, 0, bufferSize, sampleTimeUs, sampleFlags);
                if (!extractor.advance()) {
                    ended = true;
                }
            } else {
                throw new RuntimeException("Cannot dequeue an decoder input buffer.");
            }
            int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_US);
            if (outputIndex > 0) {
                encoded = true;
                decoder.releaseOutputBuffer(outputIndex, sampleTimeUs >= actualTimeUs);
                this.presentationTimeUs = decodeInfo.presentationTimeUs;
            }
        }
        if (!encoded) {
            renderTexture.notifyNoFrame();
        }
        decodeTexture.awaitFrameAvailable();
        programObject.use();
        decodeTexture.bind(0);
        renderTexture.setRenderTarget();
        updateProgramUniform(programObject, decodeTexture);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onRelease() {
        if (decodeThread != null) {
            decodeThread.quitSafely();
            decodeThread = null;
        }
        decodeHandler = null;

        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
        if (decoder != null) {
            decoder.stop();
            decoder.release();
            decoder = null;
        }
        format = null;
        presentationTimeUs = 0;
    }

    @Override
    protected void onRender(ProgramObject programObject, long presentationTimeUs) {
        if (decodeHandler == null) {
            throw new IllegalStateException("VideoMediaSource must prepare before render.");
        }
        decodeHandler.obtainMessage(MSG_RENDER, presentationTimeUs).sendToTarget();
    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }

    private void updateProgramUniform(ProgramObject programObject, RenderTexture decodeTexture) {
        float transform[] = new float[16];
        decodeTexture.getSurfaceTexture().getTransformMatrix(transform);
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX), 1, false, transform, 0);
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
    }

    private void prepareExtractorAndInfo() throws IOException {
        extractor = new MediaExtractor();
        extractor.setDataSource(filePath);
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat trackFormat = extractor.getTrackFormat(i);
            mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(VIDEO_MIME_START)) {
                trackIndex = i;
                format = trackFormat;
                break;
            }
        }

        if (trackIndex < 0) {
            throw new UnsupportedFormatException("Cannot find supported video track.");
        }
        extractor.selectTrack(trackIndex);
        width = format.containsKey(MediaFormat.KEY_WIDTH) ? format.getInteger(MediaFormat.KEY_WIDTH) : 0;
        height = format.containsKey(MediaFormat.KEY_HEIGHT) ? format.getInteger(MediaFormat.KEY_HEIGHT) : 0;
        durationMs = format.containsKey(MediaFormat.KEY_DURATION) ? format.getInteger(MediaFormat.KEY_DURATION) :
                DURATION_INFINITE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : 0;
        } else {
            rotation = MediaUtil.getRotation(filePath);
        }
        presentationTimeUs = 0;
    }

    private void prepareDecoder() throws IOException {
        decodeTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL);
        decodeSurface = new Surface(decodeTexture.getSurfaceTexture());
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(format, decodeSurface, null, 0);
        decoder.start();
    }

    private void prepareThread() {
        String name = MediaUtil.getName(filePath);
        decodeThread = new HandlerThread("VideoMediaSource:" + name);
        decodeThread.start();
        decodeHandler = new Handler(decodeThread.getLooper(), this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_RENDER:
                renderInternal((Long) msg.obj);
                return true;
        }
        return false;
    }
}
