package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.composer.exception.UnsupportedFormatException;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.gles.RenderTexture;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:08
 */
public class VideoMediaSource extends MediaSource implements Handler.Callback {
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
    private List<Long> keyFrames = new ArrayList<>();
    private HandlerThread renderThread;
    private Handler renderHandler;

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

        /* TODO
         * sync(renderThread) {
         *     if (decodeTexture.isAvailable()) {
         *         decodeTexture.updateTexImage();
         *         // do render things...
         *     } else {
         *         return 0;
         *     }
         * }
         */

//        while (!encoded && !ended && this.presentationTimeUs <= actualTimeUs) {
//            Log.d("DecoderBuffer", getName() + " request buffer" + SystemClock.elapsedRealtime());
//            try {
//                decodeSem.acquire();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                return 0;
//            }
//            Log.d("DecoderBuffer", getName() + " got feed and drain output " + SystemClock.elapsedRealtime());
//            int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, TIMEOUT_US);
//            if (outputIndex > 0) {
//                this.presentationTimeUs = decodeInfo.presentationTimeUs;
//                boolean keyFrame = (decodeInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
//                decoder.releaseOutputBuffer(outputIndex, presentationTimeUs >= actualTimeUs);
//                encoded = presentationTimeUs >= actualTimeUs;
//                Log.d("DecoderBuffer", getName() + " decode complete" + SystemClock.elapsedRealtime());
//            }
//            feedSem.release();
//        }
        if (encoded) {
//            Log.d("DecoderBuffer", "doRender[" + name + "]: rendered a frame " + this.presentationTimeUs);
            Log.d("DecoderBuffer", getName() + " updateTexImage" + SystemClock.elapsedRealtime());
            decodeTexture.updateTexImage();
            Log.d("DecoderBuffer", getName() + " renderTo Texture2D" + SystemClock.elapsedRealtime());
            long renderTimeUs = super.render(positionUs);
            Log.d("DecoderBuffer", getName() + " renderDone" + SystemClock.elapsedRealtime());
            return renderTimeUs;
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
            if ((flags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                long time = extractor.getSampleTime();
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

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
