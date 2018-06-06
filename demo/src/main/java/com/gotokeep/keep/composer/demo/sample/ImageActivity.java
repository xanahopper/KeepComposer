package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;

import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class ImageActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener {
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
    public MediaOverlay createOverlay(String name) {
        return new LayerOverlay(SourceProvider.IMAGE_SRC[1]);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        composer = MediaComposerFactory.createMediaComposer(this, this);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);

        timeline = new SourceTimeline();
        ImageItem startItem = new ImageItem(SourceProvider.IMAGE_SRC[0]);
        startItem.setStartTimeMs(0);
        startItem.setEndTimeMs(TimeUtil.secToMs(10));
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(startItem);
        timeline.addMediaTrack(videoTrack);
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
