package com.gotokeep.keep.composer.demo.sample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;

import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.ScaleType;
import com.gotokeep.keep.social.composer.filter.FilterFactory;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.ClippingTimeline;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.LayerItem;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.TextItem;
import com.gotokeep.keep.social.composer.timeline.item.TransitionItem;
import com.gotokeep.keep.social.composer.timeline.item.VideoItem;
import com.gotokeep.keep.social.composer.util.TimeUtil;
import com.seu.magicfilter.filter.DemoFilterFactory;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class SimpleTransitionActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener, MediaComposer.PlayEventListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;
    private boolean gotoNext = false;
    private static final int EXPORT_WIDTH = 960;
    private static final int EXPORT_HEIGHT = 960;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);

        previewView.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT, 0);
        FilterFactory.registerExternalFilterFactory(DemoFilterFactory.getInstance());
        if (getIntent().hasExtra("gotoNext")) {
            Log.d("Composer", "onCreate: " + getIntent().getStringExtra("gotoNext"));
            gotoNext = true;
        }
        startComposition();

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
    public MediaOverlay createOverlay(String name) {
        return new LayerOverlay(SourceProvider.IMAGE_SRC[1]);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    }

    private void startComposition() {
        composer = MediaComposerFactory.createMediaComposer(this, this);
        composer.setPreview(previewView);
        composer.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT);
        composer.setPlayEventListener(this);

        timeline = new SourceTimeline();
        if (!gotoNext) {
            ImageItem item1 = new ImageItem(SourceProvider.IMAGE_SRC[0]);
            item1.setScaleType(ScaleType.FIT_CENTER);
            item1.setStartTimeMs(TimeUtil.secToMs(0));
            item1.setEndTimeMs(TimeUtil.secToMs(3));
            VideoItem item2 = new VideoItem(SourceProvider.VIDEO_SRC[1]);
            item2.setScaleType(ScaleType.FIT_CENTER);
            item2.setStartTimeMs(TimeUtil.secToMs(3));
            item2.setEndTimeMs(TimeUtil.secToMs(6));
            item2.setPlaySpeed(2f);
            VideoItem item3 = new VideoItem(SourceProvider.VIDEO_SRC[0]);
            item3.setScaleType(ScaleType.FIT_CENTER);
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
            Track overlayTrack = new Track(true, 3);
            LayerItem layerItem = new LayerItem(2, "");
            layerItem.setTimeRangeMs(TimeUtil.secToMs(0), TimeUtil.secToMs(4));
            layerItem.setScale(0.5f);
            layerItem.setRotation(45);
            LayerItem layerItem1 = new LayerItem(2, "");
            layerItem1.setTimeRangeMs(TimeUtil.secToMs(3), TimeUtil.secToMs(8));
            layerItem1.setScale(0.25f);
            layerItem1.setOffsetX(250);
            layerItem1.setOffsetY(150);
            TextItem textItem = new TextItem(2, "我的运动时刻");
            textItem.setTimeRangeMs(0, TimeUtil.secToMs(6));
            textItem.setPosition("center");
            textItem.setTextSize(64);
            textItem.setTextColor(Color.WHITE);
            textItem.setShadowColor(0x7F444444);
            overlayTrack.addMediaItem(layerItem);
            overlayTrack.addMediaItem(layerItem1);
            overlayTrack.addMediaItem(textItem);

            FilterItem filterItem = new FilterItem("sunset", null);
            filterItem.setTimeRangeMs(0, TimeUtil.secToMs(8));
            Track filterTrack = new Track(true, 2);
            filterTrack.addMediaItem(filterItem);
            timeline.addMediaTrack(videoTrack);
            timeline.addMediaTrack(transitionTrack);
            timeline.addMediaTrack(filterTrack);
            timeline.addMediaTrack(overlayTrack);
            timeline.setAudioItem(new AudioItem(SourceProvider.AUDIO_SRC[0]));
        }

//        timeline = new ClippingTimeline(timeline, 2000, timeline.getEndTimeMs());
        composer.setTimeline(timeline);
        composer.setRepeatMode(MediaComposer.REPEAT_LOOP_INFINITE);
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
    public void onPreparing(MediaComposer composer) {

    }

    @Override
    public void onReady(MediaComposer composer) {

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
