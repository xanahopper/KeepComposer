package com.gotokeep.keep.composer.timeline.item;

import android.graphics.Color;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-31 14:51
 */
public class TextItem extends OverlayItem {
    private String content = "";
    private float textSize = 12;
    private int textColor = Color.WHITE;
    private String fontName = "";
    private int shadowColor = Color.DKGRAY;

    public TextItem(int layer) {
        super(layer);
    }

    public TextItem(int layer, String content) {
        this(layer);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getShadowColor() {
        return shadowColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }
}
