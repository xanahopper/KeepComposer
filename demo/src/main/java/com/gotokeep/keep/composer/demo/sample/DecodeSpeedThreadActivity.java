package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGLSurface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;

import com.gotokeep.keep.composer.demo.R;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.gles.EglCore;
import com.gotokeep.keep.social.composer.gles.RenderTexture;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DecodeSpeedThreadActivity extends AppCompatActivity implements Handler.Callback, TextureView
        .SurfaceTextureListener {

    private static final int MSG_INIT_TIME = 184;
    private static final int MSG_DECODE_FRAME = 206;
    private static final int MSG_DECODE_COMPLETE = 933;
    private static final String TAG = "DecodeSpeedTest";

    private Handler handler;
    private DecodeThread decodeThreads[] = new DecodeThread[2];
    private List<FrameInfo> frameInfos = new ArrayList<>();

    private TextureView previewView;
    private RecyclerView recyclerView;
    private SurfaceTexture surfaceTexture;
    private EglCore eglCore;
    private EGLSurface eglSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_speed_thread);
        handler = new Handler(this);

        previewView = findViewById(R.id.preview_view);
        previewView.setSurfaceTextureListener(this);

        initContext();
    }

    private void initContext() {
        eglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.decode_speed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                startDecode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startDecode() {
        for (int i = 0; i < decodeThreads.length; i++) {
            DecodeThread thread = decodeThreads[i];
            if (thread == null && surfaceTexture != null) {
                frameInfos.clear();
                thread = new DecodeThread(i, handler);
                decodeThreads[i] = thread;
                thread.start();
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT_TIME:
                TimeRange initTime = (TimeRange) msg.obj;
                Log.d(TAG, "initTime: " + initTime);
                return true;
            case MSG_DECODE_FRAME:
                FrameInfo frameInfo = (FrameInfo) msg.obj;
                frameInfos.add(frameInfo);
                Log.d(TAG, "decodeInfo: " + frameInfo);
                return true;
            case MSG_DECODE_COMPLETE:
                try {
                    for (DecodeThread thread : decodeThreads) {
                        thread.join();
                    }
                } catch (InterruptedException e) {
                    //
                }
                Log.d(TAG, "========= COMPLETE ========");
                return true;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.surfaceTexture = surface;
        eglSurface = eglCore.createWindowSurface(new Surface(surfaceTexture));
        eglCore.makeCurrent(eglSurface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (eglCore != null) {
            eglCore.makeNothingCurrent();
            if (eglSurface != null) {
                eglCore.releaseSurface(eglSurface);
            }
            eglCore.release();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private static class TimeRange {
        long startTime;
        long endTime;

        @Override
        public String toString() {
            return "TimeRange(" + TimeUtil.nsToUs(endTime - startTime) + ")";
        }
    }

    private static class FrameInfo {
        long presentationTimeUs;
        TimeRange inputBuffer = new TimeRange();
        TimeRange readSample = new TimeRange();
        TimeRange decode = new TimeRange();
        TimeRange updateTexImage = new TimeRange();

        @Override
        public String toString() {
            return "FrameInfo{\n " +
                    "presentationTimeUs=" + TimeUtil.usToString(presentationTimeUs) +
                    ",\n inputBuffer=" + inputBuffer +
                    ",\n readSample=" + readSample +
                    ",\n decode=" + decode +
                    ",\n updateTexImage=" + updateTexImage +
                    '}';
        }
    }

    private static class DecodeThread extends Thread {
        private MediaExtractor extractor;
        private int trackIndex = -1;
        private MediaFormat format;
        private MediaCodec decoder;
        private MediaCodec.BufferInfo decodeInfo;
        private Surface outputSurface;
        private RenderTexture renderTexture;
        private boolean ended = false;

        private TimeRange initTime = new TimeRange();
        private String sourcePath;
        private Handler handler;

        DecodeThread(int sourceIndex, Handler eventHandler) {
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
                handler.sendEmptyMessage(MSG_DECODE_COMPLETE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                releaseDecoder();
                releaseExtractor();
                releaseContext();
            }
        }

        private void prepareContext() {
            renderTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL, "DecodeTexture");
            outputSurface = new Surface(renderTexture.getSurfaceTexture());
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
                if (renderTexture.isFrameAvailable()) {
                    info.updateTexImage.startTime = SystemClock.elapsedRealtimeNanos();
//                    renderTexture.updateTexImage();
//                    drawTexture(renderTexture);
                    info.updateTexImage.endTime = SystemClock.elapsedRealtimeNanos();
                }
            }
            return info;
        }

        private void drawTexture(RenderTexture renderTexture) {

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
            if (renderTexture != null) {
                renderTexture.release();
            }
        }
    }
}
