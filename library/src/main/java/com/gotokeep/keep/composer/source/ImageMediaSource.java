package com.gotokeep.keep.composer.source;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;

import java.io.IOException;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:10
 */
public class ImageMediaSource extends MediaSource {
    private final String filePath;

    public ImageMediaSource(String filePath) {
        super(TYPE_IMAGE);
        this.filePath = filePath;
        this.presentationTimeUs = 0;
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @Override
    protected ProgramObject createProgramObject() {
        return ProgramObject.getDefaultProgram();
    }

    @Override
    protected void onPrepare() {
        prepareInternal(filePath);
    }

    @Override
    protected void onRelease() {
        // renterTexture#release will release the loaded image and the texture resource. so here do nothing.
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        return positionUs;
    }

    @Override
    protected void bindRenderTextures(boolean[] shouldRender) {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }

    private void prepareInternal(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            Bitmap b = BitmapFactory.decodeFile(filePath);
            width = b.getWidth();
            height = b.getHeight();
            renderTexture.bind();
            GLUtils.texImage2D(renderTexture.getTextureTarget(), 0, GLES20.GL_RGBA, b, GLES20.GL_UNSIGNED_BYTE, 0);
            b.recycle();
        } catch (IOException e) {
            throw new IllegalArgumentException("ImageMediaSource prepare failed.", e);
        }

        durationMs = DURATION_INFINITE;
    }
}
