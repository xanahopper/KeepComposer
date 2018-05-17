package com.gotokeep.keep.composer.overlay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

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
    private ProgramObject layerProgramObject;
    private RenderNode mainNode;

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
            Bitmap layerBitmap = MediaUtil.flipBitmap(BitmapFactory.decodeFile(layerImagePath), true);
            width = layerBitmap.getWidth();
            height = layerBitmap.getHeight();

            layerTexture.bind();
            int format = GLUtils.getInternalFormat(layerBitmap);
            GLUtils.texImage2D(layerTexture.getTextureTarget(), 0, format, layerBitmap, 0);
            layerBitmap.recycle();
        }
        if (layerProgramObject == null) {
            layerProgramObject = getOverlayProgramObject();
        }
        mainNode = inputNodes.valueAt(0);
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
        layerProgramObject.use();
        activeAttribData();
        updateLayerMatrix();
        layerTexture.bind(0);
        GLES20.glUniformMatrix4fv(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                1, false, ProgramObject.DEFAULT_MATRIX, 0);
        GLES20.glUniform1i(overlayProgramObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return mainNode.getPresentationTimeUs();
    }

    @Override
    protected void bindRenderTextures(boolean[] shouldRender) {
        for (int i = 0; i < inputNodes.size(); i++) {
            if (shouldRender.length > i && shouldRender[i]) {
                inputNodes.valueAt(i).getOutputTexture().bind(i);
            }
        }
    }

    @Override
    protected void updateRenderUniform(ProgramObject programObject, long presentationTimeUs) {
        if (mainNode != null) {
            GLES20.glUniformMatrix4fv(programObject.getUniformLocation(ProgramObject.UNIFORM_TRANSFORM_MATRIX),
                    1, false, mainNode.getTransformMatrix(), 0);
        }
        GLES20.glUniform1i(programObject.getUniformLocation(ProgramObject.UNIFORM_TEXTURE), 0);
    }
}
