package com.gotokeep.keep.social.composer.source;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.gotokeep.keep.social.composer.gles.ProgramObject;
import com.gotokeep.keep.social.composer.gles.RenderTexture;
import com.gotokeep.keep.social.composer.util.MediaUtil;
import com.gotokeep.keep.social.composer.util.TimeUtil;

import java.io.IOException;

/**
 * TODO: scaleType with final render size.
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018/5/13 14:10
 */
public class ImageMediaSource extends MediaSource {
    private static final int DEFAULT_FRAME_RATE = 25;
    private static final String TAG = ImageMediaSource.class.getSimpleName();

    private final String filePath;
    private final long intervalUs;
    private final RenderTexture sourceTexture;

    public ImageMediaSource(String filePath) {
        this(filePath, DEFAULT_FRAME_RATE);
    }

    public ImageMediaSource(String filePath, int frameRate) {
        super(TYPE_IMAGE);
        this.filePath = filePath;
        this.presentationTimeUs = 0;
        this.intervalUs = TimeUtil.BILLION_US / frameRate;
        this.sourceTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE, "Image");
    }

    @Override
    protected ProgramObject createProgramObject() {
        return null;
    }

    @Override
    protected void onPreload() {

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
    public long acquireFrame(long positionUs) {
        renderTimeUs = render(positionUs);
        return renderTimeUs;
    }

    @Override
    protected long doRender(ProgramObject programObject, long positionUs) {
//        Log.d(TAG, "doRender: " + positionUs + ", return " + (positionUs + intervalUs));
        presentationTimeUs = positionUs - TimeUtil.msToUs(startTimeMs);
        return positionUs + intervalUs;
    }

    @Override
    protected boolean needRenderSelf() {
        return false;
    }

    @Override
    public boolean isFrameAvailable() {
        return isPrepared();
    }

    @Override
    protected void bindRenderTextures() {

    }

    @Override
    protected void unbindRenderTextures() {

    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, scaleMatrix, 0);
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

            sourceTexture.bind();
            int format = GLUtils.getInternalFormat(finalImage);
            GLUtils.texImage2D(sourceTexture.getTextureTarget(), 0, format, finalImage, GLES20.GL_UNSIGNED_BYTE, 0);
            finalImage.recycle();
        } catch (IOException e) {
            throw new IllegalArgumentException("ImageMediaSource prepareVideo failed.", e);
        }

        durationMs = DURATION_INFINITE;
    }

    @Override
    protected String getName() {
        return "ImageMediaSource[" + Uri.parse(filePath).getLastPathSegment() + "]";
    }
}
