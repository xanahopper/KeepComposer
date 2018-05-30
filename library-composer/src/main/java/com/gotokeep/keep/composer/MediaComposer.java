package com.gotokeep.keep.composer;

import android.view.TextureView;

import com.gotokeep.keep.composer.timeline.Timeline;

/**
 * Keep Media Composer
 *
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 13:29
 */
public interface MediaComposer {
    void setTimeline(Timeline timeline);

    void setPreview(TextureView previewView);

    void export(ExportConfiguration exportConfiguration);

    void prepare();

    void play();

    void pause();

    void stop();

    void seekTo(long timeMs);

    void setVideoSize(int width, int height);

    void setPlayEventListener(PlayEventListener listener);

    void setExportEventListener(ExportEventListener listener);

    int getVideoWidth();

    int getVideoHeight();

    void reset();

    void release();

    void setLoop(boolean loop);

    void setDebugMode(boolean debugMode);

    void doDebugRender(long positionUs);

    interface PlayEventListener {
        void onPreparing(MediaComposer composer, RenderNode preparingNode);

        void onPlay(MediaComposer composer);

        void onPause(MediaComposer composer);

        void onPositionChange(MediaComposer composer, long presentationTimeUs, long totalTimeUs);

        void onStop(MediaComposer composer);

        void onSeeking(MediaComposer composer, boolean seekComplete, long seekTimeMs);

        void onError(MediaComposer composer, Exception exception);
    }

    interface ExportEventListener {
        void onPreparing(MediaComposer composer, RenderNode preparingNode);

        void onExportStart(MediaComposer composer);

        void onExportProgress(MediaComposer composer, long presentationTimeUs, long totalTimeUs);

        void onExportComplete(MediaComposer composer);

        void onExportError(MediaComposer composer, Exception exception);
    }

    class SimplePlayEventListener implements PlayEventListener {

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

    class SimpleExportEventListener implements ExportEventListener {

        @Override
        public void onPreparing(MediaComposer composer, RenderNode preparingNode) {

        }

        @Override
        public void onExportStart(MediaComposer composer) {

        }

        @Override
        public void onExportProgress(MediaComposer composer, long presentationTimeUs, long totalTimeUs) {

        }

        @Override
        public void onExportComplete(MediaComposer composer) {

        }

        @Override
        public void onExportError(MediaComposer composer, Exception exception) {

        }
    }

    final class PositionInfo {
        public long currentTimeUs;
        public long totalTimeUs;

        public PositionInfo(long currentTimeUs, long totalTimeUs) {
            this.currentTimeUs = currentTimeUs;
            this.totalTimeUs = totalTimeUs;
        }
    }
}
