package com.gotokeep.keep.social.composer.decode;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.gotokeep.keep.social.composer.gles.RenderTexture;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-01 17:21
 */
public final class DecodeThread extends Thread implements RequestTarget {
    private String sourcePath;
    private String name;

    private MediaExtractor extractor;
    private MediaFormat format;
    private MediaCodec decoder;
    private MediaCodec.BufferInfo decodeInfo;
    private RenderTexture decodeTexture;
    private RenderTexture renderTexture;
    private int trackIndex;
    private String mimeType;
    private boolean ended = false;
    private long presentationTimeUs;

    private final Semaphore requestSem = new Semaphore(1);
    private final Object requestSyncObj = new Object();
    private final AtomicBoolean requestUpdated = new AtomicBoolean(true);
    private ComposerRequest decodeRequest;

    private boolean profilerMode = false;
    private boolean debugMode = false;
    private long frameCount = 0;

    @Override
    public void sendRequest(ComposerRequest request) {
        synchronized (requestSyncObj) {

        }
    }
}
