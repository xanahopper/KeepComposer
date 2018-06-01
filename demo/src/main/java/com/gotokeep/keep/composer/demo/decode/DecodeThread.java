package com.gotokeep.keep.composer.demo.decode;

import android.drm.DrmInfoRequest;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.gles.EglCore;
import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.gles.RenderTexture;
import com.gotokeep.keep.social.composer.util.MediaUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class DecodeThread extends Thread {

    public static final int MSG_INIT_TIME = 184;
    public static final int MSG_DECODE_FRAME = 206;
    public static final int MSG_DECODE_COMPLETE = 933;


    private MediaExtractor extractor;
    private int trackIndex = -1;
    private long frameCount = 0;
    private MediaFormat format;
    private MediaCodec decoder;
    private MediaCodec.BufferInfo decodeInfo;
    private Surface outputSurface;
    private RenderTexture decodeTexture;
    private RenderTexture renderTexture;
    private boolean ended = false;

    private TimeRange initTime = new TimeRange();
    private int sourceIndex;
    private String sourcePath;
    private Handler handler;


    public DecodeThread(int sourceIndex, Handler eventHandler) {
        this.sourceIndex = sourceIndex;
        sourcePath = SourceProvider.VIDEO_SRC[sourceIndex];
        handler = eventHandler;
    }

    @Override
    public void run() {
        try {
            prepareContext();
            prepareExtractor();
            initTime.startTime = SystemClock.elapsedRealtimeNanos();
            prepareDecoder();
            initTime.endTime = SystemClock.elapsedRealtimeNanos();
            handler.obtainMessage(MSG_INIT_TIME, initTime).sendToTarget();
            FrameInfo info;
            do {
                info = doDecode();
                if (info != null) {
                    handler.obtainMessage(MSG_DECODE_FRAME, info).sendToTarget();
                }
            } while (!ended);
            Log.d("DecodeThread", "Complete");
            handler.obtainMessage(MSG_DECODE_COMPLETE, sourceIndex, 0).sendToTarget();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            releaseDecoder();
            releaseExtractor();
            releaseContext();
        }
    }

    private void prepareContext() {
        decodeTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL, "DecodeTexture");
        renderTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE, "RenderTexture");
        outputSurface = new Surface(decodeTexture.getSurfaceTexture());
    }

    private void prepareExtractor() throws InterruptedException {
        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(sourcePath);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    trackIndex = i;
                    break;
                }
            }
            if (trackIndex == -1) {
                throw new IOException();
            }
            extractor.selectTrack(trackIndex);
        } catch (IOException e) {
            throw new InterruptedException();
        }
    }

    private void prepareDecoder() throws InterruptedException {
        String mime = format.getString(MediaFormat.KEY_MIME);
        try {
            decoder = MediaCodec.createDecoderByType(mime);
            decodeInfo = new MediaCodec.BufferInfo();
            decoder.configure(format, outputSurface, null, 0);
            decoder.start();
        } catch (IOException e) {
            throw new InterruptedException();
        }
    }

    private FrameInfo doDecode() {
        FrameInfo info = new FrameInfo();
        boolean decoded = false;
        while (!ended && !decoded) {
            int inputIndex;
            int bufferSize;
            int sampleFlags;
            info.inputBuffer.startTime = SystemClock.elapsedRealtimeNanos();
            inputIndex = decoder.dequeueInputBuffer(0);
            if (inputIndex >= 0) {
                ByteBuffer buffer = MediaUtil.getInputBuffer(decoder, inputIndex);
                info.readSample.startTime = info.inputBuffer.endTime = SystemClock.elapsedRealtimeNanos();
                bufferSize = extractor.readSampleData(buffer, 0);
                long sampleTime = extractor.getSampleTime();
                sampleFlags = extractor.getSampleFlags();
                if (!extractor.advance() || sampleTime < 0) {
                    ended = true;
                }
                info.decode.startTime = info.readSample.endTime = SystemClock.elapsedRealtimeNanos();
                info.presentationTimeUs = -1;
                if (ended) {
                    decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    decoder.queueInputBuffer(inputIndex, 0, bufferSize, sampleTime, sampleFlags);
                    int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, 0);
                    if (outputIndex >= 0) {
                        info.presentationTimeUs = decodeInfo.presentationTimeUs;
                        decoder.releaseOutputBuffer(outputIndex, true);
                        decoded = true;
                    }
                }
            }

            info.decode.endTime = SystemClock.elapsedRealtimeNanos();
//            if (decodeTexture.isFrameAvailable()) {
//                info.updateTexImage.startTime = SystemClock.elapsedRealtimeNanos();
//                synchronized (contextSyncObj) {
//                    eglCore.makeCurrent(eglSurface);
//                    decodeTexture.updateTexImage();
//                    drawTexture(decodeTexture);
//                }
//                info.updateTexImage.endTime = SystemClock.elapsedRealtimeNanos();
//            }
        }
        info.sourceIndex = this.sourceIndex;
        info.decoded = decoded;
        info.frame = frameCount++;
        return info;
    }

    private void releaseDecoder() {
        if (decoder != null) {
            decoder.release();
            decoder = null;
        }
    }

    private void releaseExtractor() {
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
    }

    private void releaseContext() {
        if (outputSurface != null) {
            outputSurface.release();
            outputSurface = null;
        }
        if (decodeTexture != null) {
            decodeTexture.release();
        }
    }

    public RenderTexture getRenderTexture() {
        return renderTexture;
    }

    public RenderTexture getDecodeTexture() {
        return decodeTexture;
    }
}