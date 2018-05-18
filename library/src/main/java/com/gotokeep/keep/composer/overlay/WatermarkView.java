package com.gotokeep.keep.composer.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotokeep.keep.composer.R;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-18 11:15
 */
class WatermarkView extends ConstraintLayout {
    private ImageView imageView;
    private TextView textView;

    public WatermarkView(Context context) {
        super(context);
        initView(context);
    }

    public void setImage(String filePath) {
        if (imageView.getDrawable() != null && imageView.getDrawable() instanceof BitmapDrawable) {
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            drawable.getBitmap().recycle();
        }
        if (!TextUtils.isEmpty(filePath)) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public TextView getTextView() {
        return textView;
    }

    private void initView(Context context) {
        inflate(context, R.layout.layout_watermark, this);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);
    }
}
