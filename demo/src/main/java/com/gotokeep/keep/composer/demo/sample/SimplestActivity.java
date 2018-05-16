package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;

import com.gotokeep.keep.composer.MediaComposer;
import com.gotokeep.keep.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.MediaItem;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.TransitionItem;
import com.gotokeep.keep.composer.timeline.VideoItem;
import com.gotokeep.keep.composer.transition.FadeTransition;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class SimplestActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        previewView.setSurfaceTextureListener(this);

        previewView.setVideoSize(640, 360, 0);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    @Override
    public String getLayerImagePath(String name) {
        return null;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        composer = MediaComposerFactory.createMediaComposer(this, handler);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);

        timeline = new Timeline();
        VideoItem startItem = new VideoItem(SourceProvider.VIDEO_SRC[0]);
        startItem.setStartTimeMs(0);
        startItem.setEndTimeMs(TimeUtil.secToMs(10));
//        VideoItem endItem = new VideoItem(SourceProvider.VIDEO_SRC[1]);
//        endItem.setStartTimeMs(TimeUtil.secToMs(10));
//        endItem.setEndTimeMs(TimeUtil.secToMs(20));
//        TransitionItem transitionItem = new TransitionItem(startItem, endItem, 1);
//        transitionItem.setDurationMs(600);
        timeline.addMediaItem(startItem);

        composer.setTimeline(timeline);
        composer.prepare();
        composer.play();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
