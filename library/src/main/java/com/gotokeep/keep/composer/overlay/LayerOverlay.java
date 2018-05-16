package com.gotokeep.keep.composer.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.gotokeep.keep.composer.RenderNode;
import com.gotokeep.keep.composer.RenderTexture;
import com.gotokeep.keep.composer.gles.OpenGlUtils;
import com.gotokeep.keep.composer.gles.ProgramObject;

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
    protected ProgramObject createProgramObject() {
        return ProgramObject.getDefaultProgram();
    }

    @Override
    protected void onPrepare() {
        if (layerTexture == null) {
            layerTexture = new RenderTexture(RenderTexture.TEXTURE_NATIVE);
            Bitmap layerBitmap = BitmapFactory.decodeFile(layerImagePath);
            width = layerBitmap.getWidth();
            height = layerBitmap.getHeight();
            OpenGlUtils.loadTexture(layerBitmap, layerTexture.getTextureId(), true);
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
    protected long doRender(ProgramObject programObject, long positionUs) {
        // draw source
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // draw layer
        updateLayerMatrix();
        layerTexture.bind(0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return 0;
    }

    @Override
    protected void bindRenderTextures(boolean[] shouldRender) {
        renderTexture.bind(0);
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
    }
}
