package com.gotokeep.keep.composer.target;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import com.gotokeep.keep.composer.ExportConfiguration;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTarget;

import java.io.IOException;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 10:41
 */
public class MuxerRenderTarget extends RenderTarget {
    private static final String RENDER_MIME = "video/avc";

    private MediaCodec encoder;
    private MediaFormat encodeFormat;
    private MediaCodec.BufferInfo encodeInfo;
    private int muxerTrackIndex = -1;
    private MediaMuxer muxer;
    private Surface encodeInputSurface;

    private ExportConfiguration exportConfiguration;
    private String exportPath;

    public MuxerRenderTarget(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;

        try {
            this.encoder = MediaCodec.createEncoderByType(RENDER_MIME);
            this.encodeInputSurface = encoder.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Surface getInputSurface() {
        return encodeInputSurface;
    }

    @Override
    public void updateFrame(RenderNode renderNode, long presentationTimeUs) {

    }

    @Override
    public void prepare() {

    }

    @Override
    public void complete() {

    }

    @Override
    public void release() {

    }
}
