package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import android.view.View;

import com.gotokeep.keep.composer.MediaComposer;
import com.gotokeep.keep.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.item.LayerItem;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.Track;
import com.gotokeep.keep.composer.timeline.item.VideoItem;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class StepOverlayActivity extends SampleActivity implements Handler.Callback, OverlayProvider,
        TextureView.SurfaceTextureListener, MediaComposer.PlayEventListener, View.OnClickListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;
    private long positionUs = 0;
    private long intervalUs = TimeUtil.BILLION_US / 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        previewView.setSurfaceTextureListener(this);

        previewView.setVideoSize(640, 360, 0);
        previewView.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (composer != null) {
            composer.stop();
            composer.release();
            composer = null;
        }
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
        return SourceProvider.IMAGE_SRC[1];
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        composer = MediaComposerFactory.createMediaComposer(this, handler);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);
        composer.setPlayEventListener(this);

        timeline = new Timeline();
        VideoItem item1 = new VideoItem(SourceProvider.VIDEO_SRC[0]);
        item1.setStartTimeMs(TimeUtil.secToMs(0));
        item1.setEndTimeMs(TimeUtil.secToMs(9));

        LayerItem layerItem1 = new LayerItem(1, "");
        layerItem1.setStartTimeMs(TimeUtil.secToMs(0));
        layerItem1.setEndTimeMs(TimeUtil.secToMs(5));
        layerItem1.setRotation(30f);
        layerItem1.setOffsetX(20);
        layerItem1.setOffsetY(30);
        layerItem1.setScale(0.5f);

        LayerItem layerItem2 = new LayerItem(1, "");
        layerItem2.setStartTimeMs(TimeUtil.secToMs(1));
        layerItem2.setEndTimeMs(TimeUtil.secToMs(6));
        layerItem2.setRotation(30f);
        layerItem2.setOffsetX(80);
        layerItem2.setOffsetY(30);
        layerItem2.setScale(0.5f);

        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(item1);
        Track overlayTrack = new Track(true, 1);
        overlayTrack.addMediaItem(layerItem1);
        overlayTrack.addMediaItem(layerItem2);
        timeline.addMediaTrack(videoTrack);
        timeline.addMediaTrack(overlayTrack);

        composer.setTimeline(timeline);
        composer.prepare();
        composer.setDebugMode(true);
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

    @Override
    public void onClick(View v) {
        if (composer != null) {
            composer.doDebugRender(positionUs);
            positionUs += intervalUs;
        }
    }
}
