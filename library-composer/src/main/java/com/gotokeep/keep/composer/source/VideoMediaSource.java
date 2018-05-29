package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.exception.UnsupportedFormatException;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.gles.RenderTexture;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:08
 */
public class VideoMediaSource extends MediaSource {
    private static final String VIDEO_MIME_START = "video/";
    private static final int TIMEOUT_US = 1000;

    private static final int MSG_UPDATE_REQUEST = 0;

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
    private List<Long> keyFrames = new ArrayList<>();
    private DecodeThread decodeThread;

    public VideoMediaSource(String filePath) {
        super(TYPE_DYNAMIC);
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
            prepareKeyFrameInfo();
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
            prepareDecodeThread();
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
        synchronized (requestSyncObj) {
            Log.d("DecodeThread", "try to get a frame");
            if (decodeTexture.isFrameAvailable()) {
                encoded = true;
                decodeTexture.updateTexImage();
                Log.d("DecodeThread", "got a frame");
            }
        }
        if (encoded) {
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
        return positionUs;
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
        durationMs = format.containsKey(MediaFormat.KEY_DURATION) ? TimeUtil.usToMs(format.getLong(MediaFormat.KEY_DURATION)) : DURATION_INFINITE;
        long intervalMs = format.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL) ? TimeUtil.usToMs(format.getLong(MediaFormat.KEY_I_FRAME_INTERVAL)) : 0;
        Log.d("VideoMediaSource", "prepareExtractorAndInfo: " + intervalMs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            rotation = format.containsKey(MediaFormat.KEY_ROTATION) ? format.getInteger(MediaFormat.KEY_ROTATION) : 0;
        } else {
            rotation = MediaUtil.getRotation(filePath);
        }
        presentationTimeUs = 0;
    }

    private void prepareKeyFrameInfo() {
        if (extractor == null) {
            return;
        }
        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        keyFrames.clear();
        do {
            int flags = extractor.getSampleFlags();
            long time = extractor.getSampleTime();
            if ((flags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0 && time >= 0) {
                keyFrames.add(time);
                Log.d("KeyFrame", "prepareKeyFrameInfo: " + time);
            }
        } while (extractor.advance());
        extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
    }

    private void prepareDecoder() throws IOException {
        decodeTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL, "decodeTexture");
        decodeSurface = new Surface(decodeTexture.getSurfaceTexture());
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(format, decodeSurface, null, 0);
        decoder.start();
    }

    private void prepareDecodeThread() {
        decodeThread = new DecodeThread(getName());
        decodeThread.start();
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

    private class DecodeThread extends Thread {
        private final String TAG = DecodeThread.class.getSimpleName();
        private boolean ended = false;
        private long requestTimeUs = 0L;
        private long decodedTimeUs = 0L;
        private long currentKeyFrame = 0L;

        DecodeThread(String name) {
            super("DecodeThread:" + name);
        }

        @Override
        public void run() {
            while (!ended) {
                try {
                    Log.d(TAG, "wait for new request");
                    decodeSem.acquire();
                    synchronized (requestSyncObj) {
                        requestTimeUs = renderRequest != null ? renderRequest.requestRenderTimeUs : 0;
                    }
                    Log.d(TAG, "got new RenderRequest: " + requestTimeUs);
                    long actualTimeUs = (long) ((requestTimeUs - TimeUtil.msToUs(startTimeMs)) * playSpeed);
                    if (actualTimeUs > TimeUtil.msToUs(durationMs)) {
                        ended = true;
                    }
                    Log.d(TAG, "actualTime: " + actualTimeUs);
                    long keyFrameTime = TimeUtil.findClosesKeyFrame(keyFrames, actualTimeUs);
                    if (keyFrameTime > currentKeyFrame) {
                        Log.d(TAG, "seek to key frame:" + keyFrameTime);
                        extractor.seekTo(keyFrameTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                        currentKeyFrame = keyFrameTime;
                    }
                    requestUpdated.set(true);
                    while (!ended && requestUpdated.get() && decodedTimeUs < actualTimeUs) {
                        Log.d(TAG, "DECODE BEGIN");
                        int inputIndex;
                        do {
                            inputIndex = decoder.dequeueInputBuffer(0);
                        } while (inputIndex < 0 && !isInterrupted());
                        Log.d(TAG, "got input buffer");
                        ByteBuffer buffer = MediaUtil.getInputBuffer(decoder, inputIndex);
                        int bufferSize = extractor.readSampleData(buffer, 0);
                        Log.d(TAG, "got sample data");
                        long sampleTime = extractor.getSampleTime();
                        int sampleFlags = extractor.getSampleFlags();
                        if (!extractor.advance()) {
                            Log.d(TAG, "no more sample data");
                            ended = true;
                        }
                        if (sampleTime >= 0) {
                            decoder.queueInputBuffer(inputIndex, 0, bufferSize, sampleTime, sampleFlags);
                            Log.d(TAG, "feed sample to decoder");
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            ended = true;
                            Log.d(TAG, "feed EOS to decoder");
                        }
                        int outputIndex;
                        Log.d(TAG, "wait for decoded frame");
                        synchronized (requestSyncObj) {
                            Log.d(TAG, "try to get output buffer");
                            outputIndex = decoder.dequeueOutputBuffer(decodeInfo, 0);
                            if (outputIndex >= 0) {
                                Log.d(TAG, "got a decoded frame");
                                decodedTimeUs = decodeInfo.presentationTimeUs;
//                                    boolean keyFrame = (decodeInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                                decoder.releaseOutputBuffer(outputIndex, true);
                                Log.d(TAG, "render to decodeTexture");
                            }
                        }
                        Log.d(TAG, "DECODE END");
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            Log.d("DecodeThread", "run: finish");
        }
    }
}
