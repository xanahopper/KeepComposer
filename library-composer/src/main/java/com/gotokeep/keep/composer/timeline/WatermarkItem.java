package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-17 10:36
 */
public class WatermarkItem extends OverlayItem {
    private String imagePath;
    private int imageLeft;
    private int imageTop;
    private String text;
    private int textTop;
    private int textHorizontalPoint;
    private int textAlignment;
    private int textColor;
    private int textSize;

    public WatermarkItem(int layer) {
        super(layer);
    }

    public String getImagePath() {
        return imagePath;
    }

    public WatermarkItem setImagePath(String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    public int getImageLeft() {
        return imageLeft;
    }

    public WatermarkItem setImageLeft(int imageLeft) {
        this.imageLeft = imageLeft;
        return this;
    }

    public int getImageTop() {
        return imageTop;
    }

    public WatermarkItem setImageTop(int imageTop) {
        this.imageTop = imageTop;
        return this;
    }

    public String getText() {
        return text;
    }

    public WatermarkItem setText(String text) {
        this.text = text;
        return this;
    }

    public int getTextTop() {
        return textTop;
    }

    public WatermarkItem setTextTop(int textTop) {
        this.textTop = textTop;
        return this;
    }

    public int getTextHorizontalPoint() {
        return textHorizontalPoint;
    }

    public WatermarkItem setTextHorizontalPoint(int textHorizontalPoint) {
        this.textHorizontalPoint = textHorizontalPoint;
        return this;
    }

    public int getTextAlignment() {
        return textAlignment;
    }

    public WatermarkItem setTextAlignment(int textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public WatermarkItem setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public WatermarkItem setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }


}
