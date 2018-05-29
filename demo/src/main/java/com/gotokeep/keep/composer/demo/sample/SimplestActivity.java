package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;

import com.gotokeep.keep.composer.MediaComposer;
import com.gotokeep.keep.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.Track;
import com.gotokeep.keep.composer.timeline.item.VideoItem;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class SimplestActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener, MediaComposer.PlayEventListener {
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
        if (msg.what == 3) {
            MediaComposer.PositionInfo positionInfo = (MediaComposer.PositionInfo) msg.obj;
            onPositionChange(composer, positionInfo.currentTimeUs, positionInfo.totalTimeUs);
        }
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
        startItem.setEndTimeMs(16000);
        startItem.setPlaySpeed(10f);
//        VideoItem endItem = new VideoItem(SourceProvider.VIDEO_SRC[1]);
//        endItem.setStartTimeMs(TimeUtil.secToMs(10));
//        endItem.setEndTimeMs(TimeUtil.secToMs(20));
//        TransitionItem transitionItem = new TransitionItem(startItem, endItem, 1);
//        transitionItem.setDurationMs(600);
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(startItem);
        timeline.addMediaTrack(videoTrack);

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

    @Override
    public void onPreparing(MediaComposer composer, RenderNode preparingNode) {

    }

    @Override
    public void onPlay(MediaComposer composer) {

    }

    @Override
    public void onPause(MediaComposer composer) {

    }

    @Override
    public void onPositionChange(MediaComposer composer, long presentationTimeUs, long totalTimeUs) {
        updateInfo(TimeUtil.usToString(presentationTimeUs));
    }

    @Override
    public void onStop(MediaComposer composer) {

    }

    @Override
    public void onSeeking(MediaComposer composer, boolean seekComplete, long seekTimeMs) {

    }

    @Override
    public void onError(MediaComposer composer, Exception exception) {

    }
}
