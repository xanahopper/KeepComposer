package com.gotokeep.keep.composer.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.OpenGlUtils;
import com.gotokeep.keep.composer.gles.ProgramObject;
import com.gotokeep.keep.composer.util.MediaUtil;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 15:51
 */
public class LayerOverlay extends MediaOverlay {

    private final String layerImagePath;
    private RenderTexture layerTexture;

    public LayerOverlay(RenderNode mainInputNode, String layerImagePath) {
        super(mainInputNode);
        this.layerImagePath = layerImagePath;
    }

    @Override
    protected void onPrepare() {
        super.onPrepare();
        if (layerTexture == null) {
            layerTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE);
            Bitmap layerBitmap = MediaUtil.flipBitmap(BitmapFactory.decodeFile(layerImagePath), true);
            width = layerBitmap.getWidth();
            height = layerBitmap.getHeight();

            layerTexture.bind();
            int format = GLUtils.getInternalFormat(layerBitmap);
            GLUtils.texImage2D(layerTexture.getTextureTarget(), 0, format, layerBitmap, 0);
            layerBitmap.recycle();
        }
    }

    @Override
    protected void onRelease() {
        if (layerTexture != null) {
            layerTexture.release();
            layerTexture = null;
        }
    }

    @Override
    protected void renderOverlay(ProgramObject overlayProgramObject) {
        // draw layer
//        GLES20.glEnable(GLES20.GL_BLEND);
        overlayProgramObject.use();
        activeAttribData();
        layerTexture.bind(0);
        updateLayerMatrix();
        GLES20.glUniformMatrix4fv(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, ProgramObject.DEFAULT_MATRIX, 0);
        GLES20.glUniform1i(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        Log.d("MediaOverlay", "render layer");
    }
}
