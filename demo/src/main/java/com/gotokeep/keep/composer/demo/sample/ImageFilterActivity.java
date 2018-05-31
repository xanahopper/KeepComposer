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
import com.gotokeep.keep.composer.filter.FilterFactory;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.item.AudioItem;
import com.gotokeep.keep.composer.timeline.item.FilterItem;
import com.gotokeep.keep.composer.timeline.item.ImageItem;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.Track;
import com.gotokeep.keep.composer.util.TimeUtil;
import com.seu.magicfilter.filter.DemoFilterFactory;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class ImageFilterActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        previewView.setSurfaceTextureListener(this);

        previewView.setVideoSize(640, 360, 0);
        FilterFactory.registerExternalFilterFactory(DemoFilterFactory.getInstance());
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
        composer = MediaComposerFactory.createMediaComposer(this, this, handler);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);

        timeline = new Timeline();
        ImageItem startItem = new ImageItem(SourceProvider.IMAGE_SRC[0]);
        startItem.setStartTimeMs(0);
        startItem.setEndTimeMs(TimeUtil.secToMs(10));
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(startItem);
        FilterItem filterItem = new FilterItem("blackcat", null);
        filterItem.setTimeRangeMs(0, TimeUtil.secToMs(10));
        Track filterTrack = new Track(true, 1);
        filterTrack.addMediaItem(filterItem);
        timeline.addMediaTrack(videoTrack);
        timeline.addMediaTrack(filterTrack);
        timeline.setAudioItem(new AudioItem(SourceProvider.AUDIO_SRC[0]));

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
