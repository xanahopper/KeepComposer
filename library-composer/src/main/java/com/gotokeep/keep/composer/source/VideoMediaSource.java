package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.gles.RenderTexture;
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
    protected void onPreload() {
        try {
            prepareExtractorAndInfo();
        } catch (IOException e) {
            throw new RuntimeException("VideoMediaSource preload failed.", e);
        }
    }

    @Override
    public void onPrepare() {
        // this called from GL(engine) thread
        if (decoder != null) {
            throw new IllegalStateException("VideoMediaSource already prepared.");
        }
        try {
            prepareDecoder();
        } catch (IOException e) {
            throw new RuntimeException("VideoMediaSource prepareVideo failed.", e);
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
        if (decodeTexture != null) {
            decodeTexture.release();
            decodeTexture = null;
        }
        format = null;
        presentationTimeUs = 0;
    }

    @Override
    public long render(long positionUs) {
        long actualTimeUs = (long) ((positionUs - TimeUtil.msToUs(startTimeMs)) * playSpeed);
        if (actualTimeUs > TimeUtil.msToUs(durationMs)) {
            Log.d("Composer", "doRender: === END ===");
            ended = true;
        }
        boolean encoded = false;
        while (!encoded && !ended && this.presentationTimeUs <= actualTimeUs) {
            int inputIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputIndex >= 0) {
                ByteBuffer buffer = MediaUtil.getInputBuffer(decoder, inputIndex);
                int bufferSize;
//                do {
                    buffer.clear();
                    bufferSize = extractor.readSampleData(buffer, 0);
                    sampleTimeUs = extractor.getSampleTime();
                    sampleFlags = extractor.getSampleFlags();
                    if (!extractor.advance() || sampleTimeUs < 0) {
                        ended = true;
                    }
                    Log.d("VideoMediaSource", "readSampleData: sampleTimeUs = " + sampleTimeUs + ", flags = " +
                            sampleFlags);
//                } while (sampleTimeUs <= actualTimeUs &&
//                        ((sampleFlags & MediaExtractor.SAMPLE_FLAG_SYNC) == 0) &&
//                        !ended);
                Log.d("VideoMediaSource", "render: feed to decoder input buffer " + sampleTimeUs);
                if (!ended) {
                    decoder.queueInputBuffer(inputIndex, 0, bufferSize, sampleTimeUs, sampleFlags);
                }
            } else {
                Log.w("VideoMediaSource", "doRender: cannot dequeue input buffer from decoder, reason: " + inputIndex);
            }
            int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_US);
            if (outputIndex > 0) {
                this.presentationTimeUs = decodeInfo.presentationTimeUs;
                boolean keyFrame = (decodeInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                decoder.releaseOutputBuffer(outputIndex, presentationTimeUs >= actualTimeUs);
                encoded = presentationTimeUs >= actualTimeUs;
            }
        }
        if (encoded) {
//            Log.d("VideoMediaSource", "doRender[" + name + "]: rendered a frame " + this.presentationTimeUs);
            decodeTexture.updateTexImage();
            return super.render(positionUs);
        } else {
            decodeTexture.notifyNoFrame();
            renderTexture.notifyNoFrame();
            return 0;
        }
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return getPresentationTimeUs() + TimeUtil.msToUs(startTimeMs);
    }

    @Override
    public RenderTexture getOutputTexture() {
        return renderTexture;
    }

    @Override
    protected void bindRenderTextures() {
        decodeTexture.bind(0);
      //checkGlError("bindDecodeTexture");
    }

    @Override
    protected void unbindRenderTextures() {
        decodeTexture.unbind(0);
      //checkGlError("unbindDecodeTexture");
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, decodeTexture.getTransitionMatrix(), 0);
      //checkGlError("updateDecodeTextureTransformMatrix");
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
      //checkGlError("updateDecodeTextureId");

    }

    private void prepareExtractorAndInfo() throws IOException {
        if (extractor == null) {
            extractor = new MediaExtractor();
        }
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
        durationMs = format.containsKey(MediaFormat.KEY_DURATION) ? TimeUtil.usToMs(format.getLong(MediaFormat
                .KEY_DURATION)) :
                DURATION_INFINITE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : 0;
        } else {
            rotation = MediaUtil.getRotation(filePath);
        }
        presentationTimeUs = 0;
    }

    private void prepareDecoder() throws IOException {
        decodeTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL, "decodeTexture");
        decodeSurface = new Surface(decodeTexture.getSurfaceTexture());
        decoder = MediaCodec.createDecoderByType(mime);
        Log.d("VideoMediaSource", "decoder name: " + decoder.getName());
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

    @Override
    public String getName() {
        return "VideoMediaSource[" + Uri.parse(filePath).getLastPathSegment() + "]";
    }
}
