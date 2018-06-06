package com.gotokeep.keep.social.composer.util;

import android.opengl.Matrix;

import com.gotokeep.keep.social.composer.ScaleType;

import static com.gotokeep.keep.social.composer.ScaleType.*;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-06-04 16:56
 */
public final class ScaleUtil {
    public static float[] getScaleMatrix(ScaleType scaleType, float[] matrix, int viewWidth, int viewHeight,
                                         int sourceWidth, int sourceHeight) {
        if (matrix == null || matrix.length != 16) {
            return matrix;
        }
        Matrix.setIdentityM(matrix, 0);
        float scaleX = (float) sourceWidth / (float) viewWidth;
        float scaleY = (float) sourceHeight / (float) viewHeight;
        float scale = 1f;
        float dx = 0f;
        float dy = 0f;
        switch (scaleType) {
            case CENTER_INSIDE:
                if (sourceWidth <= viewWidth && sourceHeight <= viewHeight) {
                    scale = 1f;
                } else {
                    scale = Math.min((float) viewWidth / (float) sourceWidth,
                            (float) viewHeight / (float) sourceHeight);
                }
                dx = (viewWidth - sourceWidth * scale) * 0.5f / (float) viewWidth;
                dy = (viewHeight - sourceHeight * scale) * 0.5f / (float) viewHeight;
                break;
            case CENTER_CROP:
                if (sourceWidth * viewHeight > viewWidth * sourceHeight) {
                    scale = (float) viewHeight / (float) sourceHeight;
                    dx = (viewWidth - sourceWidth * scale) * 0.5f / (float) viewWidth;
                } else {
                    scale = (float) viewWidth / (float) sourceWidth;
                    dy = (viewHeight - sourceHeight * scale) * 0.5f / (float) viewHeight;
                }
                break;
            case FIT_CENTER:
                float sx = (float) viewWidth / (float) sourceWidth;
                float sy = (float) viewHeight / (float) sourceHeight;
                int tx = 0, ty = 0;
                boolean xLarger = sx > sy;
                if (xLarger) {
                    sx = sy;
                } else {
                    sy = sx;
                }
                int diff = (int) ((xLarger ? viewWidth - sourceWidth * sy : viewHeight - sourceHeight * sy) * 0.5f);
                if (xLarger) {
                    tx += diff;
                } else {
                    ty += diff;
                }
                scale = sy;
                dx = (float) tx / (float) viewWidth;
                dy = (float) ty / (float) viewHeight;
                break;
        }
//        Matrix.translateM(matrix, 0, dx, dy, 0);
        Matrix.scaleM(matrix, 0, scaleX * scale, scaleY * scale, 1f);
//        Matrix.scaleM(matrix, 0, scaleX, scaleY, 1f);
        return matrix;
    }
}
