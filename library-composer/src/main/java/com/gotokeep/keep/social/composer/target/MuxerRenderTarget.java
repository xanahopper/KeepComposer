package com.gotokeep.keep.social.composer.target;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.gotokeep.keep.social.composer.ComposerEngine;
import com.gotokeep.keep.social.composer.ExportConfiguration;
import com.gotokeep.keep.social.composer.RenderNode;
import com.gotokeep.keep.social.composer.RenderTarget;
import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.source.AudioSource;
import com.gotokeep.keep.social.composer.util.MediaUtil;

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
    private static final String VIDEO_MIME = "video/avc";
    private static final String AUDIO_MIME = "audio/mp4a-latm";
    private static final int DEFAULT_AUDIO_BIT_RATE = 128 * 1024;
    private static final int DEFAULT_AUDIO_AAC_PROFILE =
            MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    private static final long TIMEOUT_US = 10000;

    private MediaCodec videoEncoder;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoInfo = new MediaCodec.BufferInfo();
    private boolean hasAudio = false;
    private MediaCodec audioEncoder;
    private MediaFormat audioFormat;
    private MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();

    private MediaFormat videoOutputFormat;
    private MediaFormat audioOutputFormat;

    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;
    private MediaMuxer muxer;
    private boolean muxing = false;
    private Surface encodeInputSurface;
    private boolean videoEncoderDone = false;
    private boolean audioEncoderDone = false;

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
            this.videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME);
            prepareVideoEncoder();
            this.encodeInputSurface = videoEncoder.createInputSurface();
            prepareMuxer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Surface getInputSurface() {
        return encodeInputSurface;
    }

    @Override
    public void updateFrame(RenderNode renderNode, long presentationTimeUs, ComposerEngine engine) {
        drawFrame(renderNode, presentationTimeUs);
        engine.swapBuffers();
        drainVideoEncoder();
        if (videoTrackIndex < 0 && videoOutputFormat != null) {
            videoTrackIndex = muxer.addTrack(videoOutputFormat);
            updateMuxerState();
        }
    }

    @Override
    public void updateAudioChunk(AudioSource audioSource) {
        while (audioTrackIndex >= 0 && !muxing) {
            // wait for mux begin
        }
        drainAudioEncoder(audioSource);
        if (audioTrackIndex < 0 && audioOutputFormat != null) {
            audioTrackIndex = muxer.addTrack(audioOutputFormat);
            updateMuxerState();
        }
    }

    @Override
    public void prepareVideo() {
        prepareRenderProgram();
        videoEncoder.start();
    }

    private void drawFrame(RenderNode renderNode, long presentationTimeUs) {
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
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXCOORD_MATRIX),
                1, false, renderNode.getTexCoordMatrix(), 0);
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    private void prepareRenderProgram() {
        if (programObject == null) {
            programObject = new ProgramObject();
            vertexBuffer = ByteBuffer.allocateDirect(DEFAULT_VERTEX_DATA.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexBuffer.put(DEFAULT_VERTEX_DATA).position(0);

            texCoordBuffer = ByteBuffer.allocateDirect(DEFAULT_TEX_COORDS_DATA.length * 2)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
            texCoordBuffer.put(DEFAULT_TEX_COORDS_DATA).position(0);
        }
    }

    private void prepareVideoEncoder() {
        videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME,
                exportConfiguration.getWidth(), exportConfiguration.getHeight());
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, exportConfiguration.getVideoBitRate());
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, exportConfiguration.getFrameRate());
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, exportConfiguration.getKeyFrameInterval());
        videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    @Override
    public void prepareAudio(int sampleRate, int channelCount) {
        hasAudio = true;
        audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME,
                sampleRate, channelCount);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, exportConfiguration.getAudioBitRate());
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, DEFAULT_AUDIO_AAC_PROFILE);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 8192);
        try {
            audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME);
            audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            audioEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        // reset muxer
        // reset encoder
        // reset
    }

    private void prepareMuxer() {
        if (muxer == null) {
            try {
                muxer = new MediaMuxer(exportConfiguration.getOutputFilePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                throw new RuntimeException("MediaMuxer prepare failed.", e);
            }
        }
    }

    private void drainVideoEncoder() {
        if (!videoEncoderDone && (videoOutputFormat == null || muxing)) {
            int outputIndex = videoEncoder.dequeueOutputBuffer(videoInfo, TIMEOUT_US);
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                return;
            }
            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                videoOutputFormat = videoEncoder.getOutputFormat();
                return;
            }
            if (outputIndex >= 0) {
                ByteBuffer outputBuffer = MediaUtil.getOutputBuffer(videoEncoder, outputIndex);
                if ((videoInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    videoEncoder.releaseOutputBuffer(outputIndex, false);
                    return;
                }
                if (videoInfo.size != 0) {
                    muxer.writeSampleData(videoTrackIndex, outputBuffer, videoInfo);
                }
                if ((videoInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d("muxer", "drainVideoEncoder: end");
                    videoEncoderDone = true;
                }
            }
            videoEncoder.releaseOutputBuffer(outputIndex, false);
        }
    }

    private void drainAudioEncoder(AudioSource audioSource) {
        if (videoOutputFormat == null || muxing) {
            // feed input buffer
            if (audioSource.getRenderOutputStatus() < 0) {
                return;
            }
            audioInfo = audioSource.getAudioInfo();
            int size = audioInfo.size;
            long presentationTime = audioInfo.presentationTimeUs;
            int inputIndex = audioEncoder.dequeueInputBuffer(TIMEOUT_US);
            if (inputIndex >= 0) {
                ByteBuffer inputBuffer = MediaUtil.getInputBuffer(audioEncoder, inputIndex);
                inputBuffer.clear();
                if (size >= 0) {
                    inputBuffer.position(0);
                    inputBuffer.put(audioSource.getChunk());
                    audioSource.resetChunk();
                    audioEncoder.queueInputBuffer(inputIndex, 0, size, presentationTime,
                            audioInfo.flags);
                } else {
                    audioEncoder.queueInputBuffer(inputIndex, 0, 0, 0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
            }

            // poll from encoder
            int outputIndex = audioEncoder.dequeueOutputBuffer(audioInfo, TIMEOUT_US);
            if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                return;
            }
            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                audioOutputFormat = audioEncoder.getOutputFormat();
                return;
            }
            if (outputIndex >= 0) {
                ByteBuffer outputBuffer = MediaUtil.getOutputBuffer(audioEncoder, outputIndex);
                if ((audioInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    audioEncoder.releaseOutputBuffer(outputIndex, false);
                    return;
                }

                if (audioInfo.size > 0 && audioInfo.presentationTimeUs >= 0) {
                    Log.d("Muxer", "writeSampleData: " + audioInfo.size + ", " + audioInfo.presentationTimeUs + ", " +
                             + audioInfo.flags + ", " + outputBuffer.limit());
                    muxer.writeSampleData(audioTrackIndex, outputBuffer, audioInfo);
                }
                if ((audioInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    audioEncoderDone = true;
                }
                outputBuffer.clear();
            }
            audioEncoder.releaseOutputBuffer(outputIndex, false);
        }
    }

    private void updateMuxerState() {
        if (videoTrackIndex >= 0 && (audioTrackIndex >= 0 || !hasAudio) && !muxing) {
            muxer.start();
            muxing = true;
        }
    }

    @Override
    public void complete() {
        Log.d("muxer", "complete");
//        if (videoEncoder != null && !videoEncoderDone) {
//            Log.d("muxer", "complete: video done");
//            videoEncoder.signalEndOfInputStream();
//        }
//        if (audioEncoder != null && !audioEncoderDone) {
//            audioEncoder.signalEndOfInputStream();
//        }
        if (muxing) {
            muxer.stop();
            muxing = false;
        }
    }

    @Override
    public void release() {
        if (programObject != null) {
            programObject.release();
            programObject = null;
        }
        if (videoEncoder != null) {
            try {
                videoEncoder.stop();
            } catch (IllegalStateException e) {
                // ignore
            }
            videoEncoder.release();
            videoEncoder = null;
        }
        if (audioEncoder != null) {
            try {
                audioEncoder.stop();
            } catch (IllegalStateException e) {
                // ignore
            }
            audioEncoder.release();
            audioEncoder = null;
        }
        if (muxer != null) {
            if (muxing) {
                try {
                    muxer.stop();
                } catch (RuntimeException e) {
                    Log.e("Muxer", "stop muxer failed: ", e);
                }
            }
            muxer.release();
            muxer = null;
        }
    }
}
