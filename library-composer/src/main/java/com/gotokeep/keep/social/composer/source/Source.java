package com.gotokeep.keep.social.composer.source;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * 用于提供给解码器的数据源
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:18
 */
public interface Source {
    String MIME_VIDEO = "video/";
    String MIME_AUDIO = "audio/";

    void setDataSource(String filePath, @NonNull String mimeStartWith);

    boolean isAvailable();

    boolean isEnded();

    void setLoop(boolean loop);

    long seekTo(long timeUs);

    long findClosestKeyTime(long presentationTimeMs);

    SampleDataInfo readSampleData(ByteBuffer buffer);

    void release();
}
