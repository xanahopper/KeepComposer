package com.gotokeep.keep.social.composer.source;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:37
 */
public abstract class MediaSource implements Source {
    protected MediaExtractor extractor;
    protected MediaFormat format;
    protected int trackIndex = -1;
    protected String mimeType = null;
    protected boolean available = false;

    protected boolean ended = false;
    protected boolean loop = false;

    public MediaSource() {
        extractor = new MediaExtractor();
    }

    @Override
    public void setDataSource(String filePath, @NonNull String mimeStartWith) {
        try {
            extractor.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        reset();
        prepareExtractor(mimeStartWith);
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    private void reset() {
        format = null;
        trackIndex = -1;
        mimeType = null;
        available = false;
        loop = false;
        ended = false;
    }

    private void prepareExtractor(String mimeStartWith) {
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat trackFormat = extractor.getTrackFormat(i);
            String mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (mime != null && mime.startsWith(mimeStartWith)) {
                trackIndex = i;
                format = trackFormat;
                mimeType = mime;
                break;
            }
        }
        if (trackIndex > 0) {
            available = true;
            extractor.selectTrack(trackIndex);
        } else {
            available = false;
        }
    }

    @Override
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    @Override
    public long seekTo(long timeUs) {
        extractor.seekTo(timeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        return extractor.getSampleTime();
    }

    @Override
    public SampleDataInfo readSampleData(ByteBuffer buffer) {
        SampleDataInfo info = SampleDataInfo.obtain();
        if (!ended) {
            info.bufferSize = extractor.readSampleData(buffer, 0);
            info.flags = extractor.getSampleFlags();
            info.presentationTimeUs = extractor.getSampleTime();
            ended = !extractor.advance();
            if (ended && loop) {
                seekTo(0);
                ended = false;
            }
        }
        info.ended = ended;
        return info;
    }

    @Override
    public void release() {
        if (extractor != null) {
            reset();
            extractor.release();
            extractor = null;
        }
    }
}
