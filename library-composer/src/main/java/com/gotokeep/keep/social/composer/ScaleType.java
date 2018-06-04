package com.gotokeep.keep.social.composer;

import android.support.annotation.NonNull;

public enum ScaleType {
    CENTER_CROP,
    CENTER_INSIDE,
    FIT_CENTER,
    NONE;

    @NonNull
    public static ScaleType fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal > NONE.ordinal()) {
            return ScaleType.NONE;
        }

        return ScaleType.values()[ordinal];
    }
}