package com.gotokeep.keep.composer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gotokeep.keep.composer.widgets.ScalableTextureView;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 19:07
 */
public abstract class BaseActivity extends AppCompatActivity {
    private ScalableTextureView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        previewView = findViewById(R.id.preview_view);
    }
}
