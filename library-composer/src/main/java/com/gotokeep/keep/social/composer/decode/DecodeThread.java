package com.gotokeep.keep.social.composer.decode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.gotokeep.keep.social.composer.gles.RenderTexture;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 17:21
 */
public final class DecodeThread implements RequestTarget, Handler.Callback {
    private static final int MSG_SET_REQUEST = 390;
    private static final int MSG_SET_TARGET_TEXTURE = 665;
    private static final int MSG_DO_DECODE_WORK = 10;

    private static final Map<String, List<Long>> keyFrameCacheMap = new TreeMap<>();
    private final HandlerThread internalThread;
    private Handler handler;

    private MediaExtractor extractor;
    private MediaFormat sourceFormat;
    private MediaFormat targetFormat;
    private MediaCodec decoder;
    private MediaCodec.BufferInfo decodeInfo;
    private RenderTexture decodeTexture;
    private int trackIndex;
    private String mimeType;
    private List<Long> keyFrames;
    private boolean decoderStarted = false;
    private boolean inputEndOfStream = false;
    private boolean outputEndOfStream = false;
    private long currentKeyFrame;
    private long presentationTimeUs;
    private long requestDecodeTimeUs;
    private boolean renderAllFrame = false;

    private String sourcePath;

    private boolean profilerMode = false;
    private boolean debugMode = false;
    private long frameCount = 0;

    public DecodeThread(String name) {
        internalThread = new HandlerThread(name);
        internalThread.start();
        synchronized (internalThread) {
            handler = new Handler(internalThread.getLooper(), this);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_TARGET_TEXTURE:
                setTargetTextureInternal((RenderTexture) msg.obj);
                break;
            case MSG_SET_REQUEST:
                applyRequestInternal((ComposerRequest) msg.obj);
            case MSG_DO_DECODE_WORK:
            default:
                if (presentationTimeUs <= requestDecodeTimeUs) {
                    doDecode();
                }
        }
        return false;
    }

    public void setTargetTexture(RenderTexture targetTexture) {
        handler.obtainMessage(MSG_SET_TARGET_TEXTURE, targetTexture).sendToTarget();
    }

    @Override
    public void sendRequest(@NonNull ComposerRequest request) {
        handler.obtainMessage(MSG_SET_REQUEST, request).sendToTarget();
    }

    private void setTargetTextureInternal(RenderTexture targetTexture) {
        if (decodeTexture != targetTexture) {
            decodeTexture = targetTexture;
            if (decoder != null) {
                decoder.stop();
                if (sourceFormat != null && targetTexture != null) {
                    configureDecoder();
                }
            }
        }
    }

    private void applyRequestInternal(ComposerRequest request) {
        if (request != null) {
            // 如果播放源改变，重新初始化 MediaExtractor 和 MediaCodec
            if (request.requestSourcePath != null && !request.requestSourcePath.equals(sourcePath)) {
                if (extractor == null) {
                    extractor = new MediaExtractor();
                }
                try {
                    extractor.setDataSource(request.requestSourcePath);
                } catch (IOException e) {
                    return;
                }
                MediaFormat format = null;
                String mime = null;
                int index;
                for (index = 0; index < extractor.getTrackCount(); index++) {
                    format = extractor.getTrackFormat(index);
                    mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("video/")) {
                        break;
                    }
                }
                if (index >= extractor.getTrackCount() || format == null) {
                    trackIndex = -1;
                    return;
                } else {
                    trackIndex = index;
                    extractor.selectTrack(index);
                    if (keyFrameCacheMap.containsKey(request.requestSourcePath)) {
                        keyFrames = keyFrameCacheMap.get(request.requestSourcePath);
                    } else {
                        keyFrames = getKeyFrames(extractor);
                        keyFrameCacheMap.put(request.requestSourcePath, keyFrames);
                    }
                    // 如果 mime 发生变化，decoder 重建
                    if (!mime.equals(mimeType)) {
                        createDecoder(mime, format);
                    } else if (format.equals(sourceFormat)) {
                        sourceFormat = format;
                        if (decodeTexture != null) {
                            configureDecoder();
                        }
                    }
                    frameCount = 0;
                }
            }

            requestDecodeTimeUs = request.requestDecodeTimeUs;
            presentationTimeUs = 0L;
            inputEndOfStream = false;
            outputEndOfStream = false;
            // Seek 至当前关键帧
            long targetKeyFrame = TimeUtil.findClosestKeyFrame(keyFrames, requestDecodeTimeUs);
            if (targetKeyFrame != currentKeyFrame) {
                extractor.seekTo(requestDecodeTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                currentKeyFrame = targetKeyFrame;
            }
            handler.sendEmptyMessage(MSG_DO_DECODE_WORK);
        }
    }

