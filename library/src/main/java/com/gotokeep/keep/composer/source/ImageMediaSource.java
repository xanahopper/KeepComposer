package com.gotokeep.keep.composer.source;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.MediaUtil;
import com.gotokeep.keep.composer.util.TimeUtil;

import java.io.IOException;

/**
 * TODO: scaleType with final render size.
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:10
 */
public class ImageMediaSource extends MediaSource {
    private static final int DEFAULT_FRAME_RATE = 25;

    private final String filePath;
    private final long intervalUs;


    public ImageMediaSource(String filePath) {
        this(filePath, DEFAULT_FRAME_RATE);
    }

    public ImageMediaSource(String filePath, int frameRate) {
        super(TYPE_IMAGE);
        this.filePath = filePath;
        this.presentationTimeUs = 0;
        this.intervalUs = TimeUtil.BILLION_US / DEFAULT_FRAME_RATE;
    }

    @Override
    protected RenderTexture createRenderTexture() {
        return new RenderTexture(RenderTexture.TEXTURE_NATIVE);
    }

    @Override
    protected ProgramObject createProgramObject() {
        return null;
    }

    @Override
    protected void onPrepare() {
        prepareInternal(filePath);
    }

    @Override
    protected boolean needRenderSelf() {
        return false;
    }

    @Override
    protected void onRelease() {
        // renterTexture#release will release the loaded image and the texture resource. so here do nothing.
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
        return positionUs + intervalUs;
    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {

    }

    private void prepareInternal(String filePath) {
        try {
            ExifInterface exif = new ExifInterface(filePath);
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);
            width = imageBitmap.getWidth();
            height = imageBitmap.getHeight();
            // 由于 OpenGL 纹理(0,0)在左下角，Bitmap(0,0)在左上角，而视频(0,0)由硬解后也是左下角，考虑到
            // 通用性和性能（图片只需要初始化一次即可），在这里对图片进行 Y 轴翻转
            Bitmap finalImage = MediaUtil.flipBitmap(imageBitmap, true);

            renderTexture.bind();
            int format = GLUtils.getInternalFormat(finalImage);
            GLUtils.texImage2D(renderTexture.getTextureTarget(), 0, format, finalImage, GLES20.GL_UNSIGNED_BYTE, 0);
            finalImage.recycle();
        } catch (IOException e) {
            throw new IllegalArgumentException("ImageMediaSource prepare failed.", e);
        }

        durationMs = DURATION_INFINITE;
    }
}
