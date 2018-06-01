package com.gotokeep.keep.social.composer;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.TextureView;

import com.gotokeep.keep.social.composer.source.AudioSource;
import com.gotokeep.keep.social.composer.target.MuxerRenderTarget;
import com.gotokeep.keep.social.composer.target.PreviewRenderTarget;
import com.gotokeep.keep.social.composer.timeline.item.AudioItem;
import com.gotokeep.keep.social.composer.timeline.MediaItem;
import com.gotokeep.keep.social.composer.timeline.RenderFactory;
import com.gotokeep.keep.social.composer.timeline.Timeline;
import com.gotokeep.keep.social.composer.util.MediaClock;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final int MSG_PREPARE_ENGINE = 13;
    private static final int MSG_RELEASE = 14;
    private static final int MSG_DEBUG_MODE = 15;
    private static final int MSG_DEBUG_DO_RENDER = 16;
    private static final int MSG_DO_AUDIO_WORK = 17;
    private static final int MSG_RESET = 18;

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
    private static final String TAG = MediaComposer.class.getSimpleName();

    private HandlerThread videoThread;
    private HandlerThread audioThread;
    private Handler videoHandler;
    private Handler audioHandler;
    private EventDispatcher eventDispatcher;

    private Timeline timeline;
    private PlayEventListener playEventListener;
    private ExportEventListener exportEventListener;
    private RenderTarget renderTarget;
    private RenderNode renderRoot;
    private AudioSource audioSource;
    private RenderFactory renderFactory;
    private Map<MediaItem, RenderNode> renderNodeMap;
    private ComposerEngine engine;
    private MediaClock mediaClock;

    private long videoTimeUs;
    private long audioTimeUs;
    private int videoWidth;
    private int videoHeight;
    private int canvasWidth;
    private int canvasHeight;
    private boolean export = false;
    private AtomicBoolean playing = new AtomicBoolean(false);
    private long elapsedRealtimeUs;
    private long exportTimeUs = 0;
    private long frameIntervalUs = 0;

    private boolean debugMode = false;
    private long debugPositionUs = 0;

    public MediaComposerImpl(RenderFactory renderFactory, Handler eventHandler) {
        engine = new ComposerEngine();
        renderNodeMap = new HashMap<>();
        mediaClock = new MediaClock();

        this.renderFactory = renderFactory;

        this.videoThread = new HandlerThread("ComposerImpl:video", HandlerThread.MAX_PRIORITY);
        this.videoThread.start();
        this.videoHandler = new Handler(videoThread.getLooper(), this);

        this.audioThread = new HandlerThread("ComposerImpl:audio", HandlerThread.MAX_PRIORITY);
        this.audioThread.start();
        this.audioHandler = new Handler(audioThread.getLooper(), this);

        this.eventDispatcher = new EventDispatcher(eventHandler);
    }

    @Override
    public void setTimeline(Timeline timeline) {
        videoHandler.obtainMessage(MSG_SET_TIMELINE, timeline).sendToTarget();
    }

    @Override
    public void setPreview(TextureView previewView) {
        videoHandler.obtainMessage(MSG_SET_PREVIEW, previewView).sendToTarget();
    }

    @Override
    public void export(ExportConfiguration exportConfiguration) {
        videoHandler.obtainMessage(MSG_EXPORT, exportConfiguration).sendToTarget();
    }

    @Override
    public void prepare() {
        videoHandler.sendEmptyMessage(MSG_PREPARE);
    }

    @Override
    public void play() {
        videoHandler.sendEmptyMessage(MSG_PLAY);
    }

    @Override
    public void pause() {
        videoHandler.sendEmptyMessage(MSG_PAUSE);
    }

    @Override
    public void stop() {
        videoHandler.sendEmptyMessage(MSG_STOP);
    }

    @Override
    public void seekTo(long timeMs) {
        videoHandler.obtainMessage(MSG_SEEK, timeMs).sendToTarget();
    }

    @Override
    public void setVideoSize(int width, int height) {
        videoHandler.obtainMessage(MSG_SET_VIDEO_SIZE, width, height).sendToTarget();
    }

    @Override
    public void setPlayEventListener(PlayEventListener listener) {
        videoHandler.obtainMessage(MSG_SET_PLAY_LISTENER, listener).sendToTarget();
    }

    @Override
    public void setExportEventListener(ExportEventListener listener) {
        videoHandler.obtainMessage(MSG_SET_EXPORT_LISTENER, listener).sendToTarget();
    }

    @Override
    public int getVideoWidth() {
        return videoWidth;
    }

    @Override
    public int getVideoHeight() {
        return videoHeight;
    }

    @Override
    public void reset() {
        stop();
        videoHandler.sendEmptyMessage(MSG_RESET);
    }

    @Override
    public void release() {
        videoHandler.sendEmptyMessage(MSG_RELEASE);
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        videoHandler.obtainMessage(MSG_DEBUG_MODE, debugMode).sendToTarget();
    }

    @Override
    public void doDebugRender(long positionUs) {
        videoHandler.obtainMessage(MSG_DEBUG_DO_RENDER, positionUs).sendToTarget();
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
            case MSG_PREPARE_ENGINE:
                prepareEngine((SurfaceTexture) msg.obj);
                return true;
            case MSG_RELEASE:
                releaseInternal();
                return true;
            case MSG_DEBUG_MODE:
                debugMode = (boolean) msg.obj;
                return true;
            case MSG_DEBUG_DO_RENDER:
                debugPositionUs = (long) msg.obj;
                doRenderWork();
                return true;
            case MSG_DO_AUDIO_WORK:
                doAudioWork();
                return true;
        }
        return false;
    }

    private void releaseInternal() {
        videoHandler.removeMessages(MSG_DO_SOME_WORK);
        videoThread.quitSafely();
        for (MediaItem item : renderNodeMap.keySet()) {
            Log.d(TAG, "releaseInternal: " + item.toString());
            RenderNode node = renderNodeMap.get(item);
            node.release();
        }
        renderNodeMap.clear();
        if (renderTarget != null) {
            renderTarget.release();
            renderTarget = null;
        }
        if (engine != null) {
            engine.release();
            engine = null;
        }
    }

    private void setTimelineInternal(Timeline timeline) {
        if (this.timeline != timeline) {
            this.timeline = timeline;
        }
    }

    private void setPreviewInternal(TextureView previewView) {
        releaseRenderTarget();
        renderTarget = new PreviewRenderTarget();
        export = false;
        previewView.setSurfaceTextureListener(this);
        if (previewView.isAvailable()) {
            prepareEngine(previewView.getSurfaceTexture());
            canvasWidth = previewView.getWidth();
            canvasHeight = previewView.getHeight();
        } else {
            prepareEngine(null);
        }
    }

    private void exportInternal(ExportConfiguration exportConfiguration) {
        releaseRenderTarget();
        export = true;
        renderTarget = new MuxerRenderTarget(exportConfiguration);
        frameIntervalUs = TimeUtil.BILLION_US / exportConfiguration.getFrameRate();
        videoWidth = canvasWidth = exportConfiguration.getWidth();
        videoHeight = canvasHeight = exportConfiguration.getHeight();
        engine.setup(renderTarget.getInputSurface());
        prepare();
        play();
    }

    private void prepareInternal() {
        Log.d(TAG, "prepareInternal");
        if (renderTarget == null) {
            return;
        }
        renderTarget.prepareVideo();
        timeline.prepare(renderFactory);
        renderRoot = generateRenderTree(videoTimeUs);
        if (timeline.getAudioItem() != null) {
            AudioItem audioItem = timeline.getAudioItem();
            audioSource = new AudioSource(audioItem.getFilePath());
            audioSource.setTimeRange(0, audioItem.getEndTimeMs());
            audioSource.prepare();
            renderTarget.prepareAudio(audioSource.getSampleRate(), audioSource.getChannelCount());
        }
        eventDispatcher.onPreparing(this);
    }

    private void playInternal() {
        Log.d(TAG, "playInternal");
        playing.set(true);
        mediaClock.start();
        videoHandler.sendEmptyMessage(MSG_DO_SOME_WORK);
        audioHandler.sendEmptyMessage(MSG_DO_AUDIO_WORK);
        if (export) {
            eventDispatcher.onExportStart(this);
        } else {
            eventDispatcher.onPlay(this);
        }
    }

    private void pauseInternal() {
        mediaClock.stop();
        playing.set(false);
        if (!export) {
            eventDispatcher.onPause(this);
        }
    }

    private void stopInternal() {
        playing.set(false);
        mediaClock.stop();
        videoHandler.removeMessages(MSG_DO_SOME_WORK);
        audioHandler.removeMessages(MSG_DO_AUDIO_WORK);
        renderTarget.complete();
        if (export) {
            eventDispatcher.onExportComplete(this);
        } else {
            eventDispatcher.onStop(this);
        }
    }

    private void seekInternal(long timeMs) {
        long timeUs = TimeUtil.msToUs(timeMs);
        renderRoot = generateRenderTree(timeUs);
        renderRoot.seekTo(timeMs);
        mediaClock.setPositionUs(timeUs);
        if (audioSource != null) {
            audioSource.seekTo(timeMs);
        }
        if (!export) {
            eventDispatcher.onSeeking(this, true, timeMs);
        }
    }

    private void setVideoSizeInternal(int width, int height) {
        if (!playing.get()) {
            videoWidth = width;
            videoHeight = height;
        }
    }

    private void setPlayListenerInternal(PlayEventListener listener) {
        playEventListener = listener;
        eventDispatcher.setPlayEventListener(listener);
    }

    private void setExportListenerInternal(ExportEventListener listener) {
        exportEventListener = listener;
        eventDispatcher.setExportEventListener(listener);
    }

    private void doAudioWork() {
        if (!playing.get() || audioSource == null) {
            return;
        }

        audioTimeUs = audioSource.acquireBuffer(audioTimeUs);
        if (renderTarget != null) {
            if (audioSource != null && audioSource.isHasData()) {
                renderTarget.updateAudioChunk(audioSource);
            }
        }
        MediaCodec.BufferInfo info = audioSource.getAudioInfo();
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            return;
        }
        if (playing.get()) {
            audioHandler.sendEmptyMessage(MSG_DO_AUDIO_WORK);
        }
    }

    private void doRenderWork() {
        long operationStartMs = SystemClock.elapsedRealtime();
        videoTimeUs = debugMode ? debugPositionUs : getCurrentTimeUs();
        if (renderRoot == null) {
            if (export) {
                eventDispatcher.onExportComplete(this);
            } else {
                eventDispatcher.onStop(this);
            }
            return;
        }
        if (videoTimeUs > TimeUtil.msToUs(timeline.getEndTimeMs())) {
            stop();
            return;
        }
        // do render tree
        elapsedRealtimeUs = TimeUtil.msToUs(SystemClock.elapsedRealtime());

        renderRoot.setViewport(canvasWidth, canvasHeight);
        long renderTimeUs = renderRoot.acquireFrame(videoTimeUs);
//        Log.d(TAG, "doRenderWork: " + renderTimeUs + " " + videoTimeUs);
        if (renderTarget != null) {
            if (renderTimeUs >= videoTimeUs) {
//                Log.d(TAG, "doRenderWork: has frame and render to target");
                // render result to RenderTarget
                videoTimeUs = renderTimeUs;
                if (export) {
                    engine.setPresentationTime(TimeUtil.usToNs(renderTimeUs));
                }
                renderTarget.updateFrame(renderRoot, videoTimeUs, engine);
                if (export) {
                    eventDispatcher.onExportProgress(this, videoTimeUs, timeline.getEndTimeMs());
                } else {
                    eventDispatcher.onPositionChange(this, videoTimeUs, timeline.getEndTimeMs());
                }
            }
            if (export) {
                if (renderTimeUs <= exportTimeUs) {
                    exportTimeUs += frameIntervalUs;
                } else {
                    exportTimeUs = renderTimeUs;
                }
            }
        }
//        cleanupInvalidRenderNode(videoTimeUs);
        renderRoot = generateRenderTree(export ? exportTimeUs : videoTimeUs);
        if (renderRoot != null) {
            scheduleNextWork(operationStartMs, 10);
        } else {
            videoHandler.removeMessages(MSG_DO_SOME_WORK);
        }
    }

    private long getCurrentTimeUs() {
        long time;
        if (export) {
            time = exportTimeUs;
        } else {
            time = mediaClock.getPositionUs();
        }
        return time;
    }

    private void scheduleNextWork(long operationTimeMs, long intervalTimeMs) {
        videoHandler.removeMessages(MSG_DO_SOME_WORK);
        if (debugMode) {
            return;
        }
        long nextOperationStartTime = operationTimeMs + intervalTimeMs;
        long nextOperationDelayMs = nextOperationStartTime - SystemClock.elapsedRealtime();
        if (nextOperationDelayMs <= 0 || export) {
            videoHandler.sendEmptyMessage(MSG_DO_SOME_WORK);
        } else {
            videoHandler.sendEmptyMessageDelayed(MSG_DO_SOME_WORK, nextOperationDelayMs);
        }
    }

    private RenderNode generateRenderTree(long presentationTimeUs) {
        SparseArray<List<MediaItem>> itemLayers = timeline.queryPresentationTimeItems(presentationTimeUs);
        RenderNode root = renderRoot;
        Map<MediaItem, RenderNode> nodeCache = new HashMap<>();
        for (int i = 0; i < itemLayers.size(); i++) {
            int layer = itemLayers.keyAt(i);
            List<MediaItem> items = itemLayers.get(layer);
            for (MediaItem item : items) {
                RenderNode renderNode;
                if (!renderNodeMap.containsKey(item)) {
                    renderNode = renderFactory.createRenderNode(item);
                    renderNodeMap.put(item, renderNode);
                } else {
                    renderNode = renderNodeMap.get(item);
                }
                if (renderNode != null) {
                    renderNode.inputNodes.clear();
                    if (item.getBaseItem().size() > 0) {
                        for (int j = 0; j < item.getBaseItem().size(); j++) {
                            MediaItem dependItem = item.getBaseItem().valueAt(j);
                            if (nodeCache.containsKey(dependItem)) {
                                RenderNode inputNode = nodeCache.get(dependItem);
                                inputNode.setDebugMode(debugMode);
                                renderNode.addInputNode(inputNode);
                            }
                        }
                    } else {
                        renderNode.addInputNode(root);
                    }
                    if (!renderNode.isPrepared()) {
                        renderNode.prepare();
                    }
                    renderNode.setDebugMode(debugMode);
                    renderNode.setViewport(canvasWidth, canvasHeight);
                    renderNode.setOriginSize(videoWidth, videoHeight);
                    nodeCache.put(item, renderNode);
                }
                if (renderNode != null) {
                    root = renderNode;
                }
            }
        }
        return root;
    }

    private void cleanupInvalidRenderNode(long presentationTimeUs) {
        Iterator<Map.Entry<MediaItem, RenderNode>> iterator = renderNodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MediaItem, RenderNode> entry = iterator.next();
            if (TimeUtil.usToMs(presentationTimeUs) > entry.getKey().getEndTimeMs()) {
                entry.getValue().release();
                iterator.remove();
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        if (engine != null) {
            videoHandler.obtainMessage(MSG_PREPARE_ENGINE, surface).sendToTarget();
        }
    }

    private void prepareEngine(SurfaceTexture surface) {
        engine.setup(surface);
        if (export) {
            renderTarget.prepareVideo();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        if (engine != null) {
            engine.setViewport(width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (!videoThread.isAlive()) {
            return false;
        }
        stop();
        releaseRenderTarget();
        if (engine != null) {
            engine.release();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private void releaseRenderTarget() {
        if (renderTarget != null) {
            renderTarget.release();
            renderTarget = null;
        }
    }

    private static class EventDispatcher implements PlayEventListener, ExportEventListener {
        private Handler handler;
        private PlayEventListener playEventListener;
        private ExportEventListener exportEventListener;

        public EventDispatcher(Handler handler) {
            this.handler = handler;
        }

        public void setPlayEventListener(PlayEventListener playEventListener) {
            this.playEventListener = playEventListener;
        }

        public void setExportEventListener(ExportEventListener exportEventListener) {
            this.exportEventListener = exportEventListener;
        }

        public PlayEventListener getPlayEventListener() {
            return playEventListener;
        }

        public ExportEventListener getExportEventListener() {
            return exportEventListener;
        }

        @Override
        public void onPreparing(MediaComposer composer) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onPreparing(composer));
            }
            if (exportEventListener != null) {
                handler.post(() -> exportEventListener.onPreparing(composer));
            }
        }

        @Override
        public void onExportStart(MediaComposer composer) {
            if (exportEventListener != null) {
                handler.post(() -> exportEventListener.onExportStart(composer));
            }
        }

        @Override
        public void onExportProgress(MediaComposer composer, long presentationTimeUs, long totalTimeUs) {
            if (exportEventListener != null) {
                handler.post(() -> exportEventListener.onExportProgress(composer, presentationTimeUs, totalTimeUs));
            }
        }

        @Override
        public void onExportComplete(MediaComposer composer) {
            if (exportEventListener != null) {
                handler.post(() -> exportEventListener.onExportComplete(composer));
            }
        }

        @Override
        public void onExportError(MediaComposer composer, Exception exception) {
            if (exportEventListener != null) {
                handler.post(() -> exportEventListener.onExportError(composer, exception));
            }
        }

        @Override
        public void onPlay(MediaComposer composer) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onPlay(composer));
            }
        }

        @Override
        public void onPause(MediaComposer composer) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onPause(composer));
            }
        }

        @Override
        public void onPositionChange(MediaComposer composer, long presentationTimeUs, long totalTimeUs) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onPositionChange(composer, presentationTimeUs, totalTimeUs));
            }
        }

        @Override
        public void onStop(MediaComposer composer) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onStop(composer));
            }
        }

        @Override
        public void onSeeking(MediaComposer composer, boolean seekComplete, long seekTimeMs) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onSeeking(composer, seekComplete, seekTimeMs));
            }
        }

        @Override
        public void onError(MediaComposer composer, Exception exception) {
            if (playEventListener != null) {
                handler.post(() -> playEventListener.onError(composer, exception));
            }
        }
    }
}
