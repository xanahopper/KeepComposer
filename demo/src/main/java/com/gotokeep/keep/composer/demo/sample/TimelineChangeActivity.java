package com.gotokeep.keep.composer.demo.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gotokeep.keep.composer.demo.R;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.social.composer.filter.FilterFactory;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.ClippingTimeline;
import com.gotokeep.keep.social.composer.timeline.SourceTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.timeline.Track;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.item.FilterItem;
import com.gotokeep.keep.social.composer.timeline.item.ImageItem;
import com.gotokeep.keep.social.composer.timeline.item.LayerItem;
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
public class TimelineChangeActivity extends SampleActivity implements Handler.Callback, OverlayProvider,
        MediaComposer.PlayEventListener {
    private MediaComposer composer;
    private Timeline timeline;
    private boolean gotoNext = false;
    private static final int EXPORT_WIDTH = 960;
    private static final int EXPORT_HEIGHT = 540;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewView.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT, 0);
        FilterFactory.registerExternalFilterFactory(DemoFilterFactory.getInstance());
        if (getIntent().hasExtra("gotoNext")) {
            Log.d("Composer", "onCreate: " + getIntent().getStringExtra("gotoNext"));
            gotoNext = true;
        }

        composer = MediaComposerFactory.createMediaComposer(this, this);
        composer.setPreview(previewView);
        composer.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT);
        composer.setPlayEventListener(this);

        timeline = new SourceTimeline();
        VideoItem item1 = new VideoItem(SourceProvider.VIDEO_SRC[0]);
        item1.setStartTimeMs(TimeUtil.secToMs(0));
        item1.setEndTimeMs(TimeUtil.secToMs(3));
        VideoItem item2 = new VideoItem(SourceProvider.VIDEO_SRC[1]);
        item2.setStartTimeMs(TimeUtil.secToMs(3));
        item2.setEndTimeMs(TimeUtil.secToMs(6));
        item2.setPlaySpeed(2f);
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(item1);
        videoTrack.addMediaItem(item2);
        Track transitionTrack = new Track(true, 1);
        TransitionItem transitionItem1 = new TransitionItem(item1, item2, 2000, 1);
        transitionTrack.addMediaItem(transitionItem1);
        Track overlayTrack = new Track(true, 3);
        LayerItem layerItem = new LayerItem(2, "");
        layerItem.setTimeRangeMs(TimeUtil.secToMs(0), TimeUtil.secToMs(4));
        layerItem.setScale(0.5f);
        layerItem.setRotation(45);
        TextItem textItem = new TextItem(2, "我的运动时刻");
        textItem.setTimeRangeMs(0, TimeUtil.secToMs(4));
        textItem.setPosition("center");
        textItem.setTextSize(64);
        textItem.setTextColor(Color.WHITE);
        textItem.setShadowColor(0x7F444444);
        overlayTrack.addMediaItem(layerItem);
        overlayTrack.addMediaItem(textItem);

        FilterItem filterItem = new FilterItem("sunset", null);
        filterItem.setTimeRangeMs(0, TimeUtil.secToMs(3));
        Track filterTrack = new Track(true, 2);
        filterTrack.addMediaItem(filterItem);
        timeline.addMediaTrack(videoTrack);
        timeline.addMediaTrack(transitionTrack);
        timeline.addMediaTrack(filterTrack);
        timeline.addMediaTrack(overlayTrack);
        timeline.setAudioItem(new AudioItem(SourceProvider.AUDIO_SRC[0]));

//        timeline = new ClippingTimeline(timeline, 2000, timeline.getEndTimeMs());
        composer.setTimeline(timeline);
        composer.setRepeatMode(MediaComposer.REPEAT_LOOP_INFINITE);
        composer.prepare();
        composer.play();
    }

    private void startTimeline() {

        Timeline timeline = new SourceTimeline();
        VideoItem item1 = new VideoItem(SourceProvider.VIDEO_SRC[1]);
        item1.setStartTimeMs(TimeUtil.secToMs(0));
        item1.setEndTimeMs(TimeUtil.secToMs(3));
        VideoItem item2 = new VideoItem(SourceProvider.VIDEO_SRC[2]);
        item2.setStartTimeMs(TimeUtil.secToMs(3));
        item2.setEndTimeMs(TimeUtil.secToMs(6));
        item2.setPlaySpeed(2f);
        Track videoTrack = new Track(true, 0);
        videoTrack.addMediaItem(item1);
        videoTrack.addMediaItem(item2);
        Track transitionTrack = new Track(true, 1);
        TransitionItem transitionItem1 = new TransitionItem(item1, item2, 2000, 1);
        transitionTrack.addMediaItem(transitionItem1);
        Track overlayTrack = new Track(true, 3);
        LayerItem layerItem = new LayerItem(2, "");
        layerItem.setTimeRangeMs(TimeUtil.secToMs(0), TimeUtil.secToMs(4));
        layerItem.setScale(0.5f);
        layerItem.setRotation(45);
        TextItem textItem = new TextItem(2, "再来一次");
        textItem.setTimeRangeMs(0, TimeUtil.secToMs(4));
        textItem.setPosition("center");
        textItem.setTextSize(64);
        textItem.setTextColor(Color.WHITE);
        textItem.setShadowColor(0x7F444444);
        overlayTrack.addMediaItem(layerItem);
        overlayTrack.addMediaItem(textItem);

        FilterItem filterItem = new FilterItem("blackcat", null);
        filterItem.setTimeRangeMs(0, TimeUtil.secToMs(3));
        Track filterTrack = new Track(true, 2);
        filterTrack.addMediaItem(filterItem);
        timeline.addMediaTrack(videoTrack);
        timeline.addMediaTrack(transitionTrack);
        timeline.addMediaTrack(filterTrack);
        timeline.addMediaTrack(overlayTrack);
        timeline.setAudioItem(new AudioItem(SourceProvider.AUDIO_SRC[0]));

        composer.stop();
        composer.setTimeline(timeline);
        composer.setRepeatMode(MediaComposer.REPEAT_LOOP_INFINITE);
        composer.prepare();
        composer.play();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_change, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start:
                startTimeline();
                return true;
            case R.id.action_stop:
                if (composer != null) {
                    composer.stop();
                }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public MediaOverlay createOverlay(String name) {
        return new LayerOverlay(SourceProvider.IMAGE_SRC[1]);
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
