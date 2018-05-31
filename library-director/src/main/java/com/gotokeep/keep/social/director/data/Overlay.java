package com.gotokeep.keep.social.director.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:41
 */
public final class Overlay implements MediaData {
    private long startTime;
    private long endTime;
    private float scale = 1f;
    private float rotation;
    private int offsetX;
    private int offsetY;
    private String position;

    private LayerOverlay layer;
    private WatermarkOverlay watermark;

    public LayerOverlay getLayer() {
        return layer;
    }

    public WatermarkOverlay getWatermark() {
        return watermark;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
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

    @Override
    public String toString() {
        return "Overlay{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", scale=" + scale +
                ", rotation=" + rotation +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", position='" + position + '\'' +
                ", layer=" + layer +
                ", watermark=" + watermark +
                '}';
    }

    @Override
    public List<String> getResources() {
        List<String> resources = new ArrayList<>();
        if (layer != null) {
            resources.addAll(layer.getResources());
        }
        if (watermark != null) {
            resources.addAll(watermark.getResources());
        }
        return resources;
    }
}
