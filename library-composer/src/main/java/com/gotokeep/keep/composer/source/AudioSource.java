package com.gotokeep.keep.composer.source;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.gotokeep.keep.composer.exception.UnsupportedFormatException;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 16:04
 */
public final class AudioSource {
    float playSpeed = 1f;
    String filePath;
    long startTimeMs;
    long endTimeMs;
    long durationMs;
    int sampleRate;
    int channelCount;

    private int audioTrack = -1;
    private MediaExtractor audioExtractor;
    private MediaFormat audioFormat;
    private String audioMime;
    private MediaCodec audioDecoder;
    private MediaCodec.BufferInfo audioInfo = new MediaCodec.BufferInfo();
    private long sampleTimeUs;
    private int sampleFlags;
    private byte[] chunk;

    long presentationTimeUs;
    long renderTimeUs;
    int renderOutputStatus;
    boolean ended = false;

    public AudioSource(String filePath) {
        this.filePath = filePath;
    }

    public void prepare() {
        try {
            prepareExtractor();
            prepareDecoder();
        } catch (Exception e) {
            throw new RuntimeException("AudioSource prepareVideo failed.", e);
        }
    }

    public void seekTo(long positionUs) {
        if (audioExtractor != null) {
            audioExtractor.seekTo(positionUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        }
    }

    public void release() {
        if (audioDecoder != null) {
            audioDecoder.stop();
            audioDecoder.release();
            audioDecoder = null;
        }

        if (audioExtractor != null) {
            audioExtractor.release();
            audioExtractor = null;
        }
    }

    public void setTimeRange(long startTimeMs, long endTimeMs) {
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
    }

    public long acquireBuffer(long positionUs) {
        if (positionUs >= renderTimeUs) {
            renderTimeUs = render(positionUs);
        } else {
            chunk = new byte[0];
        }
        return renderTimeUs;
    }

    public byte[] getChunk() {
        return chunk;
    }

    private long render(long positionUs) {
        boolean decoded = false;
        while (!decoded && !ended && presentationTimeUs <= positionUs) {
            int inputIndex = audioDecoder.dequeueInputBuffer(1000);
            if (inputIndex >= 0) {
                ByteBuffer buffer = getInputBuffer(audioDecoder, inputIndex);
                buffer.clear();
                audioExtractor.selectTrack(audioTrack);
                int sampleSize = audioExtractor.readSampleData(buffer, 0);
                if (sampleSize < 0) {
                    audioDecoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    sampleTimeUs = audioExtractor.getSampleTime();
                    sampleFlags = audioExtractor.getSampleFlags();
                    audioDecoder.queueInputBuffer(inputIndex, 0, sampleSize, sampleTimeUs, sampleFlags);
                    if (!audioExtractor.advance()) {
                        ended = true;
                    }
                }
            } else {
                Log.w("AudioSource", "doRender: cannot dequeue input buffer from decoder");
                continue;
            }

            int outputIndex = audioDecoder.dequeueOutputBuffer(audioInfo, 1000);
            renderOutputStatus = outputIndex;
            switch (outputIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    audioFormat = audioDecoder.getOutputFormat();
                    sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    break;
                default:
                    this.presentationTimeUs = audioInfo.presentationTimeUs;
                    ByteBuffer buffer = getOutputBuffer(audioDecoder, outputIndex);
                    final byte[] chunk = new byte[audioInfo.size];
                    this.chunk = chunk;
                    buffer.get(chunk);
                    buffer.clear();
                    audioDecoder.releaseOutputBuffer(outputIndex, false);
                    decoded = presentationTimeUs >= positionUs;
                    Log.d("AudioSource", "doRender: rendered a buffer " + this.presentationTimeUs + ", " + positionUs);
                    break;
            }
        }
        return decoded ? presentationTimeUs + TimeUtil.msToUs(startTimeMs) : positionUs;
    }

    private void prepareExtractor() throws IOException {
        if (audioExtractor == null) {
            audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(filePath);
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                MediaFormat format = audioExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioTrack = i;
                    audioFormat = format;
                    break;
                }
            }
            if (audioTrack == -1) {
                throw new UnsupportedFormatException("Cannot find playable audio track.");
            }
            audioExtractor.selectTrack(audioTrack);
            audioMime = audioFormat.getString(MediaFormat.KEY_MIME);
            durationMs = audioFormat.containsKey(MediaFormat.KEY_DURATION) ? TimeUtil.usToMs(audioFormat.getLong(MediaFormat.KEY_DURATION)) :
                    MediaUtil.getDuration(filePath);
            sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        }
    }

    private void prepareDecoder() throws IOException {
        if (audioDecoder == null && !TextUtils.isEmpty(audioMime)) {
            audioDecoder = MediaCodec.createDecoderByType(audioMime);
            audioDecoder.configure(audioFormat, null, null, 0);
            audioDecoder.start();
        }
    }

    private ByteBuffer getInputBuffer(MediaCodec decoder, int inputIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return decoder.getInputBuffer(inputIndex);
        } else {
            return decoder.getInputBuffers()[inputIndex];
        }
    }

    private ByteBuffer getOutputBuffer(MediaCodec decoder, int outputIndex) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return decoder.getOutputBuffer(outputIndex);
        } else {
            return decoder.getOutputBuffers()[outputIndex];
        }
    }

    public int getRenderOutputStatus() {
        return renderOutputStatus;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public MediaCodec.BufferInfo getAudioInfo() {
        return audioInfo;
    }

    public int getChannelCount() {
        return channelCount;
    }
}
