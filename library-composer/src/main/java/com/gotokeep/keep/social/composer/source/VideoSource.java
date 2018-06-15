package com.gotokeep.keep.social.composer.source;

import android.media.MediaExtractor;
import android.support.annotation.NonNull;

import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:54
 */
public class VideoSource extends MediaSource {
    private List<Long> keyFrames = new ArrayList<>();

    @Override
    public void setDataSource(String filePath, @NonNull String mimeStartWith) {
        super.setDataSource(filePath, mimeStartWith);
        prepareKeyFrameInfo();
    }

    private void prepareKeyFrameInfo() {
        if (available) {
            do {
                long timeUs = extractor.getSampleTime();
                if ((extractor.getSampleFlags() & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                    keyFrames.add(timeUs);
                }
            } while (extractor.advance());
            extractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        }
    }

    @Override
    public long findClosestKeyTime(long presentationTimeMs) {
        return TimeUtil.findClosestKeyFrame(keyFrames, presentationTimeMs);
    }
}
