package com.gotokeep.keep.composer.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.gotokeep.keep.composer.demo.widgets.ScalableTextureView;

public abstract class SampleActivity extends AppCompatActivity {

    protected ScalableTextureView previewView;
    protected TextView infoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        previewView = findViewById(R.id.preview_view);
        infoView = findViewById(R.id.info_view);
    }

    protected void updateInfo(String info) {
        infoView.setText(info);
    }
}
