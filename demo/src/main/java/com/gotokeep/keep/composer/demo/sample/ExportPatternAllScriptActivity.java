package com.gotokeep.keep.composer.demo.sample;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;

import com.gotokeep.keep.social.composer.ExportConfiguration;
import com.gotokeep.keep.social.composer.MediaComposer;
import com.gotokeep.keep.social.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.demo.R;
import com.gotokeep.keep.composer.demo.SampleActivity;
import com.gotokeep.keep.composer.demo.source.SourceProvider;
import com.gotokeep.keep.social.composer.filter.FilterFactory;
import com.gotokeep.keep.social.composer.overlay.LayerOverlay;
import com.gotokeep.keep.social.composer.overlay.MediaOverlay;
import com.gotokeep.keep.social.composer.overlay.OverlayProvider;
import com.gotokeep.keep.social.composer.timeline.ClippingTimeline;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.util.TimeUtil;
import com.gotokeep.keep.social.director.KeepDirector;
import com.gotokeep.keep.social.director.VideoFragment;
import com.gotokeep.keep.social.director.exception.UnsuitableException;
import com.seu.magicfilter.filter.DemoFilterFactory;
import com.seu.magicfilter.utils.OpenGlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-16 10:09
 */
public class ExportPatternAllScriptActivity extends SampleActivity implements Handler.Callback, OverlayProvider, TextureView.SurfaceTextureListener, MediaComposer.PlayEventListener, MediaComposer.ExportEventListener {
    private MediaComposer composer;
    private Handler handler;
    private Timeline timeline;
    private boolean gotoNext = false;
    private static final int EXPORT_WIDTH = 960;
    private static final int EXPORT_HEIGHT = 540;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        previewView.setSurfaceTextureListener(this);

        previewView.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT, 0);
        FilterFactory.registerExternalFilterFactory(DemoFilterFactory.getInstance());
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

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 2) {
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
        composer = MediaComposerFactory.createMediaComposer(this, this);
        composer.setPreview(previewView);
        composer.setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT);
        composer.setPlayEventListener(this);

        String scriptText = OpenGlUtils.readShaderFromRawResource(R.raw.pattern_all);
        List<VideoFragment> videoFragments = new ArrayList<>();
        for (int i = 0; i < SourceProvider.VIDEO_FRAGMENT.length; i++) {
            VideoFragment videoFragment = new VideoFragment(SourceProvider.VIDEO_FRAGMENT[i], i, null);
            videoFragments.add(videoFragment);
        }
        KeepDirector director = new KeepDirector(getApplicationContext(), scriptText);
        boolean verified = director.verifyScript();
        if (verified) {
            try {
                timeline = director.buildTimeline(videoFragments);
            } catch (UnsuitableException e) {
                Toast.makeText(this, "视频数量不符，不能使用此模板", Toast.LENGTH_SHORT).show();
                composer.release();
                return;
            }

            timeline = new ClippingTimeline(timeline, 2000, timeline.getEndTimeMs());
            composer.setTimeline(timeline);
            composer.setExportEventListener(this);
            ExportConfiguration configuration = ExportConfiguration.newBuilder()
                    .setVideoSize(EXPORT_WIDTH, EXPORT_HEIGHT)
                    .setFrameRate(25)
                    .setVideoBitRate(3000 * 1024)
                    .setAudioBitRate(128 * 1024)
                    .setKeyFrameInterval(1)
                    .setOutputFilePath(SourceProvider.OUTPUT_PATH[0])
                    .build();
            composer.export(configuration);
        } else {
            Toast.makeText(this, "脚本资源下载中，稍后再试", Toast.LENGTH_SHORT).show();
        }
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
    public void onExportStart(MediaComposer composer) {

    }

    @Override
    public void onExportProgress(MediaComposer composer, long presentationTimeUs, long totalTimeUs) {
        onPositionChange(composer, presentationTimeUs, totalTimeUs);
    }

    @Override
    public void onExportComplete(MediaComposer composer) {

    }

    @Override
    public void onExportError(MediaComposer composer, Exception exception) {

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
