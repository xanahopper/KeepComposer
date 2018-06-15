package com.gotokeep.keep.composer.demo;

import android.media.MediaExtractor;
import android.support.test.runner.AndroidJUnit4;

import com.gotokeep.keep.composer.demo.source.SourceProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-15 20:05
 */
@RunWith(AndroidJUnit4.class)
public class MediaExtractorTest {
    @Test
    public void mediaExtractorInstanceLimitationTest() {
        List<MediaExtractor> mediaExtractors = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            try {
                MediaExtractor extractor = new MediaExtractor();
                extractor.setDataSource(SourceProvider.VIDEO_SRC[0]);
                mediaExtractors.add(extractor);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println(mediaExtractors.size());
        for (MediaExtractor extractor : mediaExtractors) {
            extractor.release();
        }
    }
}
