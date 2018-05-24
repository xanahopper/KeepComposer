package com.gotokeep.keep.composer.demo.sample;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;

import com.gotokeep.keep.composer.ExportConfiguration;
import com.gotokeep.keep.composer.MediaComposer;
import com.gotokeep.keep.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.composer.overlay.OverlayProvider;
import com.gotokeep.keep.composer.timeline.ImageItem;
import com.gotokeep.keep.composer.timeline.LayerItem;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.Track;
import com.gotokeep.keep.composer.timeline.TransitionItem;
import com.gotokeep.keep.composer.timeline.VideoItem;
import com.gotokeep.keep.composer.util.TimeUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class TransitionExportActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener, MediaComposer.PlayEventListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;
    private boolean gotoNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        previewView.setSurfaceTextureListener(this);

        previewView.setVideoSize(640, 360, 0);
        if (getIntent().hasExtra("gotoNext")) {
            Log.d("Composer", "onCreate: " + getIntent().getStringExtra("gotoNext"));
            gotoNext = true;
        }
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

    private void startExport() {
        composer = MediaComposerFactory.createMediaComposer(this, handler);
        composer.setPreview(previewView);
        composer.setVideoSize(640, 360);
        composer.setPlayEventListener(this);

        timeline = new Timeline();
        if (!gotoNext) {
            ImageItem item1 = new ImageItem(SourceProvider.IMAGE_SRC[0]);
            item1.setStartTimeMs(TimeUtil.secToMs(0));
            item1.setEndTimeMs(TimeUtil.secToMs(3));
            VideoItem item2 = new VideoItem(SourceProvider.VIDEO_SRC[1]);
            item2.setStartTimeMs(TimeUtil.secToMs(3));
            item2.setEndTimeMs(TimeUtil.secToMs(6));
            item2.setPlaySpeed(2f);
            VideoItem item3 = new VideoItem(SourceProvider.VIDEO_SRC[0]);
            item3.setStartTimeMs(TimeUtil.secToMs(6));
            item3.setEndTimeMs(TimeUtil.secToMs(9));
            Track videoTrack = new Track(true, 0);
            videoTrack.addMediaItem(item1);
            videoTrack.addMediaItem(item2);
            videoTrack.addMediaItem(item3);
            Track transitionTrack = new Track(true, 1);
            TransitionItem transitionItem1 = new TransitionItem(item1, item2, 2000, 1);
            TransitionItem transitionItem2 = new TransitionItem(item2, item3, 2000, 1);
            transitionTrack.addMediaItem(transitionItem1);
            transitionTrack.addMediaItem(transitionItem2);
            Track overlayTrack = new Track(true, 2);
            LayerItem layerItem = new LayerItem(2, "");
            layerItem.setTimeRangeMs(TimeUtil.secToMs(0), TimeUtil.secToMs(4));
            layerItem.setScale(0.5f);
            layerItem.setRotation(45);
            LayerItem layerItem1 = new LayerItem(2, "");
            layerItem1.setTimeRangeMs(TimeUtil.secToMs(3), TimeUtil.secToMs(8));
            layerItem1.setScale(0.25f);
            layerItem1.setOffsetX(250);
            layerItem1.setOffsetY(150);
            overlayTrack.addMediaItem(layerItem);
            overlayTrack.addMediaItem(layerItem1);
            timeline.addMediaTrack(videoTrack);
            timeline.addMediaTrack(transitionTrack);
//            timeline.addMediaTrack(overlayTrack);
        } else {
            VideoItem item1 = new VideoItem(SourceProvider.VIDEO_SRC[1]);
            item1.setStartTimeMs(0);
            item1.setEndTimeMs(200);
            ImageItem item2 = new ImageItem(SourceProvider.IMAGE_SRC[0]);
            item2.setStartTimeMs(200);
            item2.setEndTimeMs(400);
            item2.setPlaySpeed(2f);
            Track videoTrack = new Track(true, 0);
            videoTrack.addMediaItem(item1);
            videoTrack.addMediaItem(item2);
            Track transitionTrack = new Track(true, 1);
            TransitionItem transitionItem1 = new TransitionItem(item1, item2, 200, 1);
            transitionTrack.addMediaItem(transitionItem1);
            timeline.addMediaTrack(videoTrack);
            timeline.addMediaTrack(transitionTrack);
            previewView.postDelayed(() -> {
                if (gotoNext) {
                    Intent intent = new Intent(this, SimpleOverlayActivity.class);
                    this.startActivity(intent);
                    finish();
                }
            }, 200);
        }

        composer.setTimeline(timeline);
        composer.prepare();
        ExportConfiguration configuration = ExportConfiguration.newBuilder()
                .setWidth(640)
                .setHeight(360)
                .setFrameRate(25)
                .setBitRate(3000)
                .setKeyFrameInterval(10)
                .build();
        composer.export(configuration);
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
        Log.d("Composer", "onStop");
    }

    @Override
    public void onSeeking(MediaComposer composer, boolean seekComplete, long seekTimeMs) {

    }

    @Override
    public void onError(MediaComposer composer, Exception exception) {

    }
}
