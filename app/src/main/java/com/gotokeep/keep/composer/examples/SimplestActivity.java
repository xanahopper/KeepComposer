package com.gotokeep.keep.composer.examples;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.gotokeep.keep.composer.BaseActivity;
import com.gotokeep.keep.composer.MediaComposer;
import com.gotokeep.keep.composer.MediaComposerFactory;
import com.gotokeep.keep.composer.timeline.Timeline;
import com.gotokeep.keep.composer.timeline.VideoItem;

public class SimplestActivity extends BaseActivity implements Handler.Callback {

    private MediaComposer composer;
    private Timeline timeline;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(getMainLooper(), this);
        composer = MediaComposerFactory.createMediaComposer(handler);

        timeline = new Timeline();
        timeline.addMediaItem(new VideoItem());
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
