package com.gotokeep.keep.composer.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.text.TextPaint;
import android.util.Log;

import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.gles.RenderTexture;
import com.gotokeep.keep.composer.timeline.item.OverlayItem;
import com.gotokeep.keep.composer.timeline.item.TextItem;
import com.gotokeep.keep.composer.util.MediaUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 16:03
 */
public class SubtitleOverlay extends MediaOverlay {
    public static final float RADIUS = 0.25f;
    private String content = "";
    private float textSize = 12;
    private int textColor = Color.WHITE;
    private String fontName;
    private int shadowColor = Color.DKGRAY;
    private static final float SHADOW_OFFSET = 0.02f;

    private Bitmap targetBitmap;
    private Canvas targetCanvas;
    private TextPaint textPaint;
    private RenderTexture subtitleTexture;
    private Rect textBounds;
    private Context context;

    public SubtitleOverlay(Context context) {
        this.context = context;
    }

    @Override
    public void initWithMediaItem(OverlayItem item) {
        super.initWithMediaItem(item);
        if (item instanceof TextItem) {
            TextItem textItem = (TextItem) item;
            content = textItem.getContent();
            textSize = textItem.getTextSize();
            textColor = textItem.getTextColor();
            fontName = textItem.getFontName();
            shadowColor = textItem.getShadowColor();
        }
    }

    @Override
    protected void onPreload() {
        updateResource();
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
        prepareTexture();
    }

    @Override
    protected void onRelease() {
        if (targetBitmap != null) {
            if (!targetBitmap.isRecycled()) {
                targetBitmap.recycle();
            }
            targetBitmap = null;
        }
        if (subtitleTexture != null) {
            subtitleTexture.release();
            subtitleTexture = null;
        }
    }

    @Override
    protected void renderOverlay(ProgramObject overlayProgramObject, long positionUs) {
        overlayProgramObject.use();
        subtitleTexture.bind(0);
        updateLayerMatrix();
        GLES20.glUniformMatrix4fv(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, ProgramObject.DEFAULT_MATRIX, 0);
        GLES20.glUniform1i(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void prepareTexture() {
        if (subtitleTexture == null) {
            subtitleTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE, "SubtitleOverlay:" + content);
            Bitmap subtitleBitmap = MediaUtil.flipBitmap(targetBitmap, true);
            width = subtitleBitmap.getWidth();
            height = subtitleBitmap.getHeight();

            subtitleTexture.bind();
            int format = GLUtils.getInternalFormat(subtitleBitmap);
            GLUtils.texImage2D(subtitleTexture.getTextureTarget(), 0, format, subtitleBitmap, 0);
            subtitleBitmap.recycle();
            targetBitmap = null;
        }
    }

    private void updateResource() {
        updateTextPaint();
        updateTargetBitmapSize(textBounds.width(), textBounds.height());
        drawContent();
    }

    private void updateTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint();
        }
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(TypefaceFactory.createTypeface(context, fontName));
        textPaint.setAntiAlias(true);
        // default shadow, can be custom in future
        textPaint.setShadowLayer(RADIUS, textSize * SHADOW_OFFSET,
                textSize * SHADOW_OFFSET, shadowColor);
        textBounds = new Rect();
        textPaint.getTextBounds(content, 0, content.length(), textBounds);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        Log.d("Text", "updateTextPaint: " + metrics.toString());
    }

    private void updateTargetBitmapSize(int width, int height) {
        if (targetBitmap != null && !targetBitmap.isRecycled() &&
                targetBitmap.getWidth() == width && targetBitmap.getHeight() == height) {
            return;
        }
        if (targetBitmap != null && !targetBitmap.isRecycled()) {
            targetBitmap.recycle();
        }
        targetBitmap = Bitmap.createBitmap((int) (width * (1 + SHADOW_OFFSET)),
                (int) (height * (1 + SHADOW_OFFSET)), Bitmap.Config.ARGB_4444);
        targetCanvas = new Canvas(targetBitmap);

    }

    private void drawContent() {
//        textPaint.setColor(~textColor);
//        targetCanvas.drawText(content, (float) (textSize * 0.2 - textBounds.left),
//                (float) (textSize * 0.2 - textBounds.top), textPaint);
        textPaint.setColor(textColor);
        targetCanvas.drawText(content, -textBounds.left, -textBounds.top, textPaint);
    }
}