    private void createDecoder(String mime, MediaFormat format) {
        if (TextUtils.isEmpty(mime) || format == null) {
            throw new RuntimeException("Cannot create decoder from empty info");
        }
        if (this.decoder != null) {
            this.decoder.stop();
            this.decoder.release();
            decoderStarted = false;
        }
        this.mimeType = mime;
        this.sourceFormat = format;
        try {
            this.decoder = MediaCodec.createDecoderByType(mimeType);
        } catch (IOException e) {
            throw new RuntimeException("Create decoder failed.", e);
        }
        if (decodeTexture != null) {
            configureDecoder();
        }
    }

    private void configureDecoder() {
        if (decoder == null || sourceFormat == null || decodeTexture == null) {
            throw new IllegalStateException("Decoder or format or targetTexture is missing");
        }
        targetFormat = null;
        if (decoderStarted) {
            decoder.stop();
        }
        decoder.configure(sourceFormat, decodeTexture.getSurface(), null, 0);
        decoder.start();
        decoderStarted = true;
    }

    private long doDecode() {
        if (!checkExtractor() || !checkDecoder()) {
            return -1;
        }
        if (inputEndOfStream && outputEndOfStream) {
            // TODO: no more work to do
            return -1;
        }
        if (!inputEndOfStream) {
            int inputIndex = decoder.dequeueInputBuffer(0);
            if (inputIndex >= 0) {
                ByteBuffer buffer = MediaUtil.getInputBuffer(decoder, inputIndex);
                int bufferSize = extractor.readSampleData(buffer, 0);
                long sampleTime = extractor.getSampleTime();
                int sampleFlags = extractor.getSampleFlags();
                if (sampleTime >= 0) {
                    decoder.queueInputBuffer(inputIndex, 0, bufferSize, sampleTime, sampleFlags);
                } else {
                    decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                }
                if (!extractor.advance()) {
                    inputEndOfStream = true;
                    // TODO: notify callback input EOS
                }
            }
        }

        if (!outputEndOfStream) {
            int outputIndex = decoder.dequeueOutputBuffer(decodeInfo, 0);
            if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                targetFormat = decoder.getOutputFormat();
            } else if (outputIndex >= 0) {
                presentationTimeUs = decodeInfo.presentationTimeUs;
                boolean keyFrame = (decodeInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                if (keyFrame || presentationTimeUs >= requestDecodeTimeUs || renderAllFrame) {
                    synchronized (decodeTexture) {
                        decoder.releaseOutputBuffer(outputIndex, true);
                    }
                } else {
                    decoder.releaseOutputBuffer(outputIndex, false);
                }
                if ((decodeInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    outputEndOfStream = true;
                    // TODO: notify callback output EOS
                }
            }
        }
        return presentationTimeUs;
    }

    private boolean checkExtractor() {
        return extractor != null && trackIndex >= 0 && sourceFormat != null;
    }

    private boolean checkDecoder() {
        return decoder != null && sourceFormat != null && decodeTexture != null;
    }

    private void prepareContext() {

    }

    private void prepareExtractor(ComposerRequest request) {
        if (extractor == null) {
            extractor = new MediaExtractor();
        }

    }

    private void prepareDecoder() {

    }

    private void releaseDecoder() {

    }

    private void releaseExtractor() {

    }

    private void releaseContext() {

    }

    private List<Long> getKeyFrames(MediaExtractor extractor) {
        List<Long> keyFrames = new ArrayList<>();
        if (extractor != null) {
            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            long timeUs;
            do {
                timeUs = extractor.getSampleTime();
                int flags = extractor.getSampleFlags();
                if ((flags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0 && timeUs >= 0) {
                    keyFrames.add(timeUs);
                }
            } while (timeUs >= 0 && extractor.advance());
            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        }
        return keyFrames;
    }
}
