package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
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
public class VideoMediaSource extends MediaSource {
    private static final String VIDEO_MIME_START = "video/";
    private static final int TIMEOUT_US = 1000;

    private static final String EXTERNAL_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
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
    private String name;

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
        name = Uri.parse(filePath).getLastPathSegment();
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
        } catch (IOException e) {
            throw new RuntimeException("VideoMediaSource prepare failed.", e);
        }
    }

    @Override
    public void onRelease() {

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
    public long render(long positionUs) {
        long actualTimeUs = (positionUs - TimeUtil.msToUs(startTimeMs));
        if (actualTimeUs > getRealTime(TimeUtil.msToUs(durationMs))) {
            Log.d("Composer", "doRender: === END ===");
            ended = true;
        }
        boolean encoded = false;
        Log.v("VideoMediaSource", "doRender[" + name +"]: actualTimeUs  = " + actualTimeUs + ", presentationTimeUs = " + presentationTimeUs);
        while (!encoded && !ended && this.presentationTimeUs <= actualTimeUs) {
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
                Log.w("VideoMediaSource", "doRender: cannot dequeue input buffer from decoder");
            }
            int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_US);
            if (outputIndex > 0) {
                encoded = true;
                this.presentationTimeUs = getRealTime(decodeInfo.presentationTimeUs);
                decoder.releaseOutputBuffer(outputIndex, presentationTimeUs >= actualTimeUs);
            }
        }
        if (!encoded) {
            renderTexture.notifyNoFrame();
            return 0;
        } else {
            Log.d("VideoMediaSource", "doRender[" + name + "]: rendered a frame " + this.presentationTimeUs);
            decodeTexture.getSurfaceTexture().updateTexImage();
            return super.render(positionUs);
        }
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return getPresentationTimeUs();
    }

    @Override
    protected void bindRenderTextures() {
        decodeTexture.bind(0);
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, decodeTexture.getTransitionMatrix(), 0);
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
        durationMs = format.containsKey(MediaFormat.KEY_DURATION) ? TimeUtil.usToMs(format.getLong(MediaFormat.KEY_DURATION)):
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

    @Override
    public long getPresentationTimeUs() {
        return getRealTime(presentationTimeUs);
    }

    private long getRealTime(long time) {
        return (long) (time / playSpeed);
    }
}
