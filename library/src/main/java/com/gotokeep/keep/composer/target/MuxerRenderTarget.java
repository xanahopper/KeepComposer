package com.gotokeep.keep.composer.target;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.gotokeep.keep.composer.ExportConfiguration;
import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;

import java.io.IOException;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 10:41
 */
public class MuxerRenderTarget extends RenderNode {
    private static final String RENDER_MIME = "video/avc";

    private MediaCodec encoder;
    private MediaFormat encodeFormat;
    private MediaCodec.BufferInfo encodeInfo;
    private int muxerTrackIndex = -1;
    private MediaMuxer muxer;

    private ExportConfiguration exportConfiguration;
    private String exportPath;

    public MuxerRenderTarget(ExportConfiguration exportConfiguration) {
        this.exportConfiguration = exportConfiguration;

        try {
            this.encoder = MediaCodec.createEncoderByType(RENDER_MIME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture();
    }

    @Override
    protected ProgramObject createProgramObject() {
        return new ProgramObject();
    }

    @Override
    protected void onPrepare() {

    }

    @Override
    protected void onRelease() {

    }

    @Override
    protected void onRender(ProgramObject programObject, long presentationTimeUs) {

    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }
}
