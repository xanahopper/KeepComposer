package com.gotokeep.keep.composer.timeline.item;

import android.os.Build;
import android.util.Log;

import com.gotokeep.keep.composer.timeline.MediaItem;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-14 12:07
 */
public abstract class OverlayItem extends MediaItem {
    public static final String TYPE_SUBTITLE = "subtitle";
    public static final String TYPE_WATERMARK = "watermark";
    public static final String TYPE_LAYER = "layer";

    public static final String POSITION_LEFT = "left";
    public static final String POSITION_RIGHT = "right";
    public static final String POSITION_CENTER_HORIZONTAL = "centerHorizontal";
    public static final String POSITION_TOP = "top";
    public static final String POSITION_BOTTOM = "bottom";
    public static final String POSITION_CENTER_VERTICAL = "centerVertical";
    public static final String POSITION_CENTER = "center";


    public static final int POSITION_LEFT_INT = 0x00;
    public static final int POSITION_RIGHT_INT = 0x01;
    public static final int POSITION_CENTER_HORIZONTAL_INT = 0x02;
    public static final int POSITION_HORIZONTAL_MASK_INT = 0x02;

    public static final int POSITION_VERTICAL_OFFSET = 2;
    public static final int POSITION_VERTICAL_MASK = 0x02 << POSITION_VERTICAL_OFFSET;
    public static final int POSITION_TOP_INT = 0x00;
    public static final int POSITION_BOTTOM_INT = 0x01 << POSITION_VERTICAL_OFFSET;
    public static final int POSITION_CENTER_VERTICAL_INT = 0x02 << POSITION_VERTICAL_OFFSET;

    public static final int POSITION_CENTER_INT = POSITION_CENTER_HORIZONTAL_INT & POSITION_CENTER_VERTICAL_INT;

    private int offsetX;
    private int offsetY;
    private String position;
    private int positionInt;
    private float rotation;
    private float scale = 1.0f;
    protected String type;
    protected MediaItem baseItem;
    protected String name;

    public OverlayItem(int layer) {
        super(TYPE_OVERLAY, layer, null);
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
        positionInt = parsePosition(position);
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

    public void setBaseItem(MediaItem baseItem) {
        this.baseItem = baseItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int parsePosition(String position) {
        String tokens[] = position.split("\\|");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("Overlay", "parsePosition: " + position + ", [" + String.join(", ", tokens) + "]");
        }
        int vertical = 0;
        int horizontal = 0;
        for (String token : tokens) {
            if (POSITION_CENTER.equals(token)) {
                vertical = POSITION_CENTER_VERTICAL_INT;
                horizontal = POSITION_CENTER_HORIZONTAL_INT;
                break;
            } else if (POSITION_LEFT.equals(token)) {
                horizontal = POSITION_LEFT_INT;
            } else if (POSITION_RIGHT.equals(token)) {
                horizontal = POSITION_RIGHT_INT;
            } else if (POSITION_CENTER_HORIZONTAL.equals(token)) {
                horizontal = POSITION_CENTER_HORIZONTAL_INT;
            } else if (POSITION_TOP.equals(token)) {
                vertical = POSITION_TOP_INT;
            } else if (POSITION_BOTTOM.equals(token)) {
                vertical = POSITION_BOTTOM_INT;
            } else if (POSITION_CENTER_VERTICAL.equals(token)) {
                vertical = POSITION_CENTER_VERTICAL_INT;
            }
        }
        return vertical | horizontal;
    }

    public int getPositionInt() {
        return positionInt;
    }
}
