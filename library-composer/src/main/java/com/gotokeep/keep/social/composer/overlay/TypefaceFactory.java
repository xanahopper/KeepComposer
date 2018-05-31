package com.gotokeep.keep.social.composer.overlay;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-31 15:55
 */
public final class TypefaceFactory {
    public static Typeface createTypeface(Context context, String name) {
        AssetManager assetManager = context.getAssets();
        return Typeface.createFromAsset(assetManager, "font/ZaoZiGongFangZhuoHei-2.ttf");
    }
}
