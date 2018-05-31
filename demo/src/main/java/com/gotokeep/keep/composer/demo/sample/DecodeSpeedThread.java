package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;

import com.gotokeep.keep.composer.demo.R;
import com.gotokeep.keep.social.composer.gles.EglCore;
import com.gotokeep.keep.social.composer.gles.RenderTexture;

public class DecodeSpeedThread extends AppCompatActivity implements Handler.Callback {

    private static final int MSG_INIT_TIME = 184;
    private static final int MSG_DECODE_FRAME = 206;
    private static final int MSG_DECODE_COMPLETE = 933;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_speed_thread);
        handler = new Handler(this);
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

    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    private static class TimeRange {
        long startTime;
        long endTime;
    }

    private static class FrameInfo {
        long presentationTimeUs;
        TimeRange readSample;
        TimeRange inputBuffer;
        TimeRange decode;
        TimeRange updateTexImage;
    }

    private class DecodeThread extends Thread {
        private MediaExtractor extractor;
        private int trackIndex = -1;
        private MediaFormat format;
        private MediaCodec decoder;
        private MediaCodec.BufferInfo decodeInfo;
        private Surface outputSurface;
        private EglCore eglCore;
        private EGLSurface eglSurface;
        private RenderTexture renderTexture;

        @Override
        public void run() {
            prepareContext();
            prepareExtractor();
            prepareDecoder();
            doDecode();
            releaseDecoder();
            releaseExtractor();
            releaseContext();
        }

        private void prepareContext() {
            eglCore = new EglCore();
            renderTexture = new RenderTexture(RenderTexture.TEXTURE_EXTERNAL, "DecodeTexture");
            eglSurface = eglCore.createWindowSurface(renderTexture.getSurfaceTexture());
        }

        private void prepareExtractor() {

        }

        private void prepareDecoder() {

        }

        private void doDecode() {

        }

        private void releaseDecoder() {

        }

        private void releaseExtractor() {

        }

        private void releaseContext() {
            
        }
    }
}
