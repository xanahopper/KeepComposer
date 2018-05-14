package com.gotokeep.keep.composer;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.TextureView;

import com.gotokeep.keep.composer.target.MuxerRenderTarget;
import com.gotokeep.keep.composer.target.PreviewRenderTarget;
import com.gotokeep.keep.composer.timeline.MediaItem;
import com.gotokeep.keep.composer.timeline.RenderFactory;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 14:20
 */
class MediaComposerImpl implements MediaComposer, Handler.Callback, TextureView.SurfaceTextureListener {
    private static final int MSG_SETUP = 0;
    private static final int MSG_SET_TIMELINE = 1;
    private static final int MSG_SET_PREVIEW = 2;
    private static final int MSG_EXPORT = 3;
    private static final int MSG_PREPARE = 4;
    private static final int MSG_PLAY = 5;
    private static final int MSG_PAUSE = 6;
    private static final int MSG_STOP = 7;
    private static final int MSG_SEEK = 8;
    private static final int MSG_SET_PLAY_LISTENER = 9;
    private static final int MSG_SET_EXPORT_LISTENER = 10;
    private static final int MSG_DO_SOME_WORK = 11;

    private HandlerThread internalThread;
    private Handler handler;

    private Timeline timeline;
    private PlayEventListener playEventListener;
    private ExportEventListener exportEventListener;
    private RenderNode renderTarget;
    private RenderFactory renderFactory;
    private Map<MediaItem, RenderNode> renderNodeMap;
    private LinkedList<RenderNode> renderList;
    private ComposerEngine engine;

    private long currentTimeUs;
    private boolean playing = false;

    public MediaComposerImpl() {
        engine = new ComposerEngine();
        renderList = new LinkedList<>();
        renderNodeMap = new TreeMap<>();

        this.internalThread = new HandlerThread("MediaComposerImpl");
        this.internalThread.start();
        this.handler = new Handler(internalThread.getLooper(), this);
    }

    @Override
    public void setTimeline(Timeline timeline) {
        handler.obtainMessage(MSG_SET_TIMELINE, timeline).sendToTarget();
    }

    @Override
    public void setPreview(TextureView previewView) {
        handler.obtainMessage(MSG_SET_PREVIEW, previewView).sendToTarget();
    }

    @Override
    public void export(ExportConfiguration exportConfiguration) {
        handler.obtainMessage(MSG_EXPORT, exportConfiguration).sendToTarget();
    }

    @Override
    public void prepare() {
        handler.sendEmptyMessage(MSG_PREPARE);
    }

    @Override
    public void play() {
        handler.sendEmptyMessage(MSG_PLAY);
    }

    @Override
    public void pause() {
        handler.sendEmptyMessage(MSG_PAUSE);
    }

    @Override
    public void stop() {
        handler.sendEmptyMessage(MSG_STOP);
    }

    @Override
    public void seekTo(long timeMs) {
        handler.obtainMessage(MSG_SEEK, timeMs).sendToTarget();
    }

    @Override
    public void setPlayEventListener(PlayEventListener listener) {
        handler.obtainMessage(MSG_SET_PLAY_LISTENER, listener).sendToTarget();
    }

    @Override
    public void setExportEventListener(ExportEventListener listener) {
        handler.obtainMessage(MSG_SET_EXPORT_LISTENER, listener).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SET_TIMELINE:
                setTimelineInternal((Timeline) msg.obj);
                return true;
            case MSG_SET_PREVIEW:
                setPreviewInternal((TextureView) msg.obj);
                return true;
            case MSG_EXPORT:
                exportInternal((ExportConfiguration) msg.obj);
                return true;
            case MSG_PREPARE:
                prepareInternal();
                return true;
            case MSG_PLAY:
                playInternal();
                return true;
            case MSG_PAUSE:
                pauseInternal();
                return true;
            case MSG_STOP:
                stopInternal();
                return true;
            case MSG_SEEK:
                seekInternal((long) msg.obj);
                return true;
            case MSG_SET_PLAY_LISTENER:
                setPlayListenerInternal((PlayEventListener) msg.obj);
                return true;
            case MSG_SET_EXPORT_LISTENER:
                setExportListenerInternal((ExportEventListener) msg.obj);
                return true;
            case MSG_DO_SOME_WORK:
                doRenderWork();
                return true;
        }
        return false;
    }

    private void setTimelineInternal(Timeline timeline) {
        if (this.timeline != timeline) {
            this.timeline = timeline;
        }
    }

    private void setPreviewInternal(TextureView previewView) {
        releaseRenderTarget();
        previewView.setSurfaceTextureListener(this);
        if (previewView.isAvailable()) {
            renderTarget = new PreviewRenderTarget();
            engine.setup(previewView.getSurfaceTexture());
        }
    }

    private void exportInternal(ExportConfiguration exportConfiguration) {
        releaseRenderTarget();
        renderTarget = new MuxerRenderTarget(exportConfiguration);
        engine.setup(renderTarget.renderTexture.getSurfaceTexture());
    }

    private void prepareInternal() {
        generateRenderList(0);
    }

    private void playInternal() {
        scheduleNextWork(0);
    }

    private void pauseInternal() {
        playing = false;
    }

    private void stopInternal() {

    }

    private void seekInternal(long timeMs) {
        long timeUs = TimeUtil.msToUs(timeMs);
        generateRenderList(timeUs);
        scheduleNextWork(timeUs);
    }

    private void setPlayListenerInternal(PlayEventListener listener) {
        playEventListener = listener;
    }

    private void setExportListenerInternal(ExportEventListener listener) {
        exportEventListener = listener;
    }

    private void doRenderWork() {

        // do render list
        // remove complete node
        // generate render list of next frame
    }

    private void scheduleNextWork(long presentationTimeUs) {

    }

    private void generateRenderList(long presentationTimeUs) {
        List<MediaItem> items = timeline.queryPresentationTimeItems(presentationTimeUs);
        Collections.sort(items, MediaItem.getTypeComparator());
        renderList.clear();
        for (MediaItem item : items) {
            RenderNode renderNode;
            if (!renderNodeMap.containsKey(item)) {
                renderNode = renderFactory.createRenderNode(item);
                renderNodeMap.put(item, renderNode);
            } else {
                renderNode = renderNodeMap.get(item);
            }
            renderList.add(renderNode);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        renderTarget = new PreviewRenderTarget();
        if (engine != null) {
            engine.release();
            engine.setup(surface);
            engine.setViewport(width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (engine != null) {
            engine.setViewport(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        releaseRenderTarget();
        if (engine != null) {
            engine.release();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        releaseRenderTarget();
        renderTarget = new PreviewRenderTarget();
        if (engine != null) {
            engine.release();
            engine.setup(surface);
        }
    }

    private void releaseRenderTarget() {
        if (renderTarget != null) {
            renderTarget.release();
            renderTarget = null;
        }
    }
}
