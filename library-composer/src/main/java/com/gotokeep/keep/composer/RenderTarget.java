package com.gotokeep.keep.composer;


import android.view.Surface;

import com.gotokeep.keep.composer.source.AudioSource;

/**
 * @author xana/cuixianming
 * @version 1.0
 * @since 2018-05-15 10:03
 */
public abstract class RenderTarget {

    public abstract Surface getInputSurface();

    public abstract void updateFrame(RenderNode renderNode, long presentationTimeUs, ComposerEngine engine);

    public abstract void updateAudioChunk(AudioSource audioSource);

    public abstract void prepareVideo();

    public abstract void prepareAudio(int sampleRate, int channelCount);

    public abstract void reset();

    public abstract void complete();

    public abstract void release();
}
