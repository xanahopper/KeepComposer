package com.gotokeep.keep.composer.timeline;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:07
 */
public class OverlayItem extends MediaItem {
    public static final String TYPE_SUBTITLE = "subtitle";
    public static final String TYPE_WATERMARK = "watermark";
    public static final String TYPE_LAYER = "layer";

    private int offsetX;
    private int offsetY;
    private String position;
    private float rotation;
    private float scale;
    protected String type;
    protected MediaItem baseItem;
    protected String name;

    public OverlayItem(MediaItem baseItem, int layer) {
        super(TYPE_OVERLAY, layer);
        this.baseItem = baseItem;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
