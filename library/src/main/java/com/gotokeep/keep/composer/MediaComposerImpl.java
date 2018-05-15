package com.gotokeep.keep.composer;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.view.TextureView;

import com.gotokeep.keep.composer.target.MuxerRenderTarget;
import com.gotokeep.keep.composer.target.PreviewRenderTarget;
import com.gotokeep.keep.composer.timeline.MediaItem;
import com.gotokeep.keep.composer.timeline.RenderFactory;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.util.MediaClock;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
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
    private static final int MSG_SET_VIDEO_SIZE = 12;

    private static final int EVENT_PLAY_PREPARE = 0;
    private static final int EVENT_PLAY_PLAY = 1;
    private static final int EVENT_PLAY_PAUSE = 2;
    private static final int EVENT_PLAY_POSITION_CHANGE = 3;
    private static final int EVENT_PLAY_STOP = 4;
    private static final int EVENT_PLAY_SEEKING = 5;
    private static final int EVENT_PLAY_ERROR = 6;

    private static final int EVENT_EXPORT_PREPARE = 0;
    private static final int EVENT_EXPORT_START = 1;
    private static final int EVENT_EXPORT_PROGRESS = 2;
    private static final int EVENT_EXPORT_COMPLETE = 3;
    private static final int EVENT_EXPORT_ERROR = 4;

    private HandlerThread internalThread;
    private Handler handler;
    private Handler eventHandler;

    private Timeline timeline;
    private PlayEventListener playEventListener;
    private ExportEventListener exportEventListener;
    private RenderTarget renderTarget;
    private RenderNode renderRoot;
    private RenderFactory renderFactory;
    private Map<MediaItem, RenderNode> renderNodeMap;
    private ComposerEngine engine;
    private MediaClock mediaClock;

    private long currentTimeUs;
    private int videoWidth;
    private int videoHeight;
    private boolean export = false;
    private boolean playing = false;

    public MediaComposerImpl(RenderFactory renderFactory, Handler eventHandler) {
        engine = new ComposerEngine();
        renderNodeMap = new TreeMap<>();
        mediaClock = new MediaClock();

        this.renderFactory = renderFactory;
        this.internalThread = new HandlerThread("MediaComposerImpl");
        this.internalThread.start();
        this.handler = new Handler(internalThread.getLooper(), this);
        this.eventHandler = eventHandler;
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
    public void setVideoSize(int width, int height) {
        handler.obtainMessage(MSG_SET_VIDEO_SIZE, width, height).sendToTarget();
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
            case MSG_SET_VIDEO_SIZE:
                setVideoSizeInternal(msg.arg1, msg.arg2);
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
        export = true;
        renderTarget = new MuxerRenderTarget(exportConfiguration);
        engine.setup(renderTarget.getInputSurface());
        handler.sendEmptyMessage(MSG_PLAY);
    }

    private void prepareInternal() {
        renderTarget.prepare();
        renderRoot = generateRenderTree(currentTimeUs);
        if (eventHandler != null) {
            eventHandler.sendEmptyMessage(export ? EVENT_EXPORT_PREPARE : EVENT_PLAY_PREPARE);
        }
    }

    private void playInternal() {
        playing = true;
        mediaClock.start();
        scheduleNextWork(currentTimeUs);
        if (eventHandler != null) {
            eventHandler.sendEmptyMessage(export ? EVENT_EXPORT_START : EVENT_PLAY_PLAY);
        }
    }

    private void pauseInternal() {
        mediaClock.stop();
        playing = false;
        if (eventHandler != null && !export) {
            eventHandler.sendEmptyMessage(EVENT_PLAY_PAUSE);
        }
    }

    private void stopInternal() {
        mediaClock.stop();
        handler.removeMessages(MSG_DO_SOME_WORK);
        if (eventHandler != null) {
            eventHandler.sendEmptyMessage(export ? EVENT_EXPORT_COMPLETE : EVENT_PLAY_STOP);
        }
    }

    private void seekInternal(long timeMs) {
        renderRoot = generateRenderTree(TimeUtil.msToUs(timeMs));
        scheduleNextWork(timeMs);
        if (eventHandler != null && !export) {
            eventHandler.obtainMessage(EVENT_PLAY_SEEKING, timeMs).sendToTarget();
        }
    }

    private void setVideoSizeInternal(int width, int height) {
        if (!playing) {
            videoWidth = width;
            videoHeight = height;
        }
    }

    private void setPlayListenerInternal(PlayEventListener listener) {
        playEventListener = listener;
    }

    private void setExportListenerInternal(ExportEventListener listener) {
        exportEventListener = listener;
    }

    private void doRenderWork() {
        mediaClock.setPositionUs(currentTimeUs);
        if (renderRoot == null) {
            if (eventHandler != null) {
                eventHandler.sendEmptyMessage(export ? EVENT_EXPORT_COMPLETE : EVENT_PLAY_STOP);
            }
            return;
        }
        // do render tree
        currentTimeUs = renderRoot.render(currentTimeUs);
        // render result to RenderTarget
        renderTarget.updateFrame(renderRoot, currentTimeUs);
        if (eventHandler != null) {
            eventHandler.obtainMessage(export ? EVENT_EXPORT_PROGRESS : EVENT_PLAY_POSITION_CHANGE,
                    new PositionInfo(currentTimeUs, timeline.getEndTimeMs())).sendToTarget();
        }
        // maintain render tree
        renderRoot = maintainRenderTree(renderRoot, currentTimeUs);
        if (renderRoot == null) {
            renderRoot = generateRenderTree(currentTimeUs);
        }
        if (renderRoot != null) {
            scheduleNextWork(TimeUtil.usToMs(currentTimeUs));
        } else {
            handler.removeMessages(MSG_DO_SOME_WORK);
        }
    }

    private void scheduleNextWork(long presentationTimeMs) {
        handler.removeMessages(MSG_DO_SOME_WORK);
        long nextWorkStartTimeDelayMs = presentationTimeMs - mediaClock.getPositionUs();
        if (nextWorkStartTimeDelayMs <= 0) {
            handler.sendEmptyMessage(MSG_DO_SOME_WORK);
        } else {
            handler.sendEmptyMessageDelayed(MSG_DO_SOME_WORK, nextWorkStartTimeDelayMs);
        }
    }

    private RenderNode generateRenderTree(long presentationTimeUs) {
        List<MediaItem> items = timeline.queryPresentationTimeItems(presentationTimeUs);
        Collections.sort(items, MediaItem.getTypeComparator());
        RenderNode root = renderRoot;
        MediaItem topItem = items.size() > 0 ? items.get(0) : null;
        for (MediaItem item : items) {
            RenderNode renderNode;
            if (!renderNodeMap.containsKey(item)) {
                renderNode = renderFactory.createRenderNode(item);
                renderNodeMap.put(item, renderNode);
            } else {
                renderNode = renderNodeMap.get(item);
            }
            if (topItem == null || topItem.getLayer() < item.getLayer()) {
                topItem = item;
                root = renderNode;
            }
        }
        return root;
    }

    private RenderNode maintainRenderTree(RenderNode root, long presentationTimeUs) {
        if (root != null) {
            for (int i = 0; i < root.inputNodes.size(); i++) {
                int key = root.inputNodes.keyAt(i);
                RenderNode node = root.inputNodes.valueAt(i);
                node = maintainRenderTree(node, presentationTimeUs);
                if (node != null) {
                    root.inputNodes.put(key, node);
                } else {
                    root.inputNodes.remove(key);
                }
            }
            if (!root.isInRange(TimeUtil.usToMs(presentationTimeUs)) ||
                    root.getMainInputNode(presentationTimeUs) == null) {
                root = root.getMainInputNode(presentationTimeUs);
            }
        }
        return root;
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
