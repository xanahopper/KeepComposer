package com.gotokeep.keep.composer.demo.sample;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class ExtractorActivity extends SampleActivity  {

    private static final String TAG = "ExtractorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MediaExtractor extractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        SparseArray<ByteBuffer> bufferMap = new SparseArray<>();
        try {
            extractor.setDataSource(SourceProvider.VIDEO_SRC[0]);
            audioExtractor.setDataSource(SourceProvider.VIDEO_SRC[0]);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    int size = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferMap.put(i, ByteBuffer.allocate(size));
                    break;
                }
            }
            for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    audioExtractor.selectTrack(i);
                    int size = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    bufferMap.put(i, ByteBuffer.allocate(size));
                }
            }

            for (int i = 0; i < 100; i++) {
                int buffSize;
//                do {
                int sampleIndex = extractor.getSampleTrackIndex();
                Log.d(TAG, "sampleIndex: " + sampleIndex);
                ByteBuffer buffer = bufferMap.get(sampleIndex);
                buffSize = extractor.readSampleData(buffer, 0);
                long time = extractor.getSampleTime();
//                    int flags = extractor.getSampleFlags();
                Log.d(TAG, "readSampleData: " + buffSize + ", " + time);
//                } while (buffSize > 0);
                extractor.advance();
            }

            for (int i = 0; i < 100; i++) {
                int buffSize;
//                do {
                int sampleIndex = audioExtractor.getSampleTrackIndex();
                Log.d(TAG, "Audio sampleIndex: " + sampleIndex);
                ByteBuffer buffer = bufferMap.get(sampleIndex);
                buffSize = audioExtractor.readSampleData(buffer, 0);
                long time = audioExtractor.getSampleTime();
//                    int flags = extractor.getSampleFlags();
                Log.d(TAG, "readSampleData: " + buffSize + ", " + time);
//                } while (buffSize > 0);
                audioExtractor.advance();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
