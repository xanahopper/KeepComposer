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


    final class PositionInfo {
        long currentTimeUs;
        long totalTimeUs;

        public PositionInfo(long currentTimeUs, long totalTimeUs) {
            this.currentTimeUs = currentTimeUs;
            this.totalTimeUs = totalTimeUs;
        }
    }
}
