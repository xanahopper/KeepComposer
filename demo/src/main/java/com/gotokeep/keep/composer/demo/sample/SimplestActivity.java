package com.gotokeep.keep.composer.demo.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.ClippingTimeline;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class SimplestActivity extends SampleActivity implements Handler.Callback, OverlayProvider {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);

        previewView.setVideoSize(640, 360, 0);

        composer = MediaComposerFactory.createMediaComposer(this, this);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);

        timeline = new SourceTimeline();
        VideoItem startItem = new VideoItem(SourceProvider.VIDEO_SRC[1]);
        startItem.setStartTimeMs(0);
        startItem.setEndTimeMs(3000);
//        startItem.setPlaySpeed(5f);
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(startItem);
        timeline.addMediaTrack(videoTrack);

        timeline = new ClippingTimeline(timeline, 1000, timeline.getEndTimeMs());
        composer.setTimeline(timeline);
        composer.setRepeatMode(MediaComposer.REPEAT_LOOP_INFINITE);
        composer.prepare();
        composer.play();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public String getLayerImagePath(String name) {
        return null;
    }
}
