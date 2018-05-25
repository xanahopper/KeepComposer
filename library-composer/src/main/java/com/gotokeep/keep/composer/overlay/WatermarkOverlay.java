package com.gotokeep.keep.composer.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.opengl.GLUtils;
import android.text.TextPaint;

import com.gotokeep.keep.composer.gles.ProgramObject;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 15:54
 */
public class WatermarkOverlay extends LayerOverlay {
    private int imageLeft;
    private int imageTop;
    private int imageRotation;
    private float imageScale;

    private String text;
    private int textLeft;
    private int textTop;
    private int textSize;
    private int textColor;

    public WatermarkOverlay(String layerImagePath) {
        super(layerImagePath);
    }

    private WatermarkOverlay(Builder builder) {
        super(builder.layerImagePath);
        imageLeft = builder.imageLeft;
        imageTop = builder.imageTop;
        imageRotation = builder.imageRotation;
        imageScale = builder.imageScale;
        text = builder.text;
        textLeft = builder.textLeft;
        textTop = builder.textTop;
        textSize = builder.textSize;
        textColor = builder.textColor;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    protected void prepareResource() {
        updateResource(0);
    }

    @Override
    protected void renderOverlay(ProgramObject overlayProgramObject, long positionUs) {
        updateResource(positionUs);
        super.renderOverlay(overlayProgramObject, positionUs);
    }

    protected void updateResource(long positionUs) {
        if (positionUs == 0) {
            Bitmap image = BitmapFactory.decodeFile(layerImagePath);

            Matrix imageMatrix = new Matrix();
            imageMatrix.postScale(imageScale, imageScale);
            imageMatrix.postRotate(imageRotation);
            imageMatrix.postTranslate(imageLeft, imageTop);
            RectF rect = new RectF(0, 0, image.getWidth(), image.getHeight());
            imageMatrix.mapRect(rect);
            TextPaint textPaint = new TextPaint();
            textPaint.setTextSize(textSize);
            textPaint.setColor(textColor);
            float textWidth = textPaint.measureText(text);

            Bitmap targetImage = Bitmap.createBitmap((int) Math.max(rect.width(), textWidth),
                    (int) Math.max(rect.height(), textTop + textSize), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(targetImage);
            canvas.drawBitmap(image, imageMatrix, null);
            image.recycle();

            canvas.drawText(text, textLeft, textTop, textPaint);
            int format = GLUtils.getInternalFormat(targetImage);

            layerTexture.bind();
            GLUtils.texImage2D(layerTexture.getTextureTarget(), 0, format, targetImage, 0);

            targetImage.recycle();
        }
    }

    public static final class Builder {
        private String layerImagePath;
        private int imageLeft;
        private int imageTop;
        private int imageRotation;
        private float imageScale = 1f;
        private String text;
        private int textLeft;
        private int textTop;
        private int textSize;
        private int textColor;

        private Builder() {
        }

        public Builder setLayerImagePath(String layerImagePath) {
            this.layerImagePath = layerImagePath;
            return this;
        }

        public Builder setImageLeft(int imageLeft) {
            this.imageLeft = imageLeft;
            return this;
        }

        public Builder setImageTop(int imageTop) {
            this.imageTop = imageTop;
            return this;
        }

        public Builder setImageRotation(int imageRotation) {
            this.imageRotation = imageRotation;
            return this;
        }

        public Builder setImageScale(float imageScale) {
            this.imageScale = imageScale;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setTextLeft(int textLeft) {
            this.textLeft = textLeft;
            return this;
        }

        public Builder setTextTop(int textTop) {
            this.textTop = textTop;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public WatermarkOverlay build() {
            return new WatermarkOverlay(this);
        }
    }
}
